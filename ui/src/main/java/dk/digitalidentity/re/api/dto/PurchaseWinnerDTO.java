package dk.digitalidentity.re.api.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PurchaseWinnerDTO {
	private String name;
	private List<AnswerDTO> answers = new ArrayList<>();
}
