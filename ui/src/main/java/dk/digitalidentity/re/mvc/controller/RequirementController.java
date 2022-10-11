package dk.digitalidentity.re.mvc.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import dk.digitalidentity.re.Constants;
import dk.digitalidentity.re.dao.CategoryDao;
import dk.digitalidentity.re.dao.DomainDao;
import dk.digitalidentity.re.dao.GlobalEditorDao;
import dk.digitalidentity.re.dao.PrincipleDao;
import dk.digitalidentity.re.dao.TagDao;
import dk.digitalidentity.re.dao.model.Attachment;
import dk.digitalidentity.re.dao.model.Category;
import dk.digitalidentity.re.dao.model.Community;
import dk.digitalidentity.re.dao.model.Domain;
import dk.digitalidentity.re.dao.model.GlobalEditor;
import dk.digitalidentity.re.dao.model.Requirement;
import dk.digitalidentity.re.dao.model.RequirementExtension;
import dk.digitalidentity.re.dao.model.Subcategory;
import dk.digitalidentity.re.dao.model.Tag;
import dk.digitalidentity.re.dao.model.enums.RequirementType;
import dk.digitalidentity.re.mvc.dto.AttachmentDTO;
import dk.digitalidentity.re.mvc.dto.PromoteRequestDTO;
import dk.digitalidentity.re.mvc.dto.RequirementTreeDTO;
import dk.digitalidentity.re.mvc.form.CategoryForm;
import dk.digitalidentity.re.mvc.form.DomainForm;
import dk.digitalidentity.re.mvc.form.PrincipleForm;
import dk.digitalidentity.re.mvc.form.RequirementForm;
import dk.digitalidentity.re.mvc.form.TagForm;
import dk.digitalidentity.re.mvc.validator.RequirementFormValidator;
import dk.digitalidentity.re.security.RequireEditorRole;
import dk.digitalidentity.re.security.RequireGlobalEditorRole;
import dk.digitalidentity.re.security.RequireLocalEditorRole;
import dk.digitalidentity.re.security.SecurityUtil;
import dk.digitalidentity.re.service.CommunityService;
import dk.digitalidentity.re.service.EmailService;
import dk.digitalidentity.re.service.RequirementService;
import dk.digitalidentity.re.service.S3Service;

@RequireEditorRole
@Controller
public class RequirementController {
	private static final Logger log = LoggerFactory.getLogger(RequirementController.class);

	@Value("${email.sender}")
	private String senderEmailAddress;

	@Autowired
	private ModelMapper modelMapper;

	@Autowired
	private CategoryDao categoryDao;

	@Autowired
	private TagDao tagDao;

	@Autowired
	private DomainDao domainDao;

	@Autowired
	private PrincipleDao principleDao;

	@Autowired
	private RequirementFormValidator requirementFormValidator;

	@Autowired
	private RequirementService requirementService;

	@Autowired
	private CommunityService communityService;

	@Autowired
	private S3Service s3service;

	@Autowired
	private GlobalEditorDao globalEditorDao;

	@Autowired
	private EmailService emailService;

	@Autowired
	private MessageSource messageSource;
	
	@Autowired
	private SecurityUtil securityUtil;

	@InitBinder("requirement")
	public void initBinder(WebDataBinder binder) {
		binder.addValidators(requirementFormValidator);
		binder.setBindEmptyMultipartFiles(false);
	}

	@RequireLocalEditorRole
	@RequestMapping(path = "requirement/new", method = RequestMethod.GET)
	public String newRequirement(Model model) {
		RequirementForm newRequirementForm = new RequirementForm();
		newRequirementForm.setRelevantForOnPremise(true);
		newRequirementForm.setRelevantForSaas(true);

		model.addAttribute("requirement", newRequirementForm);
		model.addAttribute("categories",  modelMapper.map(categoryDao.findAll(), new TypeToken<List<CategoryForm>>() {}.getType()));
		model.addAttribute("domains", modelMapper.map(domainDao.findAll(),new TypeToken<List<DomainForm>>() {}.getType()));
		model.addAttribute("tags", modelMapper.map(tagDao.findAll(),new TypeToken<List<TagForm>>() {}.getType()));
		model.addAttribute("principles", modelMapper.map(principleDao.findAll(),new TypeToken<List<PrincipleForm>>() {}.getType()));

		return "requirement/new";
	}

