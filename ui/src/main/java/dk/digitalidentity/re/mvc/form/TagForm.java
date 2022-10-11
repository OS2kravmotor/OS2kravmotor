package dk.digitalidentity.re.mvc.form;

import lombok.Data;

@Data
public class TagForm {
	private long  id;
	private String name;
	private String question;

	public TagForm() {}

	public TagForm(String name, long id, String question){
		this.id = id;
		this.name = name;
		this.question = question;
	}
}
