package dk.digitalidentity.re.mvc.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import dk.digitalidentity.re.dao.model.EmailTemplate;
import dk.digitalidentity.re.rest.dto.EmailTemplateDTO;
import dk.digitalidentity.re.security.RequireAdministratorRole;
import dk.digitalidentity.re.service.EmailTemplateService;

@RequireAdministratorRole
@Controller
public class EmailTemplateController {

	@Autowired
	private EmailTemplateService emailTemplateService;
	
	@GetMapping("/mailtemplates")
	public String editTemplate(Model model) {
		List<EmailTemplate> templates = emailTemplateService.findAll();
	
		List<EmailTemplateDTO> templateDTOs = templates.stream()
				.map(t -> EmailTemplateDTO.builder()
						.id(t.getId())
						.message(t.getMessage())
						.title(t.getTitle())
						.templateTypeText(t.getTemplateType().getMessage())
						.emailTemplateType(t.getTemplateType())
						.build())
				.collect(Collectors.toList());

		model.addAttribute("templates", templateDTOs);

		return "emailtemplate/edit";
	}
}