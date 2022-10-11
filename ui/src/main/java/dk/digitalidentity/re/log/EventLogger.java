package dk.digitalidentity.re.log;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import dk.digitalidentity.re.security.SecurityUtil;
import lombok.extern.log4j.Log4j;

@Component
@Log4j
public class EventLogger {

	@Autowired
	private SecurityUtil securityUtil;

	public void log(LogEvent event, String entityType, String entityId) {
		log.info("User=" + SecurityUtil.getUser() + ", MunicipalityCvr=" + SecurityUtil.getMunicipalityCvr()+", CommunityCvr=" + securityUtil.getCvr() + ", Event=" + event + ", EntityType=" + entityType + ", EntityId=" + entityId);
	}
}