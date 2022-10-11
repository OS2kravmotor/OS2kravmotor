package dk.digitalidentity.re.mvc.validator;

import dk.digitalidentity.re.mvc.form.CustomRequirementForm;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class CustomRequirementFormValidator implements Validator {
	
	@Override
	public boolean supports(Class<?> aClass) {
		return (CustomRequirementForm.class.isAssignableFrom(aClass));
	}

	@Override
	public void validate(Object o, Errors errors) {
		CustomRequirementForm requirementForm = (CustomRequirementForm) o;

		if (requirementForm.getName().isEmpty()) {
			errors.rejectValue("name", "error.requirement.custom.name.empty");
		}
		
		if (requirementForm.getCategory() < 0) {
			errors.rejectValue("category", "error.requirement.custom.category.empty");
		}
		
		if (requirementForm.getImportance() == null) {
			errors.rejectValue("importance", "error.requirement.custom.importance.empty");
		}

		if(requirementForm.getDescription().isEmpty()) {
			errors.rejectValue("description", "error.requirement.custom.description.empty");
		}
	}
}
