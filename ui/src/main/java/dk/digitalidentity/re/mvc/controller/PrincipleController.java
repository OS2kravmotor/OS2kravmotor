package dk.digitalidentity.re.mvc.controller;

import dk.digitalidentity.re.dao.PrincipleDao;
import dk.digitalidentity.re.dao.model.ArchitecturePrinciple;
import dk.digitalidentity.re.mvc.dto.DeleteStatus;
import dk.digitalidentity.re.mvc.form.PrincipleForm;
import dk.digitalidentity.re.mvc.validator.PrincipleFormValidator;
import dk.digitalidentity.re.security.RequireEditorRole;
import dk.digitalidentity.re.security.RequireGlobalEditorRole;
import dk.digitalidentity.re.service.RequirementService;

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

import javax.validation.Valid;
import java.util.List;

@Controller
@RequireEditorRole
public class PrincipleController {
	private static final Logger log = Logger.getLogger(PrincipleController.class);

	@Autowired
	private ModelMapper modelMapper;

	@Autowired
	private PrincipleDao principleDao;

	@Autowired
	private PrincipleFormValidator principleFormValidator;

	@Autowired
	private RequirementService requirementService;

	@InitBinder
	public void initBinder(WebDataBinder binder) {
		binder.addValidators(principleFormValidator);
	}

	@RequestMapping(path = {"principle/", "principle/list"}, method = RequestMethod.GET)
	public String listPrinciple(Model model) {
		List<PrincipleForm> principles = modelMapper.map(principleDao.findAll(), new TypeToken<List<PrincipleForm>>() { }.getType());

		model.addAttribute("principles", principles);
		model.addAttribute("principle", new PrincipleForm());
		model.addAttribute("principleEdit", new PrincipleForm());

		return "principle/list";
	}

	@RequireGlobalEditorRole
	@RequestMapping(path = {"principle/"}, method = RequestMethod.POST)
	public String newPrinciple(Model model, @ModelAttribute("principle") @Valid PrincipleForm principleForm, BindingResult bindingResult) {
		if (bindingResult.hasErrors()) {
			model.addAttribute(bindingResult.getAllErrors());
			model.addAttribute("principle", principleForm);
			model.addAttribute("principleEdit", new PrincipleForm());
			model.addAttribute("principles", modelMapper.map(principleDao.findAll(), new TypeToken<List<PrincipleForm>>() { }.getType()));

			return "principle/list";
		}

		List<PrincipleForm> principles = modelMapper.map(principleDao.findAll(), new TypeToken<List<PrincipleForm>>() { }.getType());

		ArchitecturePrinciple newPrinciple = modelMapper.map(principleForm, ArchitecturePrinciple.class);
		principleDao.save(newPrinciple);

		model.addAttribute("principles", principles);

		return "redirect:../principle/";
	}
	
	@RequireGlobalEditorRole
	@RequestMapping(path = { "principle/edit" }, method = RequestMethod.POST)
	public String editPrinciple(Model model, @ModelAttribute("principleEdit") @Valid PrincipleForm principleForm, BindingResult bindingResult) {
		if (bindingResult.hasErrors()) {
			model.addAttribute(bindingResult.getAllErrors());
			model.addAttribute("state", "edit");
			model.addAttribute("principleEdit", principleForm);
			model.addAttribute("principle", new PrincipleForm());
			model.addAttribute("principles", modelMapper.map(principleDao.findAll(), new TypeToken<List<PrincipleForm>>() {}.getType()));

			return "principle/list";
		}
		
		ArchitecturePrinciple principle = principleDao.getById(principleForm.getId());
		if (principle == null) {
			log.warn("Requested principle with ID:" + principleForm.getId() + " not found");
		} else {
			principle.setName(principleForm.getName());
			principle.setReference(principleForm.getReference());
			principleDao.save(principle);
		}

		return "redirect:/principle/";
	}

	@RequireGlobalEditorRole
	@RequestMapping(value = "principle/trydelete/{id}", method = RequestMethod.GET)
	public ResponseEntity<DeleteStatus> tryDelete(@PathVariable("id") long id) {
		ArchitecturePrinciple principle = principleDao.getById(id);
		if (principle == null) {
			log.warn("Cannot find principle with id = " + id);
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

		DeleteStatus status = new DeleteStatus();

		long requirementsQuantity = requirementService.countByPrinciplesContainsAndDeletedFalse(principle);

		if (requirementsQuantity < 1) {
			status.setSuccess(true);
			return new ResponseEntity<>(status, HttpStatus.OK);
		}

		status.setRequirementQuantity(requirementsQuantity);
		status.setSuccess(false);

		return new ResponseEntity<>(status, HttpStatus.OK);
	}

	@RequireGlobalEditorRole
	@RequestMapping(path = "principle/{id}", method = RequestMethod.DELETE)
	@ResponseBody
	public ResponseEntity<String> deletePrinciple(@PathVariable("id") long id) {
		ArchitecturePrinciple principle = principleDao.getById(id);

		if (principle == null) {
			log.warn("Requested Principle with ID:"+id+ " not found.");
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

		principleDao.delete(principle);
		return new ResponseEntity<>(HttpStatus.OK);
	}
}
