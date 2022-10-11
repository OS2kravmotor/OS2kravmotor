package dk.digitalidentity.re.mvc.form;

import lombok.Data;

import java.util.List;

@Data
public class QuestionnaireForm {
	private long id;
	private List<String> answers;
}
