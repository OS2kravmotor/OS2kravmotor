package dk.digitalidentity.re.security;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Component;

import dk.digitalidentity.saml.model.TokenUser;

@Component
public class SecurityUtil {

	@Autowired
	private HttpServletRequest request;
	
	public String getCvr() {
		String cvr = null;

		if (isLoggedIn()) {
			cvr = (String) request.getSession().getAttribute("cvr");
			if (cvr == null) {
				cvr = ((TokenUser) SecurityContextHolder.getContext().getAuthentication().getDetails()).getCvr();
			}
		}

		return cvr;
	}

	public Long getVendorOrganizationId() {
		Long vendorOrganizationId = null;
		if (isLoggedIn()) {
			var attributes = ((TokenUser) SecurityContextHolder.getContext().getAuthentication().getDetails()).getAttributes();
			if (attributes != null && attributes.containsKey("vendorOrganizationId")) {
				vendorOrganizationId = (long) attributes.get("vendorOrganizationId");
			}
		}
		
		return vendorOrganizationId;
	}

	public Long getVendorUserId() {
		Long vendorUserId = null;
		
		if (isLoggedIn()) {
			var attributes = ((TokenUser) SecurityContextHolder.getContext().getAuthentication().getDetails()).getAttributes();
			if (attributes != null && attributes.containsKey("vendorUserId")) {
				vendorUserId = (long) attributes.get("vendorUserId");
			}
		}

		return vendorUserId;
	}

	public static boolean isCommunityMember() {
		if (isLoggedIn()) {
			var attributes = ((TokenUser) SecurityContextHolder.getContext().getAuthentication().getDetails()).getAttributes();
			if (attributes != null && attributes.containsKey("communityMember")) {
				return (boolean) attributes.get("communityMember");
			}
		}

		return false;
	}

	public static String getMunicipalityCvr() {
		String cvr = null;

		if (isLoggedIn()) {
			cvr = ((TokenUser) SecurityContextHolder.getContext().getAuthentication().getDetails()).getCvr();
		}

		return cvr;
	}

	public static String getUser() {
		String name = null;
		
		if (isLoggedIn()) {
			name = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		}

		return name;
	}

	public static String getEmail() {
		String email = null;
		
		if (isLoggedIn()) {
			email = (String) ((TokenUser) SecurityContextHolder.getContext().getAuthentication().getDetails()).getAttributes().get("email");
		}

		return email;
	}

	public static String getUsername() {
		String name = null;

		if (isLoggedIn()) {
			name = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

			// if the principal name is in X.509 format, we just pull the CN part in this method (use getUser() for the full name)
			String nameCandidate = getNameIdValue("CN", name);
			if (nameCandidate.length() > 0) {
				name = nameCandidate;
			}
		}

		return name;
	}
	
	public static List<String> getRoles() {
		List<String> roles = new ArrayList<>();

		if (isLoggedIn()) {
			for (GrantedAuthority grantedAuthority : (SecurityContextHolder.getContext().getAuthentication()).getAuthorities()) {
				roles.add(grantedAuthority.getAuthority());
			}
		}

		return roles;
	}

	public static boolean isLoggedIn() {
		if (SecurityContextHolder.getContext().getAuthentication() != null && SecurityContextHolder.getContext().getAuthentication().getDetails() != null && SecurityContextHolder.getContext().getAuthentication().getDetails() instanceof TokenUser) {
			return true;
		}

		return false;
	}
	
	public static boolean getBooleanAttribute(String key) {
		if (isLoggedIn()) {
			Object o = ((TokenUser) SecurityContextHolder.getContext().getAuthentication().getDetails()).getAttributes().get(key);
			if (o == null) {
				return false;
			}
			
			return (boolean) o;
		}

		return false;
	}
	
	public static String getStringAttribute(String key) {
		if (isLoggedIn()) {
			return (String) ((TokenUser) SecurityContextHolder.getContext().getAuthentication().getDetails()).getAttributes().get(key);
		}
		return "";
	}

	private static String getNameIdValue(String field, String nameId) {
		StringBuilder builder = new StringBuilder();

		int idx = nameId.indexOf(field + "=");
		if (idx >= 0) {
			for (int i = idx + field.length() + 1; i < nameId.length(); i++) {
				if (nameId.charAt(i) == ',') {
					break;
				}

				builder.append(nameId.charAt(i));
			}
		}

		return builder.toString();
	}

	public void switchToGlobalEditor() {
		if (getBooleanAttribute("globalEditor")) {
			TokenUser tokenUser = ((TokenUser) SecurityContextHolder.getContext().getAuthentication().getDetails());
			
			List<GrantedAuthority> authorities = new ArrayList<>();
			authorities.add(new SimpleGrantedAuthority("ROLE_http://kravmotoren.dk/globaleditor"));
			authorities.add(new SimpleGrantedAuthority("ROLE_http://kravmotoren.dk/editor"));
			tokenUser.setAuthorities(authorities);
			
			tokenUser.setCvr("00000000");
			
			// so spring really likes caching this object, so we need to make sure Spring knows about the new version
			if (SecurityContextHolder.getContext() != null &&
				SecurityContextHolder.getContext().getAuthentication() != null &&
				SecurityContextHolder.getContext().getAuthentication() instanceof UsernamePasswordAuthenticationToken) {

				SecurityContext securityContext = SecurityContextHolder.getContext();
				UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(getUser(), null, authorities);
				
				authentication.setDetails(tokenUser);
				SecurityContextHolder.getContext().setAuthentication(authentication);
				
				if (request != null) {
					HttpSession session = request.getSession(true);
					session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, securityContext);
				}
			}
		}
	}
	
	public void switchFromGlobalEditor() {
		if (getBooleanAttribute("globalEditor")) {
			TokenUser tokenUser = ((TokenUser) SecurityContextHolder.getContext().getAuthentication().getDetails());
			
			List<GrantedAuthority> authorities = new ArrayList<>();
			
			if (getBooleanAttribute("editor")) {
				authorities.add(new SimpleGrantedAuthority("ROLE_http://kravmotoren.dk/editor"));
			}
			if (getBooleanAttribute("purchaser")) {
				authorities.add(new SimpleGrantedAuthority("ROLE_http://kravmotoren.dk/purchaser"));
			}
			if (getBooleanAttribute("admin")) {
				authorities.add(new SimpleGrantedAuthority("ROLE_http://kravmotoren.dk/admin"));
			}
			
			tokenUser.setAuthorities(authorities);
			
			tokenUser.setCvr(getStringAttribute("originalCvr"));
			
			// so spring really likes caching this object, so we need to make sure Spring knows about the new version
			if (SecurityContextHolder.getContext() != null &&
				SecurityContextHolder.getContext().getAuthentication() != null &&
				SecurityContextHolder.getContext().getAuthentication() instanceof UsernamePasswordAuthenticationToken) {

				SecurityContext securityContext = SecurityContextHolder.getContext();
				UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(getUser(), null, authorities);
				
				authentication.setDetails(tokenUser);
				SecurityContextHolder.getContext().setAuthentication(authentication);
				
				if (request != null) {
					HttpSession session = request.getSession(true);
					session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, securityContext);
				}
			}
		}
	}
}
