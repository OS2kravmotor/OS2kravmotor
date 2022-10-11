package dk.digitalidentity.re.mvc.validator;

import dk.digitalidentity.re.dao.PrincipleDao;
import dk.digitalidentity.re.mvc.form.PrincipleForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class PrincipleFormValidator implements Validator {

	@Override
	public boolean supports(Class<?> aClass) { return (PrincipleForm.class.isAssignableFrom(aClass)); }

	@Autowired
	private PrincipleDao principleDao;

	@Override
	public void validate(Object o, Errors errors) {
		PrincipleForm principleForm = (PrincipleForm) o;

		if (principleForm.getName().isEmpty()) {
			errors.rejectValue("name", "error.principle.name.empty");
		}

		if (principleDao.getByName(principleForm.getName()) != null && principleDao.getByName(principleForm.getName()).getId() != principleForm.getId()){
			errors.rejectValue("name", "error.principle.name.exists");
		}

		if (principleForm.getReference().isEmpty()) {
			errors.rejectValue("reference", "error.principle.reference.empty");
		}
	}
}
