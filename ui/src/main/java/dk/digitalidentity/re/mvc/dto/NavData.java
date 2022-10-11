package dk.digitalidentity.re.mvc.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NavData {
	private boolean communityContext;
	private List<CommunityData> communities;
}
