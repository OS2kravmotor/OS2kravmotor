package dk.digitalidentity.re.mvc.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequirementTreeDTO {
	private String id;
	private String parent = "#";
	private String text;
}
