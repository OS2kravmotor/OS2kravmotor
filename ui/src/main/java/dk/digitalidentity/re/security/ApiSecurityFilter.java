package dk.digitalidentity.re.security;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import dk.digitalidentity.re.dao.SettingDao;
import dk.digitalidentity.re.dao.model.Setting;
import dk.digitalidentity.re.dao.model.enums.CustomerSetting;
import dk.digitalidentity.saml.model.TokenUser;

public class ApiSecurityFilter implements Filter {
	private static final Logger logger = Logger.getLogger(ApiSecurityFilter.class);
	private SettingDao settingsDao;

	public ApiSecurityFilter(SettingDao settingDao) {
		this.settingsDao = settingDao;
	}

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) servletRequest;
		HttpServletResponse response = (HttpServletResponse) servletResponse;

		String cvr = request.getHeader("cvr");
		Setting setting = settingsDao.getByKeyAndCvr(CustomerSetting.APIKEY.toString(), cvr);
		if(setting == null || cvr == null) {
			unauthorized(response, "Invalid CVR or invalid ApiKey header", "");
			return;
		}
		String apiKey = setting.getValue();
		
		// we are using a custom header instead of Authorization because the Authorization header plays very badly with the SAML filter
		String authHeader = request.getHeader("ApiKey");
		if (authHeader != null && apiKey != null) {
			if (!authHeader.equals(apiKey)) {
				unauthorized(response, "Invalid ApiKey header", authHeader);
				return;
			}

			// this is a bit of a hack, but we fake that the api logged in using a token, so all of our existing security code just works without further modifications
			TokenUser tokenUser = TokenUser.builder().cvr(cvr).authorities(new ArrayList<GrantedAuthority>()).username("API").build();
			UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken("API", apiKey, new ArrayList<GrantedAuthority>());
			token.setDetails(tokenUser);
			SecurityContextHolder.getContext().setAuthentication(token);

			filterChain.doFilter(servletRequest, servletResponse);
		}
		else {
			unauthorized(response, "Missing ApiKey header", authHeader);
		}
	}

	private static void unauthorized(HttpServletResponse response, String message, String authHeader) throws IOException {
		logger.warn(message + " (authHeader = " + authHeader + ")");
		response.sendError(401, message);
	}

	@Override
	public void destroy() {
		;
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		;
	}

}
