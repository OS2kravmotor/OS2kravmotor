package dk.digitalidentity.re.mvc.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class JIRAIssuesDTO {
	private List<JIRAIssueDTO> issues;
}
