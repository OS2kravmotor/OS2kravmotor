package dk.digitalidentity.re.mvc.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CmsMessageDTO {
	private String key;
	private String name;
	private String value;
}