	@RequireLocalEditorRole
	@RequestMapping(path = "requirement/new", method = RequestMethod.POST)
	public String saveNewRequirement(Model model, @Valid @ModelAttribute("requirement") RequirementForm requirement, BindingResult bindingResult) {
		if (bindingResult.hasErrors()) {
			requirement.getFiles().clear();
			model.addAttribute(bindingResult.getAllErrors());
			model.addAttribute("requirement", requirement);
			model.addAttribute("categories",  modelMapper.map(categoryDao.findAll(), new TypeToken<List<CategoryForm>>() {}.getType()));
			model.addAttribute("domains", modelMapper.map(domainDao.findAll(),new TypeToken<List<DomainForm>>() {}.getType()));
			model.addAttribute("tags", modelMapper.map(tagDao.findAll(),new TypeToken<List<TagForm>>() {}.getType()));
			model.addAttribute("principles", modelMapper.map(principleDao.findAll(),new TypeToken<List<PrincipleForm>>() {}.getType()));

			return "requirement/new";
		}
		
		Requirement requirementToSave = modelMapper.map(requirement, Requirement.class );
		requirementToSave.getDomains().clear();
		requirementToSave.getTags().clear();
		requirementToSave.getPrinciples().clear();

		requirement.getDomains().stream().forEach(domain -> requirementToSave.getDomains().add(domainDao.getById(domain)));
		requirement.getTags().stream().forEach(tagId -> requirementToSave.getTags().add(tagDao.getById(Long.parseLong(tagId))));
		requirement.getPrinciples().stream().forEach(principle -> requirementToSave.getPrinciples().add(principleDao.getById(principle)));

		Category category = categoryDao.getById(requirement.getCategory());
		requirementToSave.setCategory(category);
		
		Subcategory subcategory = category.getSubcategories().stream().filter(s -> s.getId() == requirement.getSubcategory()).findAny().orElse(null);
		requirementToSave.setSubcategory(subcategory);
		
		List<Attachment> attachments = new ArrayList<>();
		
		for (MultipartFile file : requirement.getFiles()) {
			if (file.getOriginalFilename().isEmpty()) {
				continue;
			}

			try {
				String s3ObjectUrl = s3service.writeFile(file.getInputStream(), file.getOriginalFilename());
				if (s3ObjectUrl != null) {
					Attachment attachment = new Attachment();
					attachment.setName(file.getOriginalFilename());
					attachment.setRequirement(requirementToSave);
					attachment.setUrl(s3ObjectUrl);
					attachments.add(attachment);
				}
			} catch (IOException e) {
				log.error("Error occured while uploading file to S3. ", e);
				bindingResult.rejectValue("files", "requirement.files", "Unable to upload file. Try again.");
				requirement.setFiles(requirement.getFiles().stream().filter(f->!f.getOriginalFilename().equals(file.getOriginalFilename())).collect(Collectors.toList()));
				model.addAttribute(bindingResult.getAllErrors());
				
				return "requirement/new";
			}
		}
		
		requirementToSave.setAttachments(attachments);

		requirementService.save(requirementToSave);
		
		return "redirect:../requirement/list";
	}

