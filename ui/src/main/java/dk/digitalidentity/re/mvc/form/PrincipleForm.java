package dk.digitalidentity.re.mvc.form;

import lombok.Data;

@Data
public class PrincipleForm {
	private long  id;
	private String name;
	private String reference;

	public PrincipleForm() {}

	public PrincipleForm(long id, String name) {
		this.id = id;
		this.name = name;
	}
}
