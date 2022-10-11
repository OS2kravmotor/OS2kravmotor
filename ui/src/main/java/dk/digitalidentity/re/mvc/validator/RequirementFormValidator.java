package dk.digitalidentity.re.mvc.validator;

import dk.digitalidentity.re.mvc.form.RequirementForm;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class RequirementFormValidator implements Validator {

	@Override
	public boolean supports(Class<?> aClass) {
		return (RequirementForm.class.isAssignableFrom(aClass));
	}

	@Override
	public void validate(Object o, Errors errors) {
		RequirementForm requirementForm = (RequirementForm) o;

		if (requirementForm.getName().isEmpty()) {
			errors.rejectValue("name", "error.requirement.name.empty");
		}
		if (!requirementForm.isAvailableForAllDomains() && requirementForm.getDomains().isEmpty()) {
			errors.rejectValue("domains", "error.requirement.domain.empty");
		}
	}
}
