package dk.digitalidentity.re.mvc.validator;

import dk.digitalidentity.re.dao.DomainDao;
import dk.digitalidentity.re.dao.model.Domain;
import dk.digitalidentity.re.mvc.form.DomainForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class DomainFormValidator implements Validator {

	@Autowired
	private DomainDao domainDao;

	@Override
	public boolean supports(Class<?> aClass) {
		return (DomainForm.class.isAssignableFrom(aClass));
	}

	@Override
	public void validate(Object o, Errors errors) {
		DomainForm domainForm = (DomainForm) o;

		if (domainForm.getName().isEmpty()) {
			errors.rejectValue("name", "error.domain.name.empty");
		}
		Domain domain = domainDao.getByName(domainForm.getName());
		if (domain != null && domain.getId() != domainForm.getId()) {
			errors.rejectValue("name", "error.domain.name.exists");
		}
	}
}
