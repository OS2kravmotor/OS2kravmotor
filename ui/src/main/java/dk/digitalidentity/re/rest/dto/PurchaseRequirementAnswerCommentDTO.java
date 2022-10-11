package dk.digitalidentity.re.rest.dto;

import dk.digitalidentity.re.dao.model.enums.CustomerAnswer;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PurchaseRequirementAnswerCommentDTO {
	private CustomerAnswer status;
	private String comment;

}
