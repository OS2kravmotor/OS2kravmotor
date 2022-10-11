package dk.digitalidentity.re.task;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.LocaleUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import dk.digitalidentity.re.dao.PurchaseDao;
import dk.digitalidentity.re.dao.model.Purchase;
import dk.digitalidentity.re.dao.model.enums.Status;
import dk.digitalidentity.re.service.MailSenderService;

@Component
@EnableScheduling
public class CompletePurchaseTask {
	private static final Logger log = LoggerFactory.getLogger(CompletePurchaseTask.class);

	@Value("${email.sender}")
	private String senderEmailAddress;

	@Value("${app.locale:en_US}")
	private String localeString;
	
	@Value("${scheduled.enabled:false}")
	private boolean runScheduled;

	@Autowired
	private PurchaseDao purchaseDao;

	@Autowired
	private MessageSource messageSource;

	@Autowired
	private MailSenderService emailService;

	@Scheduled(fixedRate = 15 * 60 * 1000)
	public void processPurchases() {
		if (!runScheduled) {
			log.debug("Scheduling is disabled on this instance");
			return;
		}
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd - HH:mm");
		Locale locale = LocaleUtils.toLocale(localeString.replace('-', '_'));
		List<Purchase> activePurchases = purchaseDao.findByStatus(Status.ACTIVE);

		for (Purchase purchase : activePurchases) {
			if (purchase.getEndTime() != null && purchase.getEndTime().before(new Date())) {
				log.info("Purchase:" + purchase.getTitle() + " has reached its deadline. Changing status to COMPLETE.");

				String subject = messageSource.getMessage("email.complete.task.title", new String[] { purchase.getTitle() }, locale);
				String body = messageSource.getMessage("email.complete.task.body", new String[] { purchase.getTitle(), dateFormat.format(purchase.getEndTime()) }, locale);

				try {
					emailService.sendMessage(senderEmailAddress, purchase.getEmail(), subject, body);
				}
				catch (Exception ex) {
					log.warn("Error occured while trying to send email. ", ex);
				}

				purchase.setStatus(Status.COMPLETED);
				purchaseDao.save(purchase);
			}
		}
	}
}
