package dk.digitalidentity.re.mvc.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import dk.digitalidentity.re.dao.model.CmsMessage;
import dk.digitalidentity.re.dao.model.enums.CMSKey;
import dk.digitalidentity.re.mvc.dto.CmsMessageDTO;
import dk.digitalidentity.re.security.RequireGlobalEditorRole;
import dk.digitalidentity.re.service.CmsMessageService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequireGlobalEditorRole
@Controller
public class CmsMessageController {
	
	@Autowired
	private CmsMessageService cmsMessageService;

	@Autowired
	private MessageSource messageSource;
	
	@GetMapping("/cms/list") 
	public String listCms(Model model) {
		List<CmsMessageDTO> result = new ArrayList<>();
		Map<String, String> cmsContent = cmsMessageService.getCmsMap();
		for (String key : cmsContent.keySet()) {
			CmsMessageDTO dto = new CmsMessageDTO();
			dto.setKey(key);
			dto.setValue(cmsContent.get(key));
			fetchPrettyName(key, dto);
			result.add(dto);
		}
		model.addAttribute("cmsMessages", result);

		return "cms/list";
	}
	
	@GetMapping("/cms/edit")
	public String editCms(Model model, @RequestParam("key") String key) {
		CmsMessage entity = cmsMessageService.getByCmsKey(key);
		if (entity != null) {
			CmsMessageDTO dto = new CmsMessageDTO();
			dto.setKey(entity.getCmsKey());
			dto.setValue(entity.getCmsValue());
			model.addAttribute("cmsMessage", dto);
		} else {
			return "redirect:/cms/list";
		}

		return "cms/edit";
	}
	
	@PostMapping("/cms/edit")
	public String saveCms(Model model, CmsMessageDTO cmsMessageDTO) {
		CmsMessage cms = cmsMessageService.getByCmsKey(cmsMessageDTO.getKey());
		if (cms == null) {
			cms = new CmsMessage();
			cms.setCmsKey(cmsMessageDTO.getKey());
		}
		cms.setCmsValue(cmsMessageDTO.getValue());
		cms.setLastUpdated(LocalDateTime.now());
		cmsMessageService.save(cms);
		return "redirect:/cms/list";
	}


	private void fetchPrettyName(String key, CmsMessageDTO dto) {
		try {
			CMSKey prettyName = CMSKey.valueOf(key);
			dto.setName(messageSource.getMessage(prettyName.getMessage(), null, null));
		} catch (Exception e) {
			log.warn("Missing enum for CMS key: " + key);
		}
	}
}