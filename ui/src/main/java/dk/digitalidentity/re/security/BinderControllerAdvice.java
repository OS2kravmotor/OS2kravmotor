package dk.digitalidentity.re.security;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;

@ControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE)
public class BinderControllerAdvice {

	// temporary security mitigation of SpringShell - can be removed once upgraded
	// to Spring Boot 2.5.12 or 2.6.6 (or later)

	@InitBinder
	public void setAllowedFields(WebDataBinder dataBinder) {
		String[] denylist = new String[] { "class.*", "Class.*", "*.class.*", "*.Class.*" };
		dataBinder.setDisallowedFields(denylist);
	}
}
