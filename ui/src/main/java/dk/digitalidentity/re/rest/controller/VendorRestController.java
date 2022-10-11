package dk.digitalidentity.re.rest.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import dk.digitalidentity.re.dao.model.EmailTemplate;
import dk.digitalidentity.re.dao.model.PurchaseAnswer;
import dk.digitalidentity.re.dao.model.enums.EmailTemplateType;
import dk.digitalidentity.re.mvc.controller.PurchaseController;
import dk.digitalidentity.re.service.EmailService;
import dk.digitalidentity.re.service.EmailTemplateService;
import dk.digitalidentity.re.service.VendorOrganizationService;

@RestController
public class VendorRestController {
	private static final Logger log = LoggerFactory.getLogger(PurchaseController.class);

	@Value("${email.sender}")
	private String senderEmailAddress;
	
	@Autowired
	VendorOrganizationService vendorOrganizationService;
	
	@Autowired
	EmailService emailService;
	
	@Autowired
	EmailTemplateService emailTemplateService;
	
	@PostMapping(value = "rest/vendor/purchaseanswer/{id}/customer/send")
	@ResponseBody
	public ResponseEntity<String> purchaseAnswerSendVendorMail(@PathVariable Long id) {
		PurchaseAnswer answer = vendorOrganizationService.getPurchaseAnswerById(id);
		if (answer == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		
		answer.setVendorMustElaborate(false);
		answer.setDoneAnswering(true);
		vendorOrganizationService.savePurchaseAnswer(answer);
		
		EmailTemplate template = emailTemplateService.findByTemplateType(EmailTemplateType.NOTIFY_CUSTOMER);
		
		String emailSubject = template.getTitle();
		emailSubject = emailSubject.replace(EmailTemplateService.PURCHASE_TITLE, answer.getPurchase().getTitle());
		
		String emailBody;
		emailBody = template.getMessage();
		emailBody = emailBody.replace(EmailTemplateService.PURCHASE_TITLE, answer.getPurchase().getTitle());
		
		try {
			emailService.sendMessage(senderEmailAddress, answer.getPurchase().getEmail(), emailSubject, emailBody);
		}
		catch (Exception ex) {
			log.warn("Error occured while trying to send email", ex);
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		
		return new ResponseEntity<>(HttpStatus.OK);
	}
}
