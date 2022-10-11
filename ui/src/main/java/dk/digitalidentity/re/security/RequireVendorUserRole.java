package dk.digitalidentity.re.security;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("hasRole('ROLE_http://kravmotoren.dk/vendoruser')")
public @interface RequireVendorUserRole {

}
