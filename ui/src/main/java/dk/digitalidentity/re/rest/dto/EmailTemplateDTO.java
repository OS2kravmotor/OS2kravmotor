package dk.digitalidentity.re.rest.dto;

import dk.digitalidentity.re.dao.model.enums.EmailTemplateType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmailTemplateDTO {
	private long id;
	private String title;
	private String message;
	private String templateTypeText;
	private EmailTemplateType emailTemplateType;
}
