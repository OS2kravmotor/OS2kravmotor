package dk.digitalidentity.re.mvc.form;

import lombok.Data;
import org.springframework.data.util.Pair;

import dk.digitalidentity.re.dao.model.enums.AnswerChoice;

import java.util.ArrayList;
import java.util.List;

@Data
public class AnswerForm {
	private long id;
	private String vendorname;
	private List<String> answers;
	private List<AnswerChoice> choices;
	private List<String> prices;
	private List<Pair<String,String>> answersRichObject;
	private List<RequirementForm> requirementsFromPurchase = new ArrayList<>();
	private long requirementId;
}
