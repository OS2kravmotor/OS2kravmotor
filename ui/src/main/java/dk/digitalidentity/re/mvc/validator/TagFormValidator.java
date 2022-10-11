package dk.digitalidentity.re.mvc.validator;

import dk.digitalidentity.re.dao.TagDao;
import dk.digitalidentity.re.mvc.form.TagForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class TagFormValidator implements Validator {

	@Override
	public boolean supports(Class<?> aClass){
		return (TagForm.class.isAssignableFrom(aClass));
	}

	@Autowired
	private TagDao tagDao;

	@Override
	public void validate(Object o, Errors errors) {
		TagForm tagForm = (TagForm) o;

		if (tagForm.getName().isEmpty()) {
			errors.rejectValue("name", "error.tag.name.empty");
		}

		if (tagDao.getByName(tagForm.getName()) != null && tagDao.getByName(tagForm.getName()).getId() != tagForm.getId()){
			errors.rejectValue("name", "error.tag.name.exists");
		}

		if (tagForm.getQuestion().isEmpty()) {
			errors.rejectValue("question", "error.tag.question.empty");
		}
	}
}

