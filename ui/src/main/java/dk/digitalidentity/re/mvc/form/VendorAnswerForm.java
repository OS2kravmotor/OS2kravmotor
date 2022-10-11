package dk.digitalidentity.re.mvc.form;

import java.util.List;

import lombok.Data;

@Data
public class VendorAnswerForm {
	private List<VendorRequirementAnswerForm> requirementAnswers;
}
