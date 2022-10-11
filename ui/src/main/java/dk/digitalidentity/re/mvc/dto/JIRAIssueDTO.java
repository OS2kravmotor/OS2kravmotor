package dk.digitalidentity.re.mvc.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class JIRAIssueDTO {
	private long id;
	private String key;
	private JIRAFieldsDTO fields;
}
