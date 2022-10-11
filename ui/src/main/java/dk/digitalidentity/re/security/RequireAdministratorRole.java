package dk.digitalidentity.re.security;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.springframework.security.access.prepost.PreAuthorize;

@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("hasRole('ROLE_http://kravmotoren.dk/admin')")
public @interface RequireAdministratorRole {

}
