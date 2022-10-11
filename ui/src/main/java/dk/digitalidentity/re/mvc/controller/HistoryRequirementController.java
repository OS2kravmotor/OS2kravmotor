package dk.digitalidentity.re.mvc.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import javax.servlet.http.HttpServletResponse;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.history.Revision;
import org.springframework.data.history.Revisions;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.ModelAndView;

import dk.digitalidentity.re.dao.model.ArchitecturePrinciple;
import dk.digitalidentity.re.dao.model.Domain;
import dk.digitalidentity.re.dao.model.Requirement;
import dk.digitalidentity.re.mvc.form.DomainForm;
import dk.digitalidentity.re.mvc.form.PrincipleForm;
import dk.digitalidentity.re.mvc.form.RequirementForm;
import dk.digitalidentity.re.mvc.view.HistoryRequirementXlsView;
import dk.digitalidentity.re.security.RequireEditorRole;
import dk.digitalidentity.re.security.SecurityUtil;
import dk.digitalidentity.re.service.RequirementService;

@RequireEditorRole
@Controller
public class HistoryRequirementController {

	@Autowired
	private RequirementService requirementService;

	@Autowired
	private ModelMapper modelMapper;

	@Autowired
	private MessageSource messageSource;

	@GetMapping("/requirement/history/{id}")
	public String listRequirementHistory(Model model, @PathVariable(name = "id") long id) {
		Revisions<Integer, Requirement> revisions = requirementService.getRevisions(id);
		List<Revision<Integer, Requirement>> result = new ArrayList<>();

		//Filter the ones user has access to
		for (Revision<Integer, Requirement> revision : revisions) {
			if (requirementService.canRead(revision.getEntity())) {
				result.add(revision);
			}
		}

		model.addAttribute("revisions", result);

		return "requirement/history";
	}

	@GetMapping("/requirement/history/{id}/detail/{revId}")
	public String listRequirementHistoryDetail(Model model, @PathVariable(name = "id") long id, @PathVariable(name = "revId") long revId) {
		Revision<Integer, Requirement> revision = requirementService.getRevisions(id).stream().filter(r -> r.getRequiredRevisionNumber() == revId).findAny().orElse(null);
		if (revision == null) {
			return "redirect:/requirement/history/" + id;
		}

		if (!Objects.equals(revision.getEntity().getCvr(), SecurityUtil.getMunicipalityCvr())) {
			return "redirect:/requirement/list";
		}

		Requirement requirement = revision.getEntity();

		RequirementForm requirementForm = modelMapper.map(requirement,RequirementForm.class);

		if (requirement.getCategory() != null) {
			requirementForm.setCategory(requirement.getCategory().getId());
		}

		for (Domain domain : requirement.getDomains()) {
			requirementForm.getDomainsRichObjects().add(new DomainForm(domain.getId(), domain.getName()));
		}

		for (ArchitecturePrinciple principle : requirement.getPrinciples()) {
			requirementForm.getPrinciplesRichObjects().add(new PrincipleForm(principle.getId(), principle.getName()));
		}

		model.addAttribute("requirement",requirementForm);

		return "requirement/history_detail";
	}

	@GetMapping("/requirement/history/{id}/download/{revId}")
	public ModelAndView downloadRequirementsHistory(HttpServletResponse response, Locale loc, @PathVariable(name = "id") long id, @PathVariable(name = "revId") long revId) {
		Revision<Integer, Requirement> revision = requirementService.getRevisions(id).stream().filter(r -> r.getRequiredRevisionNumber() == revId).findAny().orElse(null);
		if (revision == null) {
			return new ModelAndView("redirect:/requirement/history/" + id);
		}

		if (!Objects.equals(revision.getEntity().getCvr(), SecurityUtil.getMunicipalityCvr())) {
			return new ModelAndView("redirect:/requirement/list");
		}

		Map<String, Object> model = new HashMap<>();
		model.put("requirement", revision.getEntity());
		model.put("messagesBundle", messageSource);
		model.put("locale", loc);

		response.setContentType("application/ms-excel");
		response.setHeader("Content-Disposition", "attachment; filename=\"krav.xls\"");

		HistoryRequirementXlsView view = new HistoryRequirementXlsView();
		
		return new ModelAndView(view, model);
	}
}
