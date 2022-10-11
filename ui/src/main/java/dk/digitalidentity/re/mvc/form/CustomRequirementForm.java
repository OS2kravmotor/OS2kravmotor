package dk.digitalidentity.re.mvc.form;

import dk.digitalidentity.re.dao.model.enums.Importance;
import lombok.Data;

@Data
public class CustomRequirementForm {
	private String name;
	private String description;
	private Importance importance;
	private long category;
}
