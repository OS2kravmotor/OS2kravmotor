package dk.digitalidentity.re.task;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class KitosStartupJob {

	@Autowired
	private PullDataFromKITOSTask kitosTask;
	
	// the KITOS task is a scheduled task that runs every night - this ensures that it also
	// runs at system startup, so we do not have to wait until night to get the latest data
	@PostConstruct
	public void init() {
		kitosTask.init();
	}
}
