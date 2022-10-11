package dk.digitalidentity.re.security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import dk.digitalidentity.re.dao.PurchaseVendorDao;
import dk.digitalidentity.re.dao.VendorUserDao;
import dk.digitalidentity.re.dao.model.PurchaseVendor;
import dk.digitalidentity.re.dao.model.VendorUser;
import dk.digitalidentity.saml.model.TokenUser;
import lombok.extern.log4j.Log4j;

@Log4j
public class VendorLoginFilter implements Filter {
	private PurchaseVendorDao purchaseVendorDao;
	private VendorUserDao vendorUserDao;

	public VendorLoginFilter(PurchaseVendorDao purchaseVendorDao, VendorUserDao vendorUserDao) {
		this.purchaseVendorDao = purchaseVendorDao;
		this.vendorUserDao = vendorUserDao;
	}
	
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest servletRequest = (HttpServletRequest) request;
		HttpServletResponse servletResponse = (HttpServletResponse) response;

		if( servletRequest.getRequestURI().equalsIgnoreCase("/vendor/login"))
		{
			String username = servletRequest.getParameter("username");
			String password = servletRequest.getParameter("password");
			if (username != null && password != null) {

				VendorUser vendorUser = vendorUserDao.getByEmail(username);
				if( vendorUser != null )
				{
					var encoder = new BCryptPasswordEncoder();
					if( encoder.matches(password,vendorUser.getPassword()))
					{
						ArrayList<GrantedAuthority> authorities = new ArrayList<>();
						authorities.add(new SimpleGrantedAuthority("ROLE_http://kravmotoren.dk/vendoruser"));
						if( vendorUser.isAdmin())
						{
							authorities.add(new SimpleGrantedAuthority("ROLE_http://kravmotoren.dk/vendoradministrator"));
						}

						// this is a bit of a hack, but we fake that the vendor logged in using a token, so all of our existing security code just works without further modifications
						UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(username, password, authorities);
						var attributes = new HashMap<String, Object>();
						attributes.put("vendorOrganizationId",vendorUser.getVendorOrganization().getId());
						attributes.put("vendorUserId",vendorUser.getId());

						var tokenUser = TokenUser.builder().authorities(authorities).username(username).cvr("").attributes(attributes).build();
						token.setDetails(tokenUser);
						SecurityContextHolder.getContext().setAuthentication(token);

						servletResponse.sendRedirect(servletRequest.getContextPath() + "/vendor/purchase/list");

						log.info("Vendor " + username + " logged in");
						return;
					}
				}
				// else-part is support for old-fashioned PurchaseVendor. Can be removed when we no longer support purchaseVendor users
				else
				{
					PurchaseVendor purchaseVendor = purchaseVendorDao.getByUsernameAndPassword(username, password);
					if (purchaseVendor != null) {
						ArrayList<GrantedAuthority> authorities = new ArrayList<>();
						authorities.add(new SimpleGrantedAuthority("ROLE_http://kravmotoren.dk/vendor"));

						// this is a bit of a hack, but we fake that the vendor logged in using a token, so all of our existing security code just works without further modifications
						UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(username, password, authorities);
						token.setDetails(TokenUser.builder().authorities(authorities).username(username).cvr("").build());
						SecurityContextHolder.getContext().setAuthentication(token);

						servletResponse.sendRedirect(servletRequest.getContextPath() + "/vendor");

						log.info("Vendor " + username + " logged in");
						return;
					}
				}

				servletResponse.sendRedirect(servletRequest.getContextPath() + "/vendor/login?wrongpassword=true");
			}
		}

		chain.doFilter(request, response);
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		;
	}

	@Override
	public void destroy() {
		;
	}
}
