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

import dk.digitalidentity.re.dao.TagDao;
import dk.digitalidentity.re.dao.model.Tag;
import dk.digitalidentity.re.mvc.dto.DeleteStatus;
import dk.digitalidentity.re.mvc.form.TagForm;
import dk.digitalidentity.re.mvc.validator.TagFormValidator;
import dk.digitalidentity.re.security.RequireEditorRole;
import dk.digitalidentity.re.security.RequireGlobalEditorRole;
import dk.digitalidentity.re.service.RequirementService;

@Controller
@RequireEditorRole
public class TagController {
	private static final Logger log = Logger.getLogger(TagController.class);

	@Autowired
	private ModelMapper modelMapper;

	@Autowired
	private TagDao tagDao;

	@Autowired
	private TagFormValidator tagFormValidator;

	@Autowired
	private RequirementService requirementService;

	@InitBinder
	public void initBinder(WebDataBinder binder) {
		binder.addValidators(tagFormValidator);
	}

	@RequestMapping(path = {"tag/", "tag/list"}, method = RequestMethod.GET)
	public String listTag(Model model) {
		List<TagForm> tags = modelMapper.map(tagDao.findAll(), new TypeToken<List<TagForm>>() { }.getType());

		model.addAttribute("tags", tags);
		model.addAttribute("tag", new TagForm());
		model.addAttribute("tagEdit", new TagForm());

		return "tag/list";
	}

	@RequireGlobalEditorRole
	@RequestMapping(path = {"tag/"}, method = RequestMethod.POST)
	public String newTag(Model model, @ModelAttribute("tag") @Valid TagForm tagForm, BindingResult bindingResult) {
		if (bindingResult.hasErrors()) {
			model.addAttribute(bindingResult.getAllErrors());
			model.addAttribute("tag", tagForm);
			model.addAttribute("tagEdit", new TagForm());
			model.addAttribute("tags", modelMapper.map(tagDao.findAll(), new TypeToken<List<TagForm>>() { }.getType()));

			return "tag/list";
		}

		List<TagForm> tags = modelMapper.map(tagDao.findAll(), new TypeToken<List<TagForm>>() { }.getType());

		Tag newTag = modelMapper.map(tagForm, Tag.class);
		tagDao.save(newTag);

		model.addAttribute("tags", tags);

		return "redirect:../tag/";
	}
	
	@RequireGlobalEditorRole
	@RequestMapping(path = { "tag/edit" }, method = RequestMethod.POST)
	public String editTag(Model model, @ModelAttribute("tagEdit") @Valid TagForm tagForm, BindingResult bindingResult) {
		if (bindingResult.hasErrors()) {
			model.addAttribute(bindingResult.getAllErrors());
			model.addAttribute("state", "edit");
			model.addAttribute("tagEdit", tagForm);
			model.addAttribute("tag", new TagForm());
			model.addAttribute("tags", modelMapper.map(tagDao.findAll(), new TypeToken<List<TagForm>>() {}.getType()));

			return "tag/list";
		}
		
		Tag tag = tagDao.getById(tagForm.getId());
		if (tag == null) {
			log.warn("Requested tag with ID:" + tagForm.getId() + " not found");
		} else {
			tag.setName(tagForm.getName());
			tag.setQuestion(tagForm.getQuestion());
			tagDao.save(tag);
		}

		return "redirect:/tag/";
	}

	@RequireGlobalEditorRole
	@RequestMapping(value = "tag/trydelete/{id}", method = RequestMethod.GET)
	public ResponseEntity<DeleteStatus> tryDelete(@PathVariable("id") long id) {
		Tag tag = tagDao.getById(id);
		if (tag == null) {
			log.warn("Cannot find tag with id = " + id);
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

		DeleteStatus status = new DeleteStatus();

		long requirementsQuantity = requirementService.countByTagsContainsAndDeletedFalse(tag);
		if (requirementsQuantity > 0) {
			status.setRequirementQuantity(requirementsQuantity);
			status.setSuccess(false);
		}
		else {
			status.setSuccess(true);			
		}

		return new ResponseEntity<>(status, HttpStatus.OK);
	}

	@RequireGlobalEditorRole
	@RequestMapping(path = "tag/{id}", method = RequestMethod.DELETE)
	@ResponseBody
	public ResponseEntity<String> deleteTag(@PathVariable("id") long id) {
		Tag tag = tagDao.getById(id);

		if (tag == null) {
			log.warn("Requested Tag with ID:"+id+ " not found.");
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

		tagDao.delete(tag);
		return new ResponseEntity<>(HttpStatus.OK);
	}
}
