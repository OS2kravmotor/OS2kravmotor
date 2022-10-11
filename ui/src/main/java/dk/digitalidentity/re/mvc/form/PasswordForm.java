package dk.digitalidentity.re.mvc.form;

import lombok.Data;

@Data
public class PasswordForm {
    private String currentPassword;
    private String newPassword;
    private String repeatPassword;
}
