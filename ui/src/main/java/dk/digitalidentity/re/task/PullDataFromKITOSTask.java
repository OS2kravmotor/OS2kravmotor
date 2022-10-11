package dk.digitalidentity.re.task;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import dk.digitalidentity.re.dao.model.ItSystem;
import dk.digitalidentity.re.service.ItSystemService;
import dk.digitalidentity.re.service.KitosService;
import dk.digitalidentity.re.service.model.KitosItSystemDTO;
import lombok.extern.log4j.Log4j;

@Log4j
@Component
@EnableScheduling
public class PullDataFromKITOSTask {

	@Value("${scheduled.enabled:false}")
	private boolean runScheduled;

	@Autowired
	private ItSystemService itSystemService;
	
	@Autowired
	private KitosService kitosService;
	
	@Async
	public void init() {
		processPurchases();
	}

	@Scheduled(cron = "0 0 3 * * *")
	public void processPurchases() {
		if (!runScheduled || !kitosService.isConfigured()) {
			log.debug("Scheduling is disabled on this instance");
			return;
		}

		log.info("Running Scheduled Task: " + this.getClass().getName());
		
		List<ItSystem> itSystemsFromKitos = new ArrayList<>();
		try {
			List<KitosItSystemDTO> kitosResponse = kitosService.getItSystemsFromKitos();
			
			for (KitosItSystemDTO itSystemDTO : kitosResponse) {
				ItSystem itSystem = new ItSystem();
				itSystem.setName(itSystemDTO.getName());
				itSystem.setSystemId(itSystemDTO.getId());
				itSystem.setVendor((itSystemDTO.getVendor() != null) ? itSystemDTO.getVendor().getName() : null);
				
				itSystemsFromKitos.add(itSystem);
			}
		}
		catch (Exception ex) {
			log.error("Failed to call KITOS", ex);
			return;
		}

		// Load ALL it-systems from the database (cache actually)
		List<ItSystem> itSystemFromDB = itSystemService.findAll();

		// Iterate over all the ItSystems from KITOS
		for (ItSystem kitosItSystem : itSystemsFromKitos) {
			// For each ItSystem from KITOS, iterate over all the it-systems
			// from the database, and look for one that has a matching systemId
			ItSystem foundItSystem = null;
			for (ItSystem dbItSystem : itSystemFromDB) {
				if (kitosItSystem.getSystemId() == dbItSystem.getSystemId()) {
					foundItSystem = dbItSystem;
					break;
				}
			}

			// If one is found, update the Name/Vendor attributes (if they are
			// different), and call dao.save() on the modified database object
			if (foundItSystem != null) {
				boolean modified = false;
				
				// Name
				if (!kitosItSystem.getName().equals(foundItSystem.getName())) {
					foundItSystem.setName(kitosItSystem.getName());
					modified = true;
				}

				// Vendor (might be null)
				if ((kitosItSystem.getVendor() == null && foundItSystem.getVendor() != null) ||
					(kitosItSystem.getVendor() != null && !kitosItSystem.getVendor().equals(foundItSystem.getVendor()))) {
					foundItSystem.setVendor(kitosItSystem.getVendor());
					modified = true;
				}
				
				if (modified) {
					itSystemService.save(foundItSystem);
				}
			}
			else {
				// If none is found, call dao.save() on the object returned from KITOS,
				// as it is a new it-system and we should create it
				itSystemService.save(kitosItSystem);
			}
			
			// note that we do not delete it-systems.... we might have references to it-systems in our database, so deleting them would
			// be a bad idea, even if they have been deleted from KITOS
		}
		
		itSystemService.clearCache();
	}
}
