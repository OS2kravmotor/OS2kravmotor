package dk.digitalidentity.re.api.dto;

import dk.digitalidentity.re.dao.model.enums.AnswerChoice;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnswerDTO {
	private long requirementId;
	private String detail;
	private AnswerChoice choice;
	private String price;
}
