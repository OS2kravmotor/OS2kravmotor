package dk.digitalidentity.re.mvc.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import dk.digitalidentity.re.dao.model.JIRASprint;
import dk.digitalidentity.re.dao.model.enums.JIRASprintState;
import dk.digitalidentity.re.service.JIRASprintService;

@Controller
public class JiraFeedController {

	@Autowired
	private JIRASprintService jiraSprintService;

	@Value("${jira.browse}")
	private String jiraBrowseUrl;
	
	@GetMapping("/JIRAfeed")
	public String getJIRAfeed(Model model) {
		List<JIRASprint> allSprints = jiraSprintService.findAll();
		List<JIRASprint> activeSprints = allSprints.stream().filter(s -> JIRASprintState.ACTIVE.equals(s.getState())).collect(Collectors.toList());
		List<JIRASprint> closedSprints = allSprints.stream().filter(s -> JIRASprintState.CLOSED.equals(s.getState())).collect(Collectors.toList());

		model.addAttribute("activeSprints", activeSprints);
		model.addAttribute("closedSprints", closedSprints);
		model.addAttribute("browseUrl", jiraBrowseUrl);
		
		return "fragment/JIRAfeed :: content";
	}
}
