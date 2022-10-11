package dk.digitalidentity.re.mvc.controller;

import java.util.List;

import javax.validation.Valid;

import org.apache.log4j.Logger;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import dk.digitalidentity.re.dao.DomainDao;
import dk.digitalidentity.re.dao.PurchaseDao;
import dk.digitalidentity.re.dao.model.Domain;
import dk.digitalidentity.re.mvc.dto.DeleteStatus;
import dk.digitalidentity.re.mvc.form.DomainForm;
import dk.digitalidentity.re.mvc.validator.DomainFormValidator;
import dk.digitalidentity.re.security.RequireEditorRole;
import dk.digitalidentity.re.security.RequireGlobalEditorRole;
import dk.digitalidentity.re.service.RequirementService;

@Controller
@RequireEditorRole
public class DomainController {
	private static final Logger log = Logger.getLogger(DomainController.class);

	@Autowired
	private ModelMapper modelMapper;

	@Autowired
	private RequirementService requirementService;
	
	@Autowired
	private PurchaseDao purchaseDao;

	@Autowired
	private DomainDao domainDao;

	@Autowired
	private DomainFormValidator domainFormValidator;

	@InitBinder
	public void initBinder(WebDataBinder binder) {
		binder.addValidators(domainFormValidator);
	}

	@RequestMapping(path = { "domain/", "domain/list" }, method = RequestMethod.GET)
	public String listDomain(Model model) {
		List<DomainForm> domains = modelMapper.map(domainDao.findAll(), new TypeToken<List<DomainForm>>() {
		}.getType());

		model.addAttribute("domains", domains);
		model.addAttribute("domain", new DomainForm());
		model.addAttribute("domainEdit", new DomainForm());

		return "domain/list";
	}

	@RequireGlobalEditorRole
	@RequestMapping(path = { "domain/" }, method = RequestMethod.POST)
	public String newDomain(Model model, @ModelAttribute("domain") @Valid DomainForm domain, BindingResult bindingResult) {
		if (bindingResult.hasErrors()) {
			model.addAttribute(bindingResult.getAllErrors());
			model.addAttribute("domain", domain);
			model.addAttribute("domainEdit", new DomainForm());
			model.addAttribute("domains", modelMapper.map(domainDao.findAll(), new TypeToken<List<DomainForm>>() { }.getType()));

			return "domain/list";
		}

		List<DomainForm> domains = modelMapper.map(domainDao.findAll(), new TypeToken<List<DomainForm>>() {
		}.getType());
		Domain newDomain = modelMapper.map(domain, Domain.class);
		domainDao.save(newDomain);

		model.addAttribute("domains", domains);

		return "redirect:../domain/";
	}

	@RequireGlobalEditorRole
	@RequestMapping(path = { "domain/edit" }, method = RequestMethod.POST)
	public String editDomain(Model model, @ModelAttribute("domainEdit") @Valid DomainForm domainForm, BindingResult bindingResult) {
		Domain domain = domainDao.getById(domainForm.getId());
		if (domain == null) {
			log.warn("Requested Domain with ID:" + domainForm.getId() + " not found");
	                return "redirect:/domain/";
		}

		if (bindingResult.hasErrors()) {
			model.addAttribute(bindingResult.getAllErrors());
			model.addAttribute("state", "edit");
			model.addAttribute("domainEdit", domainForm);
			model.addAttribute("domain", new DomainForm());
			model.addAttribute("domains", modelMapper.map(domainDao.findAll(), new TypeToken<List<DomainForm>>() {}.getType()));

			return "domain/list";
		}

		domain.setName(domainForm.getName());
		domainDao.save(domain);

		return "redirect:/domain/";
	}

	@RequireGlobalEditorRole
	@RequestMapping(value = "domain/trydelete/{id}", method = RequestMethod.GET)
	public ResponseEntity<DeleteStatus> tryDelete(@PathVariable("id") long id) {
		Domain domain = domainDao.getById(id);
		if (domain == null) {
			log.warn("Cannot find domain with id = " + id);
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

		DeleteStatus status = new DeleteStatus();

		long requirementsQuantity = requirementService.countByDomainsContainsAndDeletedFalse(domain);
		long purchaseQuantity = purchaseDao.countByDomainsContains(domain);

		if (requirementsQuantity < 1  && purchaseQuantity < 1) {
			status.setSuccess(true);

			return new ResponseEntity<>(status, HttpStatus.OK);
		}

		if (requirementsQuantity > 0) {
			status.setRequirementQuantity(requirementsQuantity);
		}

		if (purchaseQuantity > 0) {
			status.setPurchaseQuantity(purchaseQuantity);
		}

		status.setSuccess(false);
		
		return new ResponseEntity<>(status, HttpStatus.OK);
	}

	@RequireGlobalEditorRole
	@RequestMapping(path = "domain/{id}", method = RequestMethod.DELETE)
	@ResponseBody
	public ResponseEntity<String> deleteDomain(@PathVariable("id") long id) {
		Domain domain = domainDao.getById(id);

		if (domain == null) {
			log.warn("Cannot delete domain with id = " + id);
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

		domainDao.delete(domain);

		return new ResponseEntity<>(HttpStatus.OK);
	}
}
