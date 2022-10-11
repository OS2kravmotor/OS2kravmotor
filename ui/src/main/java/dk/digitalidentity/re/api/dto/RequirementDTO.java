package dk.digitalidentity.re.api.dto;

import dk.digitalidentity.re.dao.model.enums.Importance;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RequirementDTO {
	private long id;
	private String name;
	private String description;
	private Importance importance;
}
