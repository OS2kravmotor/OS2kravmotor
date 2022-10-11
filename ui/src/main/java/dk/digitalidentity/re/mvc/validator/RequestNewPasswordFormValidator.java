package dk.digitalidentity.re.mvc.validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import dk.digitalidentity.re.dao.VendorUserDao;
import dk.digitalidentity.re.mvc.form.RequestNewPasswordForm;

@Component
public class RequestNewPasswordFormValidator implements Validator {

	@Autowired
	VendorUserDao vendorUserDao;

	@Override
	public boolean supports(Class<?> aClass) {
		return (RequestNewPasswordForm.class.isAssignableFrom(aClass));
	}

	@Override
	public void validate(Object o, Errors errors) {
		var email = ((RequestNewPasswordForm) o).getEmail();
		var vendorUser = vendorUserDao.getByEmail(email);
		if( vendorUser == null)
			errors.rejectValue("email", "error.vendor.requestnewpassword.usernotfound");

	}
}
