package dk.digitalidentity.re.rest.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import dk.digitalidentity.re.dao.model.EmailTemplate;
import dk.digitalidentity.re.dao.model.PurchaseAnswer;
import dk.digitalidentity.re.dao.model.PurchaseRequirementAnswer;
import dk.digitalidentity.re.dao.model.VendorUser;
import dk.digitalidentity.re.dao.model.enums.EmailTemplateType;
import dk.digitalidentity.re.mvc.controller.PurchaseController;
import dk.digitalidentity.re.rest.dto.PurchaseRequirementAnswerCommentDTO;
import dk.digitalidentity.re.service.EmailService;
import dk.digitalidentity.re.service.EmailTemplateService;
import dk.digitalidentity.re.service.PurchaseRequirementService;
import dk.digitalidentity.re.service.PurchaseService;

@RestController
public class PurchaseRestController {
	private static final Logger log = LoggerFactory.getLogger(PurchaseController.class);

	@Value("${email.sender}")
	private String senderEmailAddress;
	
	@Autowired
	PurchaseRequirementService purchaseRequirementService;
	
	@Autowired
	PurchaseService purchaseService;
	
	@Autowired
	EmailService emailService;
	
	@Autowired
	EmailTemplateService emailTemplateService;

	@PostMapping(value = "/rest/purchase/purchaserequirementanswer/{id}/comment")
	@ResponseBody
	public ResponseEntity<String> purchaseRequirementAnswerComment(@PathVariable Long id, @RequestBody PurchaseRequirementAnswerCommentDTO purchaseRequirementAnswerCommentDTO) {
		PurchaseRequirementAnswer answer = purchaseRequirementService.getPurchaseRequirementAnswerById(id);
		if (answer == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		
		answer.setCustomerAnswer(purchaseRequirementAnswerCommentDTO.getStatus());
		answer.setCustomerComment(purchaseRequirementAnswerCommentDTO.getComment());
		purchaseRequirementService.savePurchaseRequirementAnswer(answer);
		
		return new ResponseEntity<>(HttpStatus.OK);
	}
	
	@PostMapping(value = "rest/purchase/purchaseanswer/{id}/vendor/send")
	public ResponseEntity<String> purchaseAnswerSendVendorMail(@PathVariable Long id, @RequestBody String message) {
		PurchaseAnswer answer = purchaseService.getPurchaseAnswer(id);
		if (answer == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		
		answer.setVendorMustElaborate(true);
		answer.setDoneAnswering(false);
		purchaseService.savePurchaseAnswer(answer);
		
		EmailTemplate template = emailTemplateService.findByTemplateType(EmailTemplateType.NOTIFY_VENDOR_ELABORATION);
		
		String emailSubject = template.getTitle();
		emailSubject = emailSubject.replace(EmailTemplateService.PURCHASE_TITLE, answer.getPurchase().getTitle());
		emailSubject = emailSubject.replace(EmailTemplateService.CUSTOM_MESSAGE, message);
		
		String emailBody;
		emailBody = template.getMessage();
		emailBody = emailBody.replace(EmailTemplateService.PURCHASE_TITLE, answer.getPurchase().getTitle());
		
		for (VendorUser vendorUser : answer.getVendorUsers()) {
			try {
				emailService.sendMessage(senderEmailAddress, vendorUser.getEmail(), emailSubject, emailBody);
			}
			catch (Exception ex) {
				log.warn("Error occured while trying to send email", ex);
			}
		}
		
		return new ResponseEntity<>(HttpStatus.OK);
	}
}
