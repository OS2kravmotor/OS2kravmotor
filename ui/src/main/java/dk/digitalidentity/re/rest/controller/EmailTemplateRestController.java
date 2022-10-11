package dk.digitalidentity.re.rest.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

import org.htmlcleaner.BrowserCompactXmlSerializer;
import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import dk.digitalidentity.re.dao.model.EmailTemplate;
import dk.digitalidentity.re.rest.dto.EmailTemplateDTO;
import dk.digitalidentity.re.security.RequireAdministratorRole;
import dk.digitalidentity.re.service.EmailTemplateService;
import lombok.extern.log4j.Log4j;

@Log4j
@RestController
@RequireAdministratorRole
public class EmailTemplateRestController {

	@Autowired
	private EmailTemplateService emailTemplateService;

	@PostMapping(value = "/rest/mailtemplates")
	@ResponseBody
	public ResponseEntity<String> updateTemplate(@RequestBody EmailTemplateDTO emailTemplateDTO) {
		toXHTML(emailTemplateDTO);

		EmailTemplate template = emailTemplateService.findById(emailTemplateDTO.getId());
		if (template == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}

		template.setMessage(emailTemplateDTO.getMessage());
		template.setTitle(emailTemplateDTO.getTitle());
		emailTemplateService.save(template);

		return new ResponseEntity<>(HttpStatus.OK);
	}

	/**
	 * summernote does not generate valid XHTML. At least the <br/>
	 * and <img/> tags are not closed, so we need to close them, otherwise our PDF
	 * processing will fail.
	 */
	private void toXHTML(EmailTemplateDTO emailTemplateDTO) {
		String message = emailTemplateDTO.getMessage();
		if (message != null) {
			try {
				CleanerProperties properties = new CleanerProperties();
				properties.setOmitXmlDeclaration(true);
				TagNode tagNode = new HtmlCleaner(properties).clean(message);

				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				new BrowserCompactXmlSerializer(properties).writeToStream(tagNode, bos);

				emailTemplateDTO.setMessage(new String(bos.toByteArray(), Charset.forName("UTF-8")));
			} catch (IOException ex) {
				log.error("could not parse: " + emailTemplateDTO.getMessage());
			}
		}
	}
}
