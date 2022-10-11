package dk.digitalidentity.re.service.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class KitosWrapperDTO {
	private List<KitosItSystemDTO> value;
}
