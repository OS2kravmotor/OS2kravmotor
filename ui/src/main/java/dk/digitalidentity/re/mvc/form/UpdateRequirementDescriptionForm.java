package dk.digitalidentity.re.mvc.form;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateRequirementDescriptionForm {
	private String description;
	private long requirementId;
	private long purchaseRequirementId;
}
