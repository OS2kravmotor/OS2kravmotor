package dk.digitalidentity.re.config;

import java.io.IOException;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfiguration {

    @Bean
    public ClientHttpRequestFactory clientHttpRequestFactory() {
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();

        // ensure we have cookie support
        HttpClient httpClient = HttpClientBuilder.create()
                .useSystemProperties()
                .build();

        factory.setConnectTimeout(5000);
        factory.setReadTimeout(10000);
        factory.setHttpClient(httpClient);

        return factory;
    }

	@Bean
	public RestTemplate restTemplate(ClientHttpRequestFactory clientHttpRequestFactory) {
		RestTemplate restTemplate = new RestTemplate(clientHttpRequestFactory);
		restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {

			// otherwise RestTemplate throws an exception on HTTP statuscodes != 200
			@Override
			public boolean hasError(ClientHttpResponse response) throws IOException {
				return false;
			}
		});

		return restTemplate;
	}
}
