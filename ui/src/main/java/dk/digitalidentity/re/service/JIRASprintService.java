package dk.digitalidentity.re.service;

import java.io.IOException;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import dk.digitalidentity.re.dao.JIRASprintDao;
import dk.digitalidentity.re.dao.model.JIRASprint;
import dk.digitalidentity.re.mvc.dto.JIRAIssuesDTO;
import dk.digitalidentity.re.mvc.dto.JIRASprintsDTO;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class JIRASprintService {

	@Autowired
	private JIRASprintDao jiraSprintDao;

	@Autowired
	private RestTemplate restTemplate;

	@Value("${jira.login}:${jira.password}")
	private String plainCreds;

	@Value("${jira.baseURL}")
	private String jiraBaseUrl;

	@Value("${jira.boardId}")
	private String jiraBoardId;

	public List<JIRASprint> findAll() {
		return jiraSprintDao.findAll();
	}

	public JIRASprint save(JIRASprint entity) {
		return jiraSprintDao.save(entity);
	}
	
	public boolean isConfigured() {
		if (plainCreds == null || plainCreds.length() < 2) {
			log.info("No jira username/password configured - will not pull data from jira!");
			return false;
		}

		return true;
	}

	@Retryable(value = { IOException.class }, maxAttempts = 3, backoff = @Backoff(delay = 5000))
	public JIRASprintsDTO getSprints() throws IOException {
		HttpEntity<String> request = new HttpEntity<String>(getJIRAHeaders());

		ResponseEntity<String> response = restTemplate.exchange(jiraBaseUrl + "/board/" + jiraBoardId + "/sprint", HttpMethod.GET, request, String.class);

		if (response.getStatusCodeValue() == 200) {
			return new ObjectMapper().readValue(response.getBody(), JIRASprintsDTO.class);
		}

		throw new IOException("Failed to fetch sprints from jira (" + response.getStatusCodeValue() + ") - " + response.getBody());
	}

	@Retryable(value = { IOException.class }, maxAttempts = 3, backoff = @Backoff(delay = 5000))
	public JIRAIssuesDTO getIssues(long issueId) throws IOException {
		HttpEntity<String> request = new HttpEntity<String>(getJIRAHeaders());

		String issuesURL = jiraBaseUrl + "/sprint/" + issueId + "/issue?fields=summary";

		ResponseEntity<String> response = restTemplate.exchange(issuesURL, HttpMethod.GET, request, String.class);

		if (response.getStatusCodeValue() == 200) {
			return new ObjectMapper().readValue(response.getBody(), JIRAIssuesDTO.class);
		}

		throw new IOException("Failed to fetch issues from jira (" + response.getStatusCodeValue() + ") - " + response.getBody());
	}
	
	private HttpHeaders getJIRAHeaders() {
		byte[] plainCredsBytes = plainCreds.getBytes();
		byte[] base64CredsBytes = Base64.encodeBase64(plainCredsBytes);
		String base64Creds = new String(base64CredsBytes);

		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Basic " + base64Creds);
		return headers;
	}
}
