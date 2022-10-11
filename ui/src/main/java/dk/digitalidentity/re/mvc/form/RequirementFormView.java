package dk.digitalidentity.re.mvc.form;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class RequirementFormView {
	private long id;
	private String name;
	private String description;
	private List<DomainForm> domains = new ArrayList<>();
	private List<TagForm> tags = new ArrayList<>();
	private String type;
}
