package dk.digitalidentity.re.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import dk.digitalidentity.re.dao.EmailTemplateDao;
import dk.digitalidentity.re.dao.model.EmailTemplate;
import dk.digitalidentity.re.dao.model.enums.EmailTemplateType;
import dk.digitalidentity.re.security.SecurityUtil;

@Service
public class EmailTemplateService {
	public static final String PURCHASE_TITLE = "{markedsdialog}";
	public static final String PURCHASE_ENDTIME = "{nysluttid}";
	public static final String PURCHASE_PREVIOUS_ENDTIME = "{forrigesluttid}";
	public static final String CUSTOM_MESSAGE = "{besked}";
	public static final String PURCHASE_DEADLINE = "{sluttid}";
	
	@Autowired
	private EmailTemplateDao emailTemplateDao;

	public List<EmailTemplate> findAll() {
		List<EmailTemplate> result = new ArrayList<>();
		
		for (EmailTemplateType type : EmailTemplateType.values()) {
			result.add(findByTemplateType(type));
		}
		
		return result;
	}

	public EmailTemplate findByTemplateType(EmailTemplateType type) {
		EmailTemplate template = emailTemplateDao.findByTemplateTypeAndCvr(type, SecurityUtil.getMunicipalityCvr());
		if (template == null) {
			template = new EmailTemplate();
			String title = "Overskrift";
			String message = "Besked";
			
			switch (type) {
				case INVITE_USER:
					title = "Invitation til at besvare krav for markedsdialogen \"{markedsdialog}\"";
					message = "I er blevet inviteret til at besvare krav til markedsdialogen <b>{markedsdialog}</b>. Der er vedlagt følgende besked til invitationen:<br/><br/>";
					break;
				case PURCHASE_POSTPONEMENT:
					title = "Svarfristen for \"{markedsdialog}\" er blevet ændret";
					message = "Svarfristen for markedsdialogen <b>{markedsdialog}</b> er blevet ændret fra <b>{forrigesluttid}</b> til <b>{nysluttid}</b><br/><br/>{besked}";
					break;
				case PURCHASE_CANCELLATION:
					title = "Markedsdialogen \"{markedsdialog}\" er blevet aflyst";
					message = "Kravsbesvarelse af markedsdialogen <b>{markedsdialog}</b> som du tidligere er blevet inviteret til at besvare, er blevet aflyst af.<br/><br/>{besked}";
					break;
				case NOTIFY_VENDOR_ELABORATION:
					title = "Du bedes uddybe dine svar for \"{markedsdialog}\"";
					message = "Kunden ønsker nogle af svarene på kravene for <b>{markedsdialog}</b> uddybet.<br/><br/>{besked}";
					break;
				case NOTIFY_CUSTOMER:
					title = "Leverandøren har uddybet sine svar for \"{markedsdialog}\"";
					message = "Leverandøren har uddybet sine svar for <b>{markedsdialog}</b> som tidligere anmodet";
					break;
			}
			
			template.setTitle(title);
			template.setMessage(message);
			template.setTemplateType(type);
			template.setCvr(SecurityUtil.getMunicipalityCvr());

			template = emailTemplateDao.save(template);
		}
		
		return template;
	}

	public EmailTemplate save(EmailTemplate template) {
		template.setCvr(SecurityUtil.getMunicipalityCvr());

		return emailTemplateDao.save(template);
	}

	public EmailTemplate findById(long id) {
		return emailTemplateDao.findByIdAndCvr(id, SecurityUtil.getMunicipalityCvr());
	}
}
