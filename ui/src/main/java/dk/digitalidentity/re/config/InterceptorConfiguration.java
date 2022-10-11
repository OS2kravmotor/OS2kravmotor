package dk.digitalidentity.re.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import dk.digitalidentity.re.interceptor.DatabaseOperarationInterceptor;

@Configuration
public class InterceptorConfiguration {

	@Bean
	public DatabaseOperarationInterceptor databaseOperarationInterceptor() {
		return new DatabaseOperarationInterceptor();
	}
}