	@RequestMapping(path = "requirement/view/{id}", method = RequestMethod.GET)
	public String viewRequirement(Model model, @ModelAttribute @PathVariable("id") long id){
		Requirement requirement = requirementService.getById(id);
		if (requirement == null) {
			log.warn("Requested Requirement with ID:" + id + " not found.");
			return "redirect:../../requirement/list";
		}

		RequirementForm requirementForm = modelMapper.map(requirement,RequirementForm.class);
		
		// if the requirement is a global requirement,
		// pull the helpText from the local extension instead
		if (requirementForm.getCvr().equals(Constants.DEFAULT_CVR)) {
			RequirementExtension extension = requirementService.getRequirementExtension(requirement);
			if (extension != null) {
				requirementForm.setHelpText(extension.getHelpText());
			}
			else {
				requirementForm.setHelpText(null);
			}
		}

		//As above but for interestedParty
		if (requirementForm.getCvr().equals(Constants.DEFAULT_CVR)) {
			RequirementExtension extension = requirementService.getRequirementExtension(requirement);
			if (extension != null) {
				requirementForm.setInterestedParty(extension.getInterestedParty());
			}
			else {
				requirementForm.setInterestedParty(null);
			}
		}

		RequirementExtension extension = requirementService.getRequirementExtension(requirement);
		if (extension != null) {
			requirementForm.setExtDescription(extension.getDescription());
			requirementForm.setLocalAttachments(extension.getAttachments());
			requirementForm.setFavorite(extension.isFavorite());
			requirementForm.setExtDisable(extension.isDisableRequirement());
			requirementForm.setExtDisableReason(extension.getDisableRequirementReason());
		}

		requirementForm.setCategory(requirement.getCategory().getId());
		requirementForm.getDomains().stream().forEach(
				domainId -> requirementForm.getDomainsRichObjects().add(
						new DomainForm(domainId, domainDao.getById(domainId).getName()))
		);
		requirementForm.getPrinciples().stream().forEach(
				principleId -> requirementForm.getPrinciplesRichObjects().add(
						new PrincipleForm(principleId, principleDao.getById(principleId).getName()))
		);
		
		requirementForm.setSubcategoryName(requirement.getSubcategory() == null ? null : requirement.getSubcategory().getName());
		
		model.addAttribute("requirement",requirementForm);

		return "requirement/view";
	}

	@RequestMapping(path = "requirement/edit/{id}", method = RequestMethod.GET)
	public String editRequirement(Model model, @ModelAttribute @PathVariable("id") long id) {
		Requirement requirement = requirementService.getById(id);
		if (requirement == null) {
			log.warn("Requested Requirement with ID:" + id + " not found.");
			return "redirect:../../requirement/list";
		}
		
		RequirementForm requirementForm = modelMapper.map(requirement, RequirementForm.class);
		requirementForm.getDomains().stream().forEach(domain -> requirementForm.getDomainsRichObjects().add(modelMapper.map(domainDao.getById(domain), DomainForm.class)));
		requirementForm.getTags().stream().forEach(tagId -> requirementForm.getTagsRichObjects().add(modelMapper.map(tagDao.getById(Long.parseLong(tagId)), TagForm.class)));
		requirementForm.getPrinciples().stream().forEach(principle -> requirementForm.getPrinciplesRichObjects().add(modelMapper.map(principleDao.getById(principle), PrincipleForm.class)));
		requirementForm.setShared(requirement.isShared());

		RequirementExtension extension = requirementService.getRequirementExtension(requirement);
		if (extension != null) {
			requirementForm.setExtDescription(extension.getDescription());
			requirementForm.setLocalAttachments(extension.getAttachments());
			requirementForm.setFavorite(extension.isFavorite());
			requirementForm.setExtDisable(extension.isDisableRequirement());
			requirementForm.setExtDisableReason(extension.getDisableRequirementReason());
		}
		
		// if the requirement is a global requirement,
		// pull the helpText from the local extension instead
		if (requirementForm.getCvr().equals(Constants.DEFAULT_CVR)) {
			if (extension != null) {
				requirementForm.setHelpText(extension.getHelpText());
			}
			else {
				requirementForm.setHelpText(null);
			}
		}

		if (requirementForm.getCvr().equals(Constants.DEFAULT_CVR)) {
			if (extension != null) {
				requirementForm.setInterestedParty(extension.getInterestedParty());
			}
			else {
				requirementForm.setInterestedParty(null);
			}
		}
		
		requirementForm.setSubcategory(requirement.getSubcategory() == null ? -1 : requirement.getSubcategory().getId());

		model.addAttribute("requirement", requirementForm);
		model.addAttribute("categories",  modelMapper.map(categoryDao.findAll(), new TypeToken<List<CategoryForm>>() {}.getType()));
		model.addAttribute("alldomains", modelMapper.map(domainDao.findAll(),new TypeToken<List<DomainForm>>() {}.getType()));
		model.addAttribute("alltags", modelMapper.map(tagDao.findAll(),new TypeToken<List<TagForm>>() {}.getType()));
		model.addAttribute("allprinciples" , modelMapper.map(principleDao.findAll(),new TypeToken<List<PrincipleForm>>() {}.getType()));

		return "requirement/edit";
	}

