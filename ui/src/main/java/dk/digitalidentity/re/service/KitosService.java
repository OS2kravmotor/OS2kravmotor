package dk.digitalidentity.re.service;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import dk.digitalidentity.re.service.model.KitosItSystemDTO;
import dk.digitalidentity.re.service.model.KitosWrapperDTO;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class KitosService {

	@Autowired
	private RestTemplate restTemplate;

	@Value("${kitos.rest.url}")
	private String kitosUrl;
	
	@Value("${kitos.rest.loginurl}")
	private String kitosLoginUrl;
	
	@Value("${kitos.rest.username}")
	private String kitosUsername;
	
	@Value("${kitos.rest.password}")
	private String kitosPassword;

	public boolean isConfigured() {
		if (StringUtils.isEmpty(kitosUsername) || StringUtils.isEmpty(kitosPassword)) {
			log.warn("No KITOS username/password configured!");
			return false;
		}

		return true;
	}

	@Retryable(value = { IOException.class }, maxAttempts = 3, backoff = @Backoff(delay = 5000))
	public List<KitosItSystemDTO> getItSystemsFromKitos() throws IOException {
		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Type", "application/json");

		HttpEntity<String> request = new HttpEntity<String>("{\"email\": \"" + kitosUsername + "\",\"password\": \"" + kitosPassword + "\"}", headers);

		ResponseEntity<String> response = restTemplate.postForEntity(kitosLoginUrl, request, String.class);
		if (response.getStatusCodeValue() != 200 && response.getStatusCodeValue() != 201) {
			throw new IOException("Failed to login to KITOS, responseCode=" + response.getStatusCodeValue() + ", response=" + response.getBody());
		}

		response = restTemplate.getForEntity(kitosUrl, String.class);

		if (response.getStatusCodeValue() == 200) {
			KitosWrapperDTO dto = new ObjectMapper().readValue(response.getBody(), KitosWrapperDTO.class);
			
			return dto.getValue();
		}

		throw new IOException("Failed to call KITOS, responseCode=" + response.getStatusCodeValue() + ", response=" + response.getBody());
	}
}
