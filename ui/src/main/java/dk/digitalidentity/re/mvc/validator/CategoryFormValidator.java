package dk.digitalidentity.re.mvc.validator;

import dk.digitalidentity.re.dao.CategoryDao;
import dk.digitalidentity.re.dao.model.Category;
import dk.digitalidentity.re.mvc.form.CategoryForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class CategoryFormValidator implements Validator {

	@Autowired
	private CategoryDao categoryDao;

	@Override
	public boolean supports(Class<?> aClass){
		return (CategoryForm.class.isAssignableFrom(aClass));
	}

	@Override
	public void validate(Object o, Errors errors) {
		CategoryForm categoryForm = (CategoryForm) o;

		if (categoryForm.getName().isEmpty()) {
			errors.rejectValue("name", "error.category.name.empty");
		}
		Category category = categoryDao.getByName(categoryForm.getName());
		if (category != null && category.getId() != categoryForm.getId()) {
			errors.rejectValue("name", "error.category.name.exists");
		}
	}
}
