package dk.digitalidentity.re.mvc.form;

import lombok.Data;

@Data
public class DomainForm {
	private long id;
	private String name;

	public DomainForm() {
		
	}

	public DomainForm(long id, String name) {
		this.id = id;
		this.name = name;
	}
}