	@RequestMapping(path = "requirement/edit", method = RequestMethod.POST)
	public String saveEditRequirement(Model model, @Valid @ModelAttribute("requirement") RequirementForm requirement, BindingResult bindingResult) {
		Requirement requirementToSave = requirementService.getById(requirement.getId());
		if (requirementToSave == null) {
			log.warn("Requested Requirement with ID:" + requirement.getId() + " not found.");
			return "redirect:../requirement/list";
		}
		
		// always update local extensions on global requirements
		if (requirementToSave.getCvr().equals(Constants.DEFAULT_CVR)) {
			List<Attachment> attachmentsToBeAdded = new ArrayList<>();

			try {
				for (MultipartFile file : requirement.getLocalFiles()) {
					if (file.getOriginalFilename().isEmpty()) {
						continue;
					}

					String s3ObjectUrl = s3service.writeFile(file.getInputStream(), file.getOriginalFilename());
					if (s3ObjectUrl != null) {
						Attachment attachment = new Attachment();
						attachment.setName(file.getOriginalFilename());
						attachment.setRequirement(requirementToSave);
						attachment.setUrl(s3ObjectUrl);
						attachmentsToBeAdded.add(attachment);
					}
				}
			}
			catch (IOException ex) {
				bindingResult.rejectValue("localFiles", "requirement.localFiles", "Unable to upload file. Try again.");
			}
			
			List<Long> attachmentsToBeRemoved = requirement.getRemoveLocalAttachments();
			requirementService.updateLocalExtensionsOnly(requirement, attachmentsToBeAdded, attachmentsToBeRemoved);
			
			// do not then later save the helpText on the global requirement
			requirement.setHelpText(null);
		}

		List<Community> communities = communityService.getCommunities(SecurityUtil.getMunicipalityCvr());
		Set<String> communityCvrs = communities.stream().map(c -> c.getCommunityCvr()).collect(Collectors.toSet());
		if (communityCvrs.contains(requirementToSave.getCvr())) {
			requirementService.updateLocalExtensionsForCommunityRequirement(requirementToSave, requirement.isFavorite());
		}

		// only perform updates if it is possible to actually modify the requirement
		if (requirementService.canModify(requirementToSave)) {
			List<Attachment> attachmentsToBeAdded = new ArrayList<>();
			try {
				for (MultipartFile file : requirement.getFiles()) {
					if (file.getOriginalFilename().isEmpty()) {
						continue;
					}
	
					String s3ObjectUrl = s3service.writeFile(file.getInputStream(), file.getOriginalFilename());
					if (s3ObjectUrl != null) {
						Attachment attachment = new Attachment();
						attachment.setName(file.getOriginalFilename());
						attachment.setRequirement(requirementToSave);
						attachment.setUrl(s3ObjectUrl);
						attachmentsToBeAdded.add(attachment);
					}
				}
			}
			catch (IOException ex) {
				bindingResult.rejectValue("files", "requirement.files", "Unable to upload file. Try again.");
			}
	
			if (bindingResult.hasErrors()) {
				for (String tagId : requirement.getTags()) {
					Tag tag = tagDao.getById(Long.parseLong(tagId));
					requirement.getTagsRichObjects().add(new TagForm(tag.getName(), tag.getId(), tag.getQuestion()));
				}
	
				for (long domainId : requirement.getDomains()) {
					Domain domain = domainDao.getById(domainId);
					requirement.getDomainsRichObjects().add(new DomainForm(domain.getId(), domain.getName()));
				}
	
				requirement.setAttachments(modelMapper.map(requirementService.getById(requirement.getId()).getAttachments(),new TypeToken<List<AttachmentDTO>>() {}.getType()));
	
				model.addAttribute(bindingResult.getAllErrors());
				model.addAttribute(requirement);
				model.addAttribute("categories",  modelMapper.map(categoryDao.findAll(), new TypeToken<List<CategoryForm>>() {}.getType()));
				model.addAttribute("domains", requirement.getDomainsRichObjects());
				model.addAttribute("tags", requirement.getTagsRichObjects());
				model.addAttribute("alldomains", modelMapper.map(domainDao.findAll(),new TypeToken<List<DomainForm>>() {}.getType()));
				model.addAttribute("alltags", modelMapper.map(tagDao.findAll(),new TypeToken<List<TagForm>>() {}.getType()));
				model.addAttribute("allprinciples", modelMapper.map(principleDao.findAll(),new TypeToken<List<PrincipleForm>>() {}.getType()));
	
				return "requirement/edit";
			}
			
			requirement.getRemoveAttachments().forEach(attachmentId -> requirementToSave.getAttachments().removeIf(a -> a.getId() == attachmentId));
			requirementToSave.getAttachments().addAll(attachmentsToBeAdded);
			attachmentsToBeAdded.clear();
			
			requirementToSave.setName(requirement.getName());
			
			Category category = categoryDao.getById(requirement.getCategory());
			requirementToSave.setCategory(category);
			Subcategory subcategory = category.getSubcategories().stream().filter(s -> s.getId() == requirement.getSubcategory()).findAny().orElse(null);
			requirementToSave.setSubcategory(subcategory);
			
			requirementToSave.setImportance(requirement.getImportance());
			requirementToSave.setDescription(requirement.getDescription());
			requirementToSave.setRationale(requirement.getRationale());
			requirementToSave.setInfoRequirement(requirement.isInfoRequirement());
			requirementToSave.setNotes(requirement.getNotes());
			requirementToSave.setHelpText(requirement.getHelpText());
			requirementToSave.setInterestedParty(requirement.getInterestedParty());
			requirementToSave.setAvailableForAllDomains(requirement.isAvailableForAllDomains());
			requirementToSave.setAvailableForAllTags(requirement.isAvailableForAllTags());
			requirementToSave.setRelevantForOnPremise(requirement.isRelevantForOnPremise());
			requirementToSave.setRelevantForSaas(requirement.isRelevantForSaas());

			// special case - this is saved as an extension
			if (!communityCvrs.contains(requirementToSave.getCvr())) {
				requirementToSave.setFavorite(requirement.isFavorite());
			}
	
			requirementToSave.getDomains().clear();
			requirementToSave.getTags().clear();
			requirementToSave.getPrinciples().clear();
	
			requirement.getDomains().stream().forEach(domain -> requirementToSave.getDomains().add(domainDao.getById(domain)));
			requirement.getTags().stream().forEach(tagId -> requirementToSave.getTags().add(tagDao.getById(Long.parseLong(tagId))));
			requirement.getPrinciples().stream().forEach(principle -> requirementToSave.getPrinciples().add(principleDao.getById(principle)));
	
			requirementService.save(requirementToSave);
		}

		boolean communityRequirement = communities.stream().map(c -> c.getCommunityCvr()).collect(Collectors.toSet()).contains(requirementToSave.getCvr());
		
		String redirect = "redirect:../requirement/list";
		if (communityRequirement) {
			redirect = "redirect:../requirement/listcommunity";
		}
		else if (requirementToSave.getCvr().equals(Constants.DEFAULT_CVR)) {
			redirect = "redirect:../requirement/listshared";
		}

		return redirect;
	}

