package dk.digitalidentity.re.mvc.validator;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import dk.digitalidentity.re.mvc.form.InviteVendorForm;

@Component
public class InviteFormValidator implements Validator {

	@Override
	public boolean supports(Class<?> aClass) {
		return (InviteVendorForm.class.isAssignableFrom(aClass));
	}

	@Override
	public void validate(Object o, Errors errors) {
		InviteVendorForm inviteForm = (InviteVendorForm) o;

		if (inviteForm.getEmail() == null || inviteForm.getEmail().isEmpty()) {
			errors.rejectValue("email", "error.purchase.invite.email.empty");
		} else if (!ValidatorUtil.isValidEmailAddress(inviteForm.getEmail())) {
			errors.rejectValue("email", "error.purchase.invite.vendor.email.incorrect");
		}

		if (inviteForm.getMessage() == null || inviteForm.getMessage().isEmpty()) {
			errors.rejectValue("message", "error.purchase.invite.message.empty");
		}
	}
}
