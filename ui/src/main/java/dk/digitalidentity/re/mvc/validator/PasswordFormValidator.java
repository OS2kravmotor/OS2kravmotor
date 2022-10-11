package dk.digitalidentity.re.mvc.validator;

import dk.digitalidentity.re.mvc.form.PasswordForm;
import dk.digitalidentity.re.service.VendorOrganizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.regex.Pattern;

@Component
public class PasswordFormValidator implements Validator {

    @Autowired
    VendorOrganizationService vendorOrganizationService;

    @Override
    public boolean supports(Class<?> aClass) {
        return (PasswordForm.class.isAssignableFrom(aClass));
    }

    @Override
    public void validate(Object o, Errors errors) {
        var passwordForm = (PasswordForm) o;
        var vendorUser = vendorOrganizationService.getVendorUser();
        var encoder = new BCryptPasswordEncoder();

        var currentPassword = passwordForm.getCurrentPassword();

        if (!encoder.matches(currentPassword, vendorUser.getPassword())) {
            errors.rejectValue("currentPassword", "error.vendoruser.password.wrongpassword");
        }

        var newPassword = passwordForm.getNewPassword();
        newPassword = newPassword == null ? "" : newPassword;

        // OWASP Password Complexity
        // Password must meet at least 3 out of the following 4 complexity rules
        var complexityCount = 0;
        //at least 1 uppercase character (A-Z)
        if (Pattern.compile("[A-ZÆØÅ]").matcher(newPassword).find()) {
            complexityCount++;
        }

        //at least 1 lowercase character (a-z)
        if (Pattern.compile("[a-zæøå]").matcher(newPassword).find()) {
            complexityCount++;
        }

        //at least 1 digit (0-9)
        if (Pattern.compile("\\d").matcher(newPassword).find()) {
            complexityCount++;
        }

        //at least 1 special character (punctuation) — do not forget to treat space as special characters too
        if (Pattern.compile("[^\\wæøå\\d]", Pattern.CASE_INSENSITIVE).matcher(newPassword).find()) {
            complexityCount++;
        }

        if (complexityCount < 3) {
            errors.rejectValue("newPassword", "error.vendoruser.password.complexity");
        }

        //at least 10 characters
        //at most 128 characters
        if (newPassword.length() < 10 || newPassword.length() > 128) {
            errors.rejectValue("newPassword", "error.vendoruser.password.size");
        }

        //not more than 2 identical characters in a row (e.g., 111 not allowed)
        if (Pattern.compile("(.)\\1\\1").matcher(newPassword).find()) {
            errors.rejectValue("newPassword", "error.vendoruser.password.identical");
        }

        if(!newPassword.equals(passwordForm.getRepeatPassword()))
        {
            errors.rejectValue("repeatPassword", "error.vendoruser.password.repeaterror");
        }

    }
}