	@RequestMapping(path = "requirement/{id}", method = RequestMethod.DELETE)
	@ResponseBody
	public ResponseEntity<String> deleteRequirement(@PathVariable("id") long id){
		Requirement requirement = requirementService.getById(id);
		if (requirement == null) {
			log.warn("Requested Requirement with ID:" + id + " not found.");
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

		requirementService.delete(requirement);
		
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@RequestMapping(path = {"requirement/", "requirement/list"}, method = RequestMethod.GET)
	public String listRequirement(Model model) {
		List<RequirementForm> requirements = modelMapper.map(requirementService.getAllRequirementsOfType(RequirementType.LOCAL), new TypeToken<List<RequirementForm>>() {}.getType());
		model.addAttribute("requirements", requirements);

		return "requirement/list";
	}

	@RequestMapping(path = {"requirement/listshared"}, method = RequestMethod.GET)
	public String listSharedRequirement(Model model) {
		List<RequirementForm> requirements = mapGlobalRequirementsToFromWithExtension(requirementService.getAllRequirementsOfType(RequirementType.GLOBAL));
		List<RequirementForm> tobepromoted = modelMapper.map(requirementService.getAllRequirementsOfType(RequirementType.TOBESHARED), new TypeToken<List<RequirementForm>>() {}.getType());
		model.addAttribute("requirements", requirements);
		model.addAttribute("tobepromoted", tobepromoted);

		return "requirement/listshared";
	}

	private List<RequirementForm> mapGlobalRequirementsToFromWithExtension(List<Requirement> allRequirements) {
		List<RequirementForm> result = new ArrayList<>();

		for (Requirement requirement : allRequirements) {
			RequirementForm requirementForm = modelMapper.map(requirement, RequirementForm.class);
			requirementForm.getDomains().stream().forEach(domain -> requirementForm.getDomainsRichObjects().add(modelMapper.map(domainDao.getById(domain), DomainForm.class)));
			requirementForm.getTags().stream().forEach(tagId -> requirementForm.getTagsRichObjects().add(modelMapper.map(tagDao.getById(Long.parseLong(tagId)), TagForm.class)));
			requirementForm.getPrinciples().stream().forEach(principle -> requirementForm.getPrinciplesRichObjects().add(modelMapper.map(principleDao.getById(principle), PrincipleForm.class)));
			requirementForm.setShared(requirement.isShared());

			RequirementExtension extension = requirementService.getRequirementExtension(requirement);
			if (extension != null) {
				requirementForm.setExtDescription(extension.getDescription());
				requirementForm.setLocalAttachments(extension.getAttachments());
				requirementForm.setFavorite(extension.isFavorite());
				requirementForm.setExtDisable(extension.isDisableRequirement());
				requirementForm.setExtDisableReason(extension.getDisableRequirementReason());
				requirementForm.setHelpText(extension.getHelpText());
				requirementForm.setInterestedParty(extension.getInterestedParty());
			}

			result.add(requirementForm);
		}

		return result;
	}

	@RequestMapping(path = {"requirement/listcommunity"}, method = RequestMethod.GET)
	public String listCommunityRequirement(Model model) {
		List<RequirementForm> requirements = modelMapper.map(requirementService.getAllRequirementsOfType(RequirementType.COMMUNITY), new TypeToken<List<RequirementForm>>() {}.getType());

		for (RequirementForm requirement : requirements) {
			requirement.setCvr(communityService.getCommunityName(requirement.getCvr()));
		}
		model.addAttribute("requirements", requirements);

		return "requirement/listcommunity";
	}
	
	@RequestMapping(path = {"requirement/listall"}, method = RequestMethod.GET)
	public String listAll(Model model) {
		List<RequirementTreeDTO> data = new ArrayList<>();
		List<String> roles = SecurityUtil.getRoles();
		boolean isGlobalEditor = roles.contains("ROLE_http://kravmotoren.dk/globaleditor");

		for (Category category : categoryDao.findAll()) {
			for (Subcategory subcategory : category.getSubcategories()) {
				RequirementTreeDTO subcategoryDTO = new RequirementTreeDTO();
				subcategoryDTO.setId("subcategory" + subcategory.getId());
				subcategoryDTO.setParent("category" + category.getId());
				subcategoryDTO.setText(subcategory.getName());
				data.add(subcategoryDTO);
			}
			
			// do not add local requirements when running in global editor mode (as this will cause duplicates, as local requirements ARE
			// global requirements in that special case ;))
			if (!"00000000".equals(securityUtil.getCvr())) {
				for (Requirement requirement : requirementService.getAllRequirementsOfTypeAndCategory(RequirementType.LOCAL, category)) {
					addRequirementDTO(data, category, requirement, "Lokalt krav", true, false, requirement.isFavorite());
				}
			}
			
			for (Requirement requirement : requirementService.getAllRequirementsOfTypeAndCategory(RequirementType.GLOBAL, category)) {
				RequirementExtension extension = requirementService.getRequirementExtension(requirement);

				if (extension != null) {
					addRequirementDTO(data, category, requirement, "Tværkomunalt krav", isGlobalEditor, extension.isDisableRequirement(), extension.isFavorite());
				}
				else {
					addRequirementDTO(data, category, requirement, "Tværkomunalt krav", isGlobalEditor, false, false);
				}
			}
			
			for (Requirement requirement : requirementService.getAllRequirementsOfTypeAndCategory(RequirementType.COMMUNITY, category)) {
				addRequirementDTO(data, category, requirement, "Samarbejdskrav", false, false, requirement.isFavorite());
			}
			
			RequirementTreeDTO categoryDTO = new RequirementTreeDTO();
			categoryDTO.setId("category" + category.getId());
			categoryDTO.setText(category.getName());
			data.add(categoryDTO);
		}
		model.addAttribute("data", data);

		return "requirement/listall";
	}

	private void addRequirementDTO(List<RequirementTreeDTO> data, Category category, Requirement requirement, String type, boolean canBeDeleted, boolean disabled, boolean favorite) {
		RequirementTreeDTO requirementDTO = new RequirementTreeDTO();
		requirementDTO.setId("requirement" + requirement.getId());
		requirementDTO.setParent(requirement.getSubcategory() != null ? "subcategory" + requirement.getSubcategory().getId() : "category" + category.getId());
		String badges = " <span class=\"label badge-label label-primary\">" + type + "</span>";
		badges += favorite ? " <span class=\"label badge-label label-success\">Favorit</span>" : "";
		badges += disabled ? " <span class=\"label badge-label label-danger\">Fravalgt</span>" : "";
		String text = requirement.getName() + badges + " <span class=\"handleIcons\"><em class=\"fa fa-fw fa-search\" onclick=\"treeService.view(" + requirement.getId() + ");\"></em><em class=\"fa fa-fw fa-pencil\" onclick=\"treeService.edit(" + requirement.getId() + ");\"></em>";
		text += canBeDeleted ? "<em class=\"fa fa-fw fa-times\" onclick=\"treeService.deleteRequirement(" + requirement.getId() + ");\"></em>" : "";
		text +=	"</span>";
		requirementDTO.setText(text);
		data.add(requirementDTO);
	}

	@GetMapping("/requirement/listdeleted")
	public String listDeletedRequirement(Model model) {
		List<RequirementForm> requirements = modelMapper.map(requirementService.getAllDeleted(), new TypeToken<List<RequirementForm>>() {}.getType());

		model.addAttribute("deletedRequirements", requirements);

		return "requirement/listdeleted";
	}

	@RequireLocalEditorRole
	@RequestMapping(path = "requirement/makeRequest/{id}", method = RequestMethod.POST)
	public String makeRequestToShare(PromoteRequestDTO request, @PathVariable("id") long id, Locale loc){
		Requirement requirement = requirementService.getById(id);

		if (requirement == null) {
			log.warn("Requested Requirement with ID:" + id + " not found.");
			return "redirect:../../requirement/list";
		}
		
		// if is already shared -> error
		if (requirement.isShared()) {
			log.warn("Tried to request promotion on Requirement: "+id + " but it is already Shared.");
			return "redirect:../../requirement/list";
		}
		
		// if is already requested -> error
		if (requirement.isRequestedToBeShared() == true) {
			log.warn("Tried to request promotion on Requirement: "+id + " but promotion was already Requested.");
			return "redirect:../../requirement/list";
		}
		
		if (request == null || StringUtils.isEmpty(request.getTarget())) {
			log.warn("Tried to request promotion on Requirement: " + id + " but the target was not specified or was wrong.");
			return "redirect:../../requirement/list";
		}
		
		if (request.getTarget().equals("global")) {
			requirement.setRequestedToBeShared(true);
			requirement.setRequesterEmail(SecurityUtil.getEmail());
			requirementService.save(requirement);
			
			var globaleditors = globalEditorDao.findAll();
			for (GlobalEditor globalEditor : globaleditors) {
				if (!StringUtils.hasLength(globalEditor.getEmail())) {
					continue;
				}

				try {
					String email = globalEditor.getEmail();
					String title = messageSource.getMessage("email.requirement.request.title", null, loc);
					String message = messageSource.getMessage("email.requirement.request.body", new String[] { requirement.getName(), requirement.getRequesterEmail(), request.getReason() }, loc);
					
					emailService.sendMessage(senderEmailAddress, email, title, message);
				} catch (Exception ex) {
					log.error("Error occured while trying to send email. ", ex);
				}
			}
			
			return "redirect:../../requirement/list";
		}
		
		try {
			int communityId = Integer.parseInt(request.getTarget());
			Community community = communityService.getById(communityId);
			
			requirementService.changeOwnership(requirement.getId(), community);
		}
		catch (Exception e) {
			log.warn("Tried to request promotion on Requirement: " + id + " but the specified target (Community: "+request.getTarget()+") is wrong.");
		}
		
		return "redirect:../../requirement/list";
	}
	
	@RequireLocalEditorRole
	@RequestMapping(path = "requirement/cancelRequest/{id}", method = RequestMethod.POST)
	public ResponseEntity<String> cancelRequestToShare(@PathVariable("id") long id){
		Requirement requirement = requirementService.getById(id);

		if (requirement==null) {
			log.warn("Requested Requirement with ID:" + id + " not found.");
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		
		// if is already shared -> error
		if (requirement.isShared()) {
			log.warn("Tried to cancel promotion request on Requirement: "+id + " but it is already Shared.");
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		
		// if is not requested -> error
		if (requirement.isRequestedToBeShared() != true) {
			log.warn("Tried to cancel promotion request on Requirement: " + id + " but it is not requested.");
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		
		requirement.setRequestedToBeShared(false);
		requirementService.save(requirement);
		
		return new ResponseEntity<>(HttpStatus.OK);
	}
	
	@RequireGlobalEditorRole
	@RequestMapping(path = "requirement/promote/{id}", method = RequestMethod.POST)
	public ResponseEntity<String> promote(@PathVariable("id") long id){
		Requirement requirement = requirementService.getById(id);

		if (requirement == null) {
			log.warn("Requested Requirement with ID:" + id + " not found.");
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

		// if is already shared -> error
		if (requirement.isShared()) {
			log.warn("Tried to promote Requirement: " + id + " but it is already Shared.");
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		
		// if is not requested -> error
		if (requirement.isRequestedToBeShared() != true) {
			log.warn("Tried to promote Requirement: " + id + " but it is not requested.");
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		
		requirementService.handlePromotionRequest(id, true, null);
		
		return new ResponseEntity<>(HttpStatus.OK);
	}
	
	@RequireGlobalEditorRole
	@RequestMapping(path = "requirement/reject/{id}", method = RequestMethod.POST)
	public ResponseEntity<String> reject(@PathVariable("id") long id, @RequestBody String reason){
		Requirement requirement = requirementService.getById(id);

		if (requirement == null) {
			log.warn("Requested Requirement with ID:" + id + " not found.");
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

		// if is already shared -> error
		if (requirement.isShared()) {
			log.warn("Tried to reject Requirement: " + id + " but it is already Shared.");
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

		// if is not requested -> error
		if (requirement.isRequestedToBeShared() != true) {
			log.warn("Tried to reject Requirement: " + id + " but it is not requested.");
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

		requirementService.handlePromotionRequest(id, false, reason);

		return new ResponseEntity<>(HttpStatus.OK);
	}
	
	@RequestMapping(path = "requirement/categories/{id}/getsubcategories", method = RequestMethod.GET)
	public ResponseEntity<?> getSubcategories(@PathVariable("id") long id){
		Category category = categoryDao.getById(id);
		if (category == null) {
			log.warn("Requested subcategories for category with ID:" + id + " not found.");
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<>(category.getSubcategories(), HttpStatus.OK);
	}
}
