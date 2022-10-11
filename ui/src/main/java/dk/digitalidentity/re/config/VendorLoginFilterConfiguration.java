package dk.digitalidentity.re.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import dk.digitalidentity.re.dao.PurchaseVendorDao;
import dk.digitalidentity.re.dao.VendorUserDao;
import dk.digitalidentity.re.security.VendorLoginFilter;

@Configuration
public class VendorLoginFilterConfiguration {
	
	@Autowired
	private PurchaseVendorDao purchaseVendorDao;

	@Autowired
	private VendorUserDao vendorUserDao;

	@Bean
	public FilterRegistrationBean<VendorLoginFilter> vendorLoginFilter() {
		VendorLoginFilter filter = new VendorLoginFilter(purchaseVendorDao, vendorUserDao);

		FilterRegistrationBean<VendorLoginFilter> filterRegistrationBean = new FilterRegistrationBean<VendorLoginFilter>(filter);
		filterRegistrationBean.addUrlPatterns("/vendor/*");

		return filterRegistrationBean;
	}
}
