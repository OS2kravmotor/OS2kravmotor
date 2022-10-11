package dk.digitalidentity.re.mvc.validator;

import dk.digitalidentity.re.dao.VendorUserDao;
import dk.digitalidentity.re.mvc.form.VendorUserForm;
import dk.digitalidentity.re.service.VendorOrganizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class VendorUserFormValidator implements Validator {

	@Autowired
	VendorUserDao vendorUserDao;

	@Autowired
	VendorOrganizationService vendorOrganizationService;

	@Override
	public boolean supports(Class<?> aClass) {
		return (VendorUserForm.class.isAssignableFrom(aClass));
	}

	@Override
	public void validate(Object o, Errors errors) {
		var vendorUserForm = (VendorUserForm) o;

		if (vendorUserForm.getEmail() == null || vendorUserForm.getEmail().isEmpty()) {
			errors.rejectValue("email", "error.vendoruser.email.empty");
		} else if (!ValidatorUtil.isValidEmailAddress(vendorUserForm.getEmail())) {
			errors.rejectValue("email", "error.vendoruser.email.incorrect");
		}

		// it is not allowed to rename a users email to the same as an existing users email
		// nor is it allowed to create a new user with the same email as an existing user
		var existingUser = vendorUserDao.getByEmail(vendorUserForm.getEmail());
		if( existingUser != null && existingUser.getId() != vendorUserForm.getId() )
		{
			errors.rejectValue("email", "error.vendoruser.email.duplicate");
		}

		// it is not allowed to remove admin rights if there are no other administrators in the same organization
		if( !vendorUserForm.isAdmin() )
		{
			// if no other admin user exists in the same vendor organization
			if( vendorOrganizationService.getVendorOrganization().getVendorUsers().stream().noneMatch(u -> u.isAdmin() && u.getId() != vendorUserForm.getId() ) )
			{
				errors.rejectValue("admin", "error.vendoruser.admin.lastadmin");
			}
		}


	}
}
