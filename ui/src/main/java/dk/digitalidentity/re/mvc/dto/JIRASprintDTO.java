package dk.digitalidentity.re.mvc.dto;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import dk.digitalidentity.re.dao.model.enums.JIRASprintState;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class JIRASprintDTO {
	private long id;
	private JIRASprintState state;
	private String name;
	private Date startDate;
	private Date endDate;
}
