package dk.digitalidentity.re.mvc.validator;

import dk.digitalidentity.re.mvc.form.PurchaseForm;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class PurchaseFormValidator implements Validator {

	@Override
	public boolean supports(Class<?> aClass) {
		return (PurchaseForm.class.isAssignableFrom(aClass));
	}

	@Override
	public void validate(Object o, Errors errors) {
		PurchaseForm purchaseForm = (PurchaseForm) o;

		if (purchaseForm.getTitle()==null || purchaseForm.getTitle().isEmpty()) {
			errors.rejectValue("title", "error.purchase.title.empty", "");
		}
		if (purchaseForm.getTitle()==null || purchaseForm.getTitle().length() < 5) {
			errors.rejectValue("title", "error.purchase.title.badsize", "");
		}
		if (purchaseForm.getDescription()==null || purchaseForm.getDescription().isEmpty()) {
			errors.rejectValue("description", "error.purchase.description.empty","");
		}
		if (purchaseForm.getDescription()==null || purchaseForm.getDescription().length() < 5) {
			errors.rejectValue("description", "error.purchase.description.badsize","");
		}
		if (purchaseForm.getDomains()==null || purchaseForm.getDomains().isEmpty()) {
			errors.rejectValue("domains", "error.purchase.domain.empty");
		}
		if (purchaseForm.getEmail()==null || purchaseForm.getEmail().isEmpty()) {
			errors.rejectValue("email", "error.purchase.email.empty");
		} else if (!ValidatorUtil.isValidEmailAddress(purchaseForm.getEmail())) {
			errors.rejectValue("email", "error.purchase.email.incorrect");
		}
	}
}
