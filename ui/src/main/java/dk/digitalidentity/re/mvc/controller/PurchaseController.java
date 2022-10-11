package dk.digitalidentity.re.mvc.controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;
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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import dk.digitalidentity.re.Constants;
import dk.digitalidentity.re.dao.CategoryDao;
import dk.digitalidentity.re.dao.DomainDao;
import dk.digitalidentity.re.dao.PurchaseAnswerDao;
import dk.digitalidentity.re.dao.PurchaseRequirementDao;
import dk.digitalidentity.re.dao.PurchaseVendorDao;
import dk.digitalidentity.re.dao.TagDao;
import dk.digitalidentity.re.dao.VendorOrganizationDao;
import dk.digitalidentity.re.dao.VendorUserDao;
import dk.digitalidentity.re.dao.model.Community;
import dk.digitalidentity.re.dao.model.Domain;
import dk.digitalidentity.re.dao.model.EmailTemplate;
import dk.digitalidentity.re.dao.model.Purchase;
import dk.digitalidentity.re.dao.model.PurchaseAnswer;
import dk.digitalidentity.re.dao.model.PurchaseRequirement;
import dk.digitalidentity.re.dao.model.PurchaseRequirementAnswer;
import dk.digitalidentity.re.dao.model.PurchaseVendor;
import dk.digitalidentity.re.dao.model.PurchaseVendorAnswer;
import dk.digitalidentity.re.dao.model.Requirement;
import dk.digitalidentity.re.dao.model.RequirementExtension;
import dk.digitalidentity.re.dao.model.Tag;
import dk.digitalidentity.re.dao.model.VendorOrganization;
import dk.digitalidentity.re.dao.model.VendorUser;
import dk.digitalidentity.re.dao.model.enums.AnswerChoice;
import dk.digitalidentity.re.dao.model.enums.CustomerSetting;
import dk.digitalidentity.re.dao.model.enums.EmailTemplateType;
import dk.digitalidentity.re.dao.model.enums.Importance;
import dk.digitalidentity.re.dao.model.enums.RequirementType;
import dk.digitalidentity.re.dao.model.enums.SolutionType;
import dk.digitalidentity.re.dao.model.enums.Status;
import dk.digitalidentity.re.mvc.dto.UpdatePurchase;
import dk.digitalidentity.re.mvc.dto.UpdatePurchaseStatus;
import dk.digitalidentity.re.mvc.form.CustomRequirementForm;
import dk.digitalidentity.re.mvc.form.DomainForm;
import dk.digitalidentity.re.mvc.form.InviteVendorForm;
import dk.digitalidentity.re.mvc.form.PurchaseForm;
import dk.digitalidentity.re.mvc.form.QuestionnaireForm;
import dk.digitalidentity.re.mvc.form.RequirementForm;
import dk.digitalidentity.re.mvc.form.TagForm;
import dk.digitalidentity.re.mvc.form.UpdateRequirementDescriptionForm;
import dk.digitalidentity.re.mvc.validator.CustomRequirementFormValidator;
import dk.digitalidentity.re.mvc.validator.InviteFormValidator;
import dk.digitalidentity.re.mvc.validator.PurchaseFormValidator;
import dk.digitalidentity.re.mvc.validator.ValidatorUtil;
import dk.digitalidentity.re.mvc.view.AnswerPdfView;
import dk.digitalidentity.re.mvc.view.AnswerXlsView;
import dk.digitalidentity.re.mvc.view.PurchaseAnswerPdfView;
import dk.digitalidentity.re.mvc.view.PurchaseAnswerXlsView;
import dk.digitalidentity.re.mvc.view.PurchasePdfView;
import dk.digitalidentity.re.mvc.view.PurchasePdfViewOld;
import dk.digitalidentity.re.mvc.view.RequirementXlsView;
import dk.digitalidentity.re.security.RequireAdministratorRole;
import dk.digitalidentity.re.security.RequirePurchaserRole;
import dk.digitalidentity.re.security.SecurityUtil;
import dk.digitalidentity.re.service.CommunityService;
import dk.digitalidentity.re.service.EmailTemplateService;
import dk.digitalidentity.re.service.MailSenderService;
import dk.digitalidentity.re.service.PurchaseRequirementService;
import dk.digitalidentity.re.service.PurchaseService;
import dk.digitalidentity.re.service.RequirementService;
import dk.digitalidentity.re.service.SettingService;
import dk.digitalidentity.re.service.VendorService;

@SuppressWarnings("deprecation")
@RequirePurchaserRole
@Controller
public class PurchaseController {
	private static final Logger log = LoggerFactory.getLogger(PurchaseController.class);

	@Value("${email.sender}")
	private String senderEmailAddress;
	
	@Value("${kitos.base.url}")
	private String kitosURL;

	@Autowired
	private CommunityService communityService;

	@Autowired
	private ModelMapper modelMapper;

	@Autowired
	private RequirementService requirementService;

	@Autowired
	private PurchaseService purchaseService;

	@Autowired
	private PurchaseRequirementDao purchaseRequirementDao;
	
	@Autowired
	private CategoryDao categoryDao;

	@Autowired
	private TagDao tagDao;

	@Autowired
	private CustomRequirementFormValidator customRequirementFormValidator;

	@Autowired
	private DomainDao domainDao;

	@Autowired
	private PurchaseVendorDao answerDao;

	@Autowired
	private VendorOrganizationDao vendorOrganizationDao;
	
	@Autowired
	private PurchaseRequirementService purchaseRequirementService;

	@Autowired
	private VendorUserDao vendorUserDao;

	@Autowired
	private PurchaseAnswerDao purchaseAnswerDao;

	@Autowired
	private PurchaseFormValidator purchaseFormValidator;

	@Autowired
	private InviteFormValidator inviteVendorFormValidator;

	@Autowired
	private SettingService settingService;

	@Autowired
	private MailSenderService emailService;

	@Autowired
	private VendorService vendorService;

	@Autowired
	private MessageSource messageSource;
	
	@Autowired
	private SecurityUtil securityUtil;

	@Autowired
	private EmailTemplateService emailTemplateService;

	@InitBinder(value = {"requirement", "customRequirement"})
	public void initBinder(WebDataBinder binder) {
		binder.addValidators(customRequirementFormValidator);
	}

	@InitBinder(value = {"newPurchase"})
	public void initBinderPurchaseForm(WebDataBinder binder) {
		binder.addValidators(purchaseFormValidator);
	}

	@InitBinder(value = {"inviteVendorForm"})
	public void initBinderInviteVendor(WebDataBinder binder) {
		binder.addValidators(inviteVendorFormValidator);
	}

	@RequestMapping(path = "purchase/list", method = RequestMethod.GET)
	public String list(Model model) {
		List<Purchase> purchases = purchaseService.getAllPurchases();
		purchases = purchases.stream().filter(r -> !r.getStatus().equals(Status.ARCHIVED)).collect(Collectors.toList());
		
		// ModelMapper breaks the page
		model.addAttribute("purchases", purchases);// modelMapper.map(purchases, new TypeToken<List<PurchaseForm>>() { }.getType()));

		return "purchase/list";
	}
	
	@RequestMapping(path = "purchase/listarchived", method = RequestMethod.GET)
	public String listArchived(Model model) {
		List<Purchase> purchases = purchaseService.getAllPurchases();
		purchases = purchases.stream().filter(r -> r.getStatus().equals(Status.ARCHIVED)).collect(Collectors.toList());
		
		// ModelMapper breaks page
		model.addAttribute("purchases", purchases);// modelMapper.map(purchases, new TypeToken<List<PurchaseForm>>() { }.getType()));

		return "purchase/listarchived";
	}

	@RequestMapping(path = "purchase/new", method = RequestMethod.GET)
	public String newRequirement(Model model) {
		model.addAttribute("newPurchase", new PurchaseForm());
		model.addAttribute("domains", modelMapper.map(domainDao.findAll(),new TypeToken<List<DomainForm>>() {}.getType()));

		return "purchase/new";
	}

	@RequestMapping(path = "purchase/new", method = RequestMethod.POST)
	public String saveNewPurchase(Model model, @Valid @ModelAttribute("newPurchase") PurchaseForm purchase, BindingResult bindingResult) {
		if (bindingResult.hasErrors()) {
			model.addAttribute(bindingResult.getAllErrors());
			model.addAttribute("newPurchase", purchase);
			model.addAttribute("domains", modelMapper.map(domainDao.findAll(),new TypeToken<List<DomainForm>>() {}.getType()));

			return "purchase/new";
		}

		Purchase purchaseToSave = new Purchase(); //modelMapper.map(purchase, Purchase.class);
		purchaseToSave.setTitle(purchase.getTitle());
		purchaseToSave.setDescription(purchase.getDescription());
		purchaseToSave.setEmail(purchase.getEmail());
		purchaseToSave.setRequirementPriorityEnabled(purchase.isRequirementPriorityEnabled());
		
		purchaseToSave.setDomains(new ArrayList<>());
		purchaseToSave.setStatus(Status.DRAFT);
		purchaseToSave.setStartTime(new Date());

		purchase.getDomains().stream().forEach(domain -> purchaseToSave.getDomains().add(domainDao.getById(Long.parseLong(domain))));
		long purchaseId = purchaseService.save(purchaseToSave).getId();

		return "redirect:/purchase/questionnaire/" + purchaseId + "/skip";
	}
	
	@RequestMapping(path = "purchase/archive/{id}", method = RequestMethod.GET)
	public String archive(Model model, @ModelAttribute @PathVariable("id") long id) {
		Purchase purchase = purchaseService.getById(id);
		if (purchase == null) {
			log.warn("Requested Purchase with ID:"+ id + " not found.");
			return "redirect:../../purchase/list";
		}

		if (purchase.getStatus().equals(Status.ACTIVE) || purchase.getStatus().equals(Status.DRAFT)) {
			log.warn("Requested Purchase with ID:"+ id + " is either active or in draft, and cannot be archived.");
			return "redirect:../../purchase/list";
		}

		purchase.setStatus(Status.ARCHIVED);
		purchaseService.save(purchase);

		return "redirect:../../purchase/list";
	}

	@RequestMapping(path = "purchase/view/{id}", method = RequestMethod.GET)
	public String viewPurchase(Model model, @ModelAttribute @PathVariable("id") long id) {
		Purchase purchase = purchaseService.getById(id);
		if (purchase == null) {
			log.warn("Requested Purchase with ID:"+ id + " not found.");
			return "redirect:../../purchase/list";
		}

		boolean oldVendorModel = purchaseService.isOldVendorModel(purchase);
		model.addAttribute("oldVendorModel",oldVendorModel);
		// *** OLD VendorModel ***
		if( oldVendorModel )
		{
			model.addAttribute("answers", answerDao.findAllByPurchase(purchase));

		}
		// *** NEW VendorModel ***
		{

			model.addAttribute("purchaseAnswers",purchaseService.getPurchaseAnswers(purchase));
		}

		model.addAttribute("purchase", purchase);
		model.addAttribute("domains", purchase.getDomains());

		return "purchase/view";
	}

	@RequireAdministratorRole
	@RequestMapping(path = "purchase/{id}", method = RequestMethod.DELETE)
	@ResponseBody
	public ResponseEntity<String> deletePurchase(@PathVariable("id") long id) {
		Purchase purchase = purchaseService.getById(id);

		if (purchase == null) {
			log.warn("Cannot delete purchase with id = " + id);
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

		purchaseService.delete(purchase);

		return new ResponseEntity<>(HttpStatus.OK);
	}


	@RequestMapping(path = "purchase/edit/{id}", method = RequestMethod.GET)
	public String edit(Model model, @PathVariable("id") long id) {
		String failureUrl = updateEditModel(model, id);
		if (failureUrl != null) {
			return failureUrl;
		}

		Purchase purchase = purchaseService.getById(id);

		//Pre-populate title and message from template
		EmailTemplate template = emailTemplateService.findByTemplateType(EmailTemplateType.INVITE_USER);
		
		String emailSubject = template.getTitle();
		emailSubject = emailSubject.replace(EmailTemplateService.PURCHASE_TITLE, purchase.getTitle());
		String emailBody = template.getMessage();
		
		SimpleDateFormat parser = new SimpleDateFormat("yyyy/MM/dd - HH:mm");

		emailBody = emailBody.replace(EmailTemplateService.PURCHASE_TITLE, purchase.getTitle());
		emailBody = emailBody.replace(EmailTemplateService.CUSTOM_MESSAGE, ""); //if someone added this to the template
		emailBody = emailBody.replace(EmailTemplateService.PURCHASE_DEADLINE, (purchase.getEndTime() != null) ? parser.format(purchase.getEndTime()) : "ikke sat");

		InviteVendorForm inviteVendorForm = new InviteVendorForm();
		inviteVendorForm.setTitle(emailSubject);
		inviteVendorForm.setMessage(emailBody);

		model.addAttribute("oldVendorModel", purchaseService.isOldVendorModel(purchase));
		model.addAttribute("customRequirement", new CustomRequirementForm());
		model.addAttribute("inviteVendorForm", inviteVendorForm);
		model.addAttribute("answers", answerDao.findAllByPurchase(purchase));

		return "purchase/edit";
	}

	@RequestMapping(path = "purchase/edit/{id}/inviteVendor", method = RequestMethod.POST)
	public String sendInviteToVendor(Model model, @PathVariable("id") long id, @Valid @ModelAttribute("inviteVendorForm") InviteVendorForm inviteForm, BindingResult bindingResult, Locale loc) {
		String failureUrl = updateEditModel(model, id);
		if (failureUrl != null) {
			return failureUrl;
		}

		Purchase purchase = purchaseService.getById(id);
		if (purchase == null) {
			return "redirect:/purchase/list";
		}

		if (!purchase.getStatus().equals(Status.ACTIVE)) {
			log.warn("User tried to invite vendor to project " + id + " however the purchase status is not ACTIVE");
			return "redirect:/purchase/edit/" + purchase.getId();
		}

		if (bindingResult.hasErrors()) {
			model.addAttribute(bindingResult.getAllErrors());
			model.addAttribute("customRequirement", new CustomRequirementForm());

			return "purchase/edit";
		}

		String email = inviteForm.getEmail();
		String title = inviteForm.getTitle();
		String message = inviteForm.getMessage();

		var emailDomain = getEmailDomain(email);
		var vendorOrganization = vendorOrganizationDao.getByDomain(emailDomain);
		// create a new vendor organization if it doesn't exist
		if (vendorOrganization == null) {
			vendorOrganization = new VendorOrganization();
			vendorOrganization.setDomain(emailDomain);
		}
		vendorOrganizationDao.save(vendorOrganization);

		// create a new purchase answer if it doesn't exist
		var purchaseAnswer = purchaseAnswerDao.getByPurchaseIdAndVendorOrganizationId(purchase.getId(),vendorOrganization.getId());
		if (purchaseAnswer == null) {
			purchaseAnswer = new PurchaseAnswer();
			purchaseAnswer.setCreated(new Date());
			purchaseAnswer.setPurchase(purchase);
			purchaseAnswer.setSolutionType(SolutionType.NOT_SET);
			purchaseAnswer.setVendorOrganization(vendorOrganization);
			purchaseAnswer.setPurchaseRequirementAnswers(new ArrayList<>());
			for (var purchaseRequirement : purchase.getRequirements()) {
				var purchaseRequirementAnswer = new PurchaseRequirementAnswer();
				purchaseRequirementAnswer.setRequirement(purchaseRequirement);
				purchaseRequirementAnswer.setPurchaseAnswer(purchaseAnswer);
				purchaseRequirementAnswer.setAnswered(false);
				purchaseAnswer.getPurchaseRequirementAnswers().add(purchaseRequirementAnswer);
			}
			
			purchaseAnswerDao.save(purchaseAnswer);
		}

		// create a new user if it doesn't exist
		var vendorUser = vendorUserDao.getByEmail(email);
		if (vendorUser == null) {
			vendorUser = new VendorUser();
			vendorUser.setEmail(email);
			vendorUser.setVendorOrganization( vendorOrganization );

			// if the vendor does not have an admin - set this user as admin
			if (vendorOrganization.getVendorUsers().stream().noneMatch(u -> u.isAdmin())) {
				vendorUser.setAdmin(true);
			}
			var password = vendorService.getRandomPassword();

			String extraDetails = messageSource.getMessage("email.body.newuser.template", new String[] { email, password }, loc);
			message += "</br>" + extraDetails;

			var encoder = new BCryptPasswordEncoder();
			vendorUser.setPassword(encoder.encode(password));
		}
		else {
			String extraDetails = messageSource.getMessage("email.body.existinguser.template", new String[] { email }, loc);
			message += "</br>" + extraDetails;
		}

		// add purchase answer to vendor user if it is not already added
		if (!vendorUser.getPurchaseAnswers().contains(purchaseAnswer)) {
			vendorUser.getPurchaseAnswers().add(purchaseAnswer);
		}
		vendorUserDao.save(vendorUser);

		try {
			emailService.sendMessage(senderEmailAddress, email, title, message);
		} catch (Exception ex) {
			log.warn("Error occured while trying to send email. ", ex);
		}

		return "redirect:/purchase/edit/" + id;
	}

	@RequestMapping(path = "purchase/questionnaire/{purchaseId}/skip")
	public String askSkipQestionnaire(Model model, @PathVariable("purchaseId") String purchaseId) {
		Purchase purchase = purchaseService.getById(Long.parseLong(purchaseId));

		if (purchase == null) {
			log.warn("Requested Purchase with ID:" + purchaseId + " not found.");
			return "error/notfound";
		}

		return "purchase/skip";
	}

	@RequestMapping(path = "purchase/questionnaire/{purchaseId}/skip", method = RequestMethod.POST)
	public String skipQestionnaire(Model model, @PathVariable("purchaseId") long purchaseId) {
		Purchase purchase = purchaseService.getById(purchaseId);

		if (purchase == null) {
			log.warn("Requested Purchase with id " + purchaseId + " not found.");
			return "error/notfound";
		}

		purchase.setQuestionnaireFilledOut(true);

		Set<Requirement> requirements = getFavoriteRequirements(purchase);

		// now filter out all requirements that are from the wrong domains
		for (Iterator<Requirement> iterator = requirements.iterator(); iterator.hasNext();) {
			Requirement req = iterator.next();
			
			if (req.isAvailableForAllDomains()) {
				continue;
			}

			boolean found = false;
			for (Domain domain : purchase.getDomains() ) {
				if (req.getDomains().contains(domain)) {
					found = true;
					break;
				}
			}
			
			if (!found) {
				iterator.remove();
			}
		}

		// magical method, that ensures we do not add this information multiple times (like double-posts and other nasties)
		purchaseService.addRequirementsWithMutex(purchaseId, requirements);

		purchaseService.save(purchase);

		return "redirect:/purchase/edit/" + purchaseId;
	}

	@RequestMapping(path = "purchase/questionnaire/{purchaseId}")
	public String questionnaire(Model model, @PathVariable("purchaseId") long purchaseId) {
		Purchase purchase = purchaseService.getById(purchaseId);
		if (purchase == null) {
			log.warn("Requested Purchase with id "+ purchaseId + " not found.");
			return "error/notfound";
		}

		if (purchase.getRequirements().size() > 0) {
			log.warn("Unable to show questionnaire for purchase with id " + purchaseId + " as it already has requirements attached");
			return "redirect:/purchase/edit/" + purchaseId;
		}

		List<TagForm> tags = modelMapper.map(tagDao.findAll(), new TypeToken<List<TagForm>>() { }.getType());

		model.addAttribute("tags", tags);
		model.addAttribute("questionnaire", new QuestionnaireForm());
		model.addAttribute("purchaseId", purchaseId);

		return "purchase/questionnaire";
	}

	@RequestMapping(path = "purchase/questionnaire/{purchaseId}", method = RequestMethod.POST)
	public String questionnaireAnswers(Model model, @PathVariable("purchaseId") long purchaseId, QuestionnaireForm questionnaireForm) {
		Purchase purchase = purchaseService.getById(purchaseId);
		if (purchase == null) {
			log.warn("Requested Purchase with ID:" + purchaseId + " not found.");
			return "error/notfound";
		}

		// list of requirements there have to be selected as default
		Set<Requirement> requirements = new HashSet<>();
		boolean yesToAtLeastOneQuestion = false;
		
		// running through all tags/answers from questionnaire
		for (Tag tag : tagDao.findAll()) {
			String answer = questionnaireForm.getAnswers().get((int) tag.getId());

			// The UI shows two choices, Yes or Do-Not-Know, both of which maps to YES, hence the single check here
			if (AnswerChoice.YES.toString().equals(answer)) {
				yesToAtLeastOneQuestion = true;
				
				// Get all requirements from the tag corresponding to the answer
				requirements.addAll(requirementService.getByTagsContains(tag));
			}
		}

		// if the user has answered yes to at least one question, then we add all requirements that are available for all tags
		if (yesToAtLeastOneQuestion) {
			requirements.addAll(requirementService.getByTagsAvailableForAll());
		}

		// Add favorite requirements
		Set<Requirement> favoriteRequirements = getFavoriteRequirements(purchase);
		requirements.addAll(favoriteRequirements);
		
		// now filter out all requirements that are from the wrong domains
		for (Iterator<Requirement> iterator = requirements.iterator(); iterator.hasNext();) {
			Requirement req = iterator.next();
			
			if (req.isAvailableForAllDomains()) {
				continue;
			}

			boolean found = false;
			for (Domain domain : purchase.getDomains() ) {
				if (req.getDomains().contains(domain)) {
					found = true;
					break;
				}
			}
			
			if (!found) {
				iterator.remove();
			}
		}

		// magical method, that ensures we do not add this information multiple times (like double-posts and other nasties)
		purchaseService.addRequirementsWithMutex(purchaseId, requirements);

		Set<Requirement> allPossibleRequirements = new HashSet<>();
		for (Domain domainInPurchase : purchase.getDomains()) {
			allPossibleRequirements.addAll(requirementService.getByDomainsContains(domainInPurchase));
		}
		
		// we also need to count all the requirements that are available for all domains - otherwise our MAX count will be off
		for (Requirement req : requirementService.getAllRequirements()) {
			if (req.isAvailableForAllDomains()) {
				allPossibleRequirements.add(req);
			}
		}

		model.addAttribute("requirementsChosen", requirements.size());
		model.addAttribute("requirementsMax", allPossibleRequirements.size());
		model.addAttribute("purchaseId", purchaseId);

		return "purchase/questionnaireresult";
	}

	@ResponseBody
	@PostMapping(path = "purchase/{id}/setPriority/{requirementId}/{priority}")
	public ResponseEntity<String> setPriorityOnRequirement(@PathVariable("id") long purchaseId, @PathVariable("requirementId") long requirementId, @PathVariable("priority") Importance priority) {
		Purchase purchase = purchaseService.getById(purchaseId);
		if (purchase == null) {
			log.warn("Requested Purchase with ID:" + purchaseId + " not found.");
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

		if (!purchase.getStatus().equals(Status.DRAFT)) {
			log.warn("Rejecting priority change on purchase with ID:" + purchaseId + " because status is not DRAFT.");
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		
		if (!purchase.isRequirementPriorityEnabled()) {
			log.warn("Rejecting priority change on purchase with ID:" + purchaseId + " because RequirementPriority is not enabled.");
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

		boolean modified = false;
		for (PurchaseRequirement requirement : purchase.getRequirements()) {
			if (requirement.getRequirementId() == requirementId) {
				requirement.setImportance(priority);
				modified = true;
			}
		}
		
		if (modified) {
			purchaseService.save(purchase);
		}

		return new ResponseEntity<>(HttpStatus.OK);
	}

	@RequestMapping(path = "purchase/{id}/addcustomrequirement", method = RequestMethod.POST)
	public String addCustomRequirement(Model model, @PathVariable("id") long purchaseId, @Valid @ModelAttribute("customRequirement") CustomRequirementForm customRequirement, BindingResult bindingResult) {
		String failureUrl = updateEditModel(model, purchaseId);
		if (failureUrl != null) {
			return failureUrl;
		}

		Purchase purchase = purchaseService.getById(purchaseId);

		if (!purchase.getStatus().equals(Status.DRAFT)) {
			log.warn("User tried to add custom requirement to project " + purchaseId + " that is not in DRAFT status!");
			return "redirect:/purchase/edit/" + purchase.getId();
		}

		if (bindingResult.hasErrors()) {
			model.addAllAttributes(bindingResult.getAllErrors());
			model.addAttribute("inviteVendorForm", new InviteVendorForm());

			return "purchase/edit";
		}

		PurchaseRequirement purchaseRequirement = modelMapper.map(customRequirement, PurchaseRequirement.class);
		purchaseRequirement.setCategory(categoryDao.getById(customRequirement.getCategory()));
		purchaseRequirement.setPurchase(purchase);
		purchaseRequirementDao.save(purchaseRequirement);

		purchase.getRequirements().add(purchaseRequirement);
		purchaseService.save(purchase);

		return "redirect:/purchase/edit/" + purchaseId;
	}

	@ResponseBody
	@RequestMapping(path = "purchase/{purchaseId}/customrequirement/{requirementId}", method = RequestMethod.DELETE)
	public ResponseEntity<String> deleteCustomRequirement(@PathVariable("purchaseId") long purchaseId, @PathVariable("requirementId") long requirementId) {
		Purchase purchase = purchaseService.getById(purchaseId);

		if (purchase == null) {
			log.warn("Requested Purchase with ID:" + purchaseId + " not found.");
		} else if (!purchase.getStatus().equals(Status.DRAFT)) {
			log.warn("User tried to remove custom requirement from purchase " + purchaseId + " but project was no in status DRAFT");
			return new ResponseEntity<>("Purchase is not in DRAFT status", HttpStatus.BAD_REQUEST);
		} else {
			PurchaseRequirement toDelete = purchaseRequirementDao.getByIdAndPurchase(requirementId, purchase);

			if (toDelete != null) {
				purchaseRequirementDao.delete(toDelete);
			}
		}

		return new ResponseEntity<>(HttpStatus.OK);
	}

	@ResponseBody
	@RequestMapping(path = "purchase/{id}/requirement/{requirementid}", method = RequestMethod.PUT)
	public ResponseEntity<String> addRequirement(@PathVariable("id") long purchaseId, @PathVariable("requirementid") long requirementId) {
		Purchase purchase = purchaseService.getById(purchaseId);
		Requirement requirement = requirementService.getById(requirementId);

		if (purchase == null) {
			log.warn("Requested Purchase with ID:" + purchaseId + " not found.");
			return new ResponseEntity<>("Purchase not found in the database", HttpStatus.BAD_REQUEST);
		}
		else if (requirement == null) {
			log.warn("Requested Requirement with ID:" + requirementId + " not found.");
			return new ResponseEntity<>("Requirement not found in the database", HttpStatus.BAD_REQUEST);
		}
		if (purchase.getStatus() != Status.DRAFT) {
			log.warn("Unable to add requirement with ID:" + requirementId + " to purchase:" + purchaseId
					+ " becasue Purchase's Status is " + purchase.getStatus());
			return new ResponseEntity<>("Wrong purchase status.", HttpStatus.BAD_REQUEST);
		}

		PurchaseRequirement purchaseRequirement = purchaseRequirementDao.getByRequirementIdAndPurchase(requirementId, purchase);
		if (purchaseRequirement != null) {
			log.warn("Requested Requirement with ID:" + requirementId + " has already been added to purchase project with ID " + purchase.getId());
			return new ResponseEntity<>("Requirement has already been added", HttpStatus.BAD_REQUEST);
		}

		purchaseRequirement = new PurchaseRequirement();
		purchaseRequirement.setPurchase(purchase);
		purchaseRequirement.setRequirementId(requirement.getId());

		RequirementExtension extension = requirementService.getRequirementExtension(requirement);
		if (extension != null) {
			purchaseRequirement.setDescription(requirement.getDescription() + "\n\n" + extension.getDescription());
		} else {
			purchaseRequirement.setDescription(requirement.getDescription());
		}

		purchaseRequirement.setRationale(requirement.getRationale());
		purchaseRequirement.setInfoRequirement(requirement.isInfoRequirement());
		purchaseRequirement.setImportance(requirement.getImportance());
		purchaseRequirement.setName(requirement.getName());
		purchaseRequirement.setCategory(requirement.getCategory());
		purchaseRequirement.setSubcategory(requirement.getSubcategory());
		purchaseRequirement.setLastChanged(requirement.getLastChanged());

		purchaseRequirement = purchaseRequirementDao.save(purchaseRequirement);

		return new ResponseEntity<>(Long.toString(purchaseRequirement.getId()), HttpStatus.OK);
	}

	@ResponseBody
	@RequestMapping(path = "purchase/{id}/requirement/{requirementid}", method = RequestMethod.DELETE)
	public ResponseEntity<String> removeRequirement(@PathVariable("id") long purchaseId, @PathVariable("requirementid") long requirementId) {
		Purchase purchase = purchaseService.getById(purchaseId);
		if (purchase == null) {
			log.warn("Cannot remove a requirement from purchase with id=" + purchaseId + " because that purchase does not exist");
			return new ResponseEntity<>("Purchase not found in the database", HttpStatus.BAD_REQUEST);
		}

		if (purchase.getStatus() != Status.DRAFT) {
			log.warn("Unable to remove requirement with ID:" + requirementId + " from purchase:" + purchaseId
					+ " becasue Purchase's Status is " + purchase.getStatus());
			return new ResponseEntity<>("Wrong purchase status.", HttpStatus.BAD_REQUEST);
		}

		PurchaseRequirement purchaseRequirement = purchaseRequirementDao.getByRequirementIdAndPurchase(requirementId, purchase);
		if (purchaseRequirement == null) {
			log.warn("Tried to delete requirement with id=" + requirementId + " which does not exist in purchase with id=" + purchaseId);
			return new ResponseEntity<>("Tried to delete Requirement:"+requirementId+" that is not found in the Purchase:"+purchaseId, HttpStatus.BAD_REQUEST);
		}

		// Check if user has right to modify purchase requirements
		if (settingService.getBooleanValueByKey(CustomerSetting.ONLY_EDITORS_CAN_DESELECT_REQUIREMENT) &&
			purchaseRequirement.getImportance().equals(Importance.ABSOLUTE) &&
			!SecurityUtil.getRoles().contains("ROLE_http://kravmotoren.dk/editor")) {

			log.warn("User: " + SecurityUtil.getUser() + " doesn't have right to delete purchase's requirements.");
			return new ResponseEntity<>("Unauthorized: Access is denied", HttpStatus.BAD_REQUEST);
		}

		purchaseRequirementDao.delete(purchaseRequirement);

		return new ResponseEntity<>(HttpStatus.OK);
	}

	@ResponseBody
	@RequestMapping(path = "purchase/{id}/update/{fieldName}", method = RequestMethod.POST)
	public ResponseEntity<String> updatePurchase(@PathVariable("id") long purchaseId, @PathVariable("fieldName") String fieldName, @RequestBody UpdatePurchase updatePurchase, Locale loc) {
		Purchase purchase = purchaseService.getById(purchaseId);

		if (purchase == null) {
			log.warn("Requested Purchase with ID:" + purchaseId + " not found.");
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

		switch (fieldName) {
		case "title":
			if (updatePurchase.getField() == null || updatePurchase.getField().length() < 5) {
				log.warn("Title cannot be empty and must contain at least 5 characters.");
				return new ResponseEntity<>("title", HttpStatus.BAD_REQUEST);
			}
			if (!purchase.getStatus().equals(Status.DRAFT)) {
				log.warn("User tried to modify Purchase's title but the project is in " + purchase.getStatus() + " status.");
				return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
			}
			purchase.setTitle(updatePurchase.getField());
			break;
		case "description":
			if (updatePurchase.getField() == null || updatePurchase.getField().length() < 5) {
				log.warn("Description cannot be empty and must contain at least 5 characters.");
				return new ResponseEntity<>("description", HttpStatus.BAD_REQUEST);
			}
			if (!purchase.getStatus().equals(Status.DRAFT)) {
				log.warn("User tried to modify Purchase's description but the project is in " + purchase.getStatus() + " status.");
				return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
			}
			purchase.setDescription(updatePurchase.getField());
			break;
		case "email":
			if (updatePurchase.getField() == null || updatePurchase.getField().isEmpty() || !ValidatorUtil.isValidEmailAddress(updatePurchase.getField())) {
				log.warn("Email cannot be empty and must be a valid email.");
				return new ResponseEntity<>("email", HttpStatus.BAD_REQUEST);
			}
			if (!purchase.getStatus().equals(Status.DRAFT) && !purchase.getStatus().equals(Status.ACTIVE)) {
				log.warn("User tried to modify Purchase's email but the project is in " + purchase.getStatus() + " status.");
				return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
			}
			purchase.setEmail(updatePurchase.getField());
			break;
		case "endTime":
			// ignore changes to archived purchase projects
			if (purchase.getStatus().equals(Status.ARCHIVED)) {
				return new ResponseEntity<>(HttpStatus.OK);
			}

			Date endTime = null;
			SimpleDateFormat parser = new SimpleDateFormat("yyyy/MM/dd - HH:mm");

			if (updatePurchase.getField() != null && !updatePurchase.getField().isEmpty()) {
				try {
					endTime = parser.parse(updatePurchase.getField());
				} catch (ParseException e) {
					log.error("Unable to parse:'" + updatePurchase.getField() + "' to DateTime.");
					return new ResponseEntity<>("endTimeFormat", HttpStatus.BAD_REQUEST);
				}
			}
			
			if (endTime != null && endTime.before(new Date())) {
				log.warn("Deadline has to be in the future");
				return new ResponseEntity<>("endTime", HttpStatus.BAD_REQUEST);
			}
			
			if (purchase.getStatus().equals(Status.ACTIVE) && endTime == null) {
				log.warn("Cannot set endTime to null when purchase is active.");
				return new ResponseEntity<>("endTime", HttpStatus.BAD_REQUEST);
			}
			
			Date oldEndTime = purchase.getEndTime();
			purchase.setEndTime(endTime);

			// re-active completed projects
			if (purchase.getStatus().equals(Status.COMPLETED)) {
				purchase.setStatus(Status.ACTIVE);
			}

			if (purchase.getStatus().equals(Status.ACTIVE) && endTime != null) {
				EmailTemplate template = emailTemplateService.findByTemplateType(EmailTemplateType.PURCHASE_POSTPONEMENT);
				
				String emailSubject = template.getTitle();
				emailSubject = emailSubject.replace(EmailTemplateService.PURCHASE_TITLE, purchase.getTitle());
				String emailBody;
				
				emailBody = template.getMessage();
				emailBody = emailBody.replace(EmailTemplateService.PURCHASE_TITLE, purchase.getTitle());
				emailBody = emailBody.replace(EmailTemplateService.PURCHASE_PREVIOUS_ENDTIME, parser.format(oldEndTime));
				emailBody = emailBody.replace(EmailTemplateService.PURCHASE_ENDTIME, parser.format(endTime));
				emailBody = emailBody.replace(EmailTemplateService.CUSTOM_MESSAGE, updatePurchase.getMessage());

				List<PurchaseAnswer> purchaseAnswers = purchaseAnswerDao.findByPurchaseId(purchase.getId());
				for (PurchaseAnswer purchaseAnswer : purchaseAnswers) {
					for (VendorUser vendorUser : purchaseAnswer.getVendorUsers()) {
						try {
							emailService.sendMessage(senderEmailAddress, vendorUser.getEmail(), emailSubject, emailBody);
						}
						catch (Exception ex) {
							log.warn("Error occured while trying to send email", ex);
						}
					}
				}
			}
			break;
		case "winner":
			if (updatePurchase.getField() == null || updatePurchase.getField().isEmpty()) {
				log.warn("Winner cannot be empty and must be a valid value.");
				return new ResponseEntity<>("winner", HttpStatus.BAD_REQUEST);
			}
			else if (!purchase.isDone()) {
				log.warn("User tried to modify Purchase's winner but the project is in " + purchase.getStatus() + " status.");
				return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
			}
			
			try {
				PurchaseVendor winner = answerDao.getByPurchaseAndId(purchase, Long.parseLong(updatePurchase.getField()));
				purchase.setWinner(winner);
			} catch (Exception e) {
				log.warn("User tried to modify Purchase's winner but the PurchesVendor ID( "+updatePurchase.getField()+" ) is wrong.", e);
				return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
			}
			
			break;

		case "winnerPurchaseAnswer":
			if (updatePurchase.getField() == null || updatePurchase.getField().isEmpty()) {
				log.warn("Winner cannot be empty and must be a valid value.");
				return new ResponseEntity<>("winnerPurchaseAnswer", HttpStatus.BAD_REQUEST);
			}
			else if (!purchase.isDone()) {
				log.warn("User tried to modify Purchase's winnerPurchaseAnswer but the project is in " + purchase.getStatus() + " status.");
				return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
			}

			try {
				PurchaseAnswer winnerPurchaseAnswer = purchaseAnswerDao.getByPurchaseAndId(purchase, Long.parseLong(updatePurchase.getField()));
				purchase.setWinnerPurchaseAnswer(winnerPurchaseAnswer);
			} catch (Exception e) {
				log.warn("User tried to modify Purchase's winner but the PurchaseAnswer ID( "+updatePurchase.getField()+" ) is wrong.", e);
				return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
			}

			break;
		case "requirementPriorityEnabled":
			if (!purchase.getStatus().equals(Status.DRAFT)) {
				log.warn("User tried to modify Purchase's requirementPriorityEnabled but the project is in " + purchase.getStatus() + " status.");
				return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
			}
			purchase.setRequirementPriorityEnabled(Boolean.parseBoolean(updatePurchase.getField()));
			break;


		default:
			log.warn("Field '" + fieldName + "' not recognized.");
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

		purchaseService.save(purchase);

		return new ResponseEntity<>(HttpStatus.OK);
	}

	@ResponseBody
	@RequestMapping(path = "purchase/{id}/update/description", method = RequestMethod.POST)
	public ResponseEntity<String> updatePurchaseRequirementDescription(@PathVariable("id") long purchaseId, @RequestBody UpdateRequirementDescriptionForm updateRequirementForm, Locale loc) {
		Purchase purchase = purchaseService.getById(purchaseId);
		if (purchase == null) {
			log.warn("Requested Purchase with ID:"+ purchaseId + " not found.");
			return new ResponseEntity<>("Teknisk fejl - markedsdialog ikke fundet", HttpStatus.BAD_REQUEST);
		}

		if (!purchase.getStatus().equals(Status.DRAFT)) {
			log.warn("User tried to modify requirement description on a purchase that is not in DRAFT state");
			return new ResponseEntity<>("Markedsdialog ikke længere i udkast!", HttpStatus.BAD_REQUEST);
		}

		//Select PurchaseRequirement if the provided requirementId is 0 than find by purchaseRequirementId
		// thats for custom requirements that have requirementId = 0
		PurchaseRequirement requirement = purchase.getRequirements().stream()
				.filter(r -> updateRequirementForm.getRequirementId() != 0
						? r.getRequirementId() == updateRequirementForm.getRequirementId()
						: r.getId() == updateRequirementForm.getPurchaseRequirementId())
				.findAny().orElse(null);
		if (requirement != null) {
			requirement.setDescription(updateRequirementForm.getDescription());
			purchaseService.save(purchase);
		}
		else {
			log.warn("User tried to modify description of a requirement: " + updateRequirementForm.getPurchaseRequirementId() +" that is not found on purchase: " + purchaseId);
			return new ResponseEntity<>("Krav ikke tilvalgt på markedsdialog!", HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<>(HttpStatus.OK);
	}

	@ResponseBody
	@RequestMapping(path = "purchase/{id}/update/status", method = RequestMethod.POST)
	public ResponseEntity<String> updatePurchaseStatus(@PathVariable("id") long purchaseId, @RequestBody UpdatePurchaseStatus updatePurchaseStatus, Locale loc) {
		Purchase purchase = purchaseService.getById(purchaseId);
		if (purchase == null) {
			log.warn("Requested Purchase with ID:"+ purchaseId + " not found.");
			return new ResponseEntity<>("Purchase not found in the database", HttpStatus.BAD_REQUEST);
		}

		try {
			Status.valueOf(updatePurchaseStatus.getStatus());
		} catch (Exception e) {
			log.warn("Bad Status: " + updatePurchaseStatus.getStatus());
			return new ResponseEntity<>("Something went wrong. Purchase status is wrong.", HttpStatus.BAD_REQUEST);
		}

		if (purchase.getStatus().equals(Status.DRAFT) && Status.valueOf(updatePurchaseStatus.getStatus()).equals(Status.ACTIVE) && purchase.getEndTime() == null) {
			log.warn("User tried to Activate Purchase without endTime set.");
			return new ResponseEntity<>("Deadline must be set before activating Purchase Project", HttpStatus.BAD_REQUEST);
		}

		boolean badStatusChange = false;
		Status newStatus = Status.valueOf(updatePurchaseStatus.getStatus());
		switch (newStatus) {
			case ACTIVE:
				if (purchase.getStatus() != Status.DRAFT) {
					badStatusChange = true;
				}
				break;
			case CANCELLED:
				if (purchase.getStatus() != Status.DRAFT && purchase.getStatus() != Status.ACTIVE) {
					badStatusChange = true;
				}
				break;
			default:
				badStatusChange = true;
				break;
		}

		if (badStatusChange) {
			log.warn("Requested status change on purchase with ID:"+ purchaseId + " not allowed. " + purchase.getStatus() + " to " + newStatus + " requested!");
			return new ResponseEntity<>("Purchase not found in the database", HttpStatus.BAD_REQUEST);
		}

		purchase.setStatus(newStatus);

		if (purchase.getStatus().equals(Status.CANCELLED)) {
			EmailTemplate template = emailTemplateService.findByTemplateType(EmailTemplateType.PURCHASE_CANCELLATION);
			
			String emailSubject = template.getTitle();
			emailSubject = emailSubject.replace(EmailTemplateService.PURCHASE_TITLE, purchase.getTitle());
			String emailBody;
			
			emailBody = template.getMessage();
			emailBody = emailBody.replace(EmailTemplateService.PURCHASE_TITLE, purchase.getTitle());
			emailBody = emailBody.replace(EmailTemplateService.CUSTOM_MESSAGE, updatePurchaseStatus.getMessage());

			List<PurchaseAnswer> purchaseAnswers = purchaseAnswerDao.findByPurchaseId(purchase.getId());
			for (PurchaseAnswer purchaseAnswer : purchaseAnswers) {
				for (VendorUser vendorUser : purchaseAnswer.getVendorUsers()) {
					try {
						emailService.sendMessage(senderEmailAddress, vendorUser.getEmail(), emailSubject, emailBody);
					} catch (Exception ex) {
						log.warn("Error occured while trying to send email. ", ex);
					}
				}
			}
		}

		if (purchase.getStatus().equals(Status.CANCELLED)) {
			purchase.setEndTime(new Date());
		}

		purchaseService.save(purchase);

		return new ResponseEntity<>(HttpStatus.OK);
	}

	private static List<Status> getPossibleStatuses(String current) {
		List<Status> possibleStatuses = new ArrayList<>();

		if (current.equals(Status.ACTIVE.getValue())) {
			possibleStatuses.add(Status.CANCELLED);
			possibleStatuses.add(Status.COMPLETED);
		}
		else if (current.equals(Status.DRAFT.getValue())) {
			possibleStatuses.add(Status.ACTIVE);
			possibleStatuses.add(Status.CANCELLED);
		}

		return possibleStatuses;
	}

	private String updateEditModel(Model model, long id) {
		Purchase purchase = purchaseService.getById(id);
		if (purchase == null) {
			log.warn("Cannot edit purchase with id = " + id);
			return "error/notfound";
		}

		if (purchase.getStatus().equals(Status.CANCELLED)) {
			return "redirect:/purchase/list";
		}

		if (!purchase.isQuestionnaireFilledOut()) {
			return "redirect:/purchase/questionnaire/" + purchase.getId();
		}


		final Set<Requirement> requirements = new HashSet<>();

		// fetch all requirements for selected domains
		for (Domain domain: purchase.getDomains()) {
			requirements.addAll(requirementService.getByDomainsContains(domain).stream().distinct().collect(Collectors.toSet()));
		}

		// add all requirements that are available for all Domains
		for (Requirement req : requirementService.getAllRequirements()) {
			if (req.isAvailableForAllDomains() && !requirements.contains(req)) {
				requirements.add(req);
			}
		}
	
		List<PurchaseRequirement> purchaseRequirements = purchase.getRequirements();
		List<Long> purchaseRequirementIds = purchaseRequirements.stream().map(pr -> pr.getRequirementId()).collect(Collectors.toList());

		Map<Long, String> helpTexts = getHelpTexts(purchase);

		// add all requirements that have already been selected
		List<RequirementForm> requirementForms = new ArrayList<>();
		purchaseRequirements.stream().forEach(requirement -> {
			RequirementForm requirementForm = modelMapper.map(requirement, RequirementForm.class);
			requirementForm.setHelpText(helpTexts.get(requirement.getRequirementId()));
			requirementForm.setSelected(true);
			requirementForm.setSubcategory(requirement.getSubcategory() == null ? 0 : requirement.getSubcategory().getId());
			requirementForm.setSubcategoryName(requirement.getSubcategory() == null ? null : requirement.getSubcategory().getName());
			
			// Set requirementType (can be null if custom requirement)
			Requirement originalRequirement = requirements.stream().filter(r -> r.getId() == requirement.getRequirementId()).findAny().orElse(null);
			if (originalRequirement == null || originalRequirement.getCvr().equals(securityUtil.getCvr())) {
				requirementForm.setRequirementType(RequirementType.LOCAL);
			}
			else if (originalRequirement.getCvr().equals(Constants.DEFAULT_CVR)) {
				requirementForm.setRequirementType(RequirementType.GLOBAL);
			}
			else {
				requirementForm.setRequirementType(RequirementType.COMMUNITY);
			}

			if (originalRequirement != null) {
				requirementForm.setOriginalDescription(originalRequirement.getDescription());
			}
			
			requirementForms.add(requirementForm);
		});
		
		// now remove all the entries that have already been selected
		requirements.removeIf(r -> purchaseRequirementIds.contains(r.getId()));

		// add all requirements that have not been selected previously
		requirements.stream().forEach(requirement -> {
			RequirementForm requirementForm = modelMapper.map(requirement, RequirementForm.class);
			requirementForm.setOriginalDescription(requirement.getDescription());

			//Set requirementType
			if (requirement.getCvr().equals(securityUtil.getCvr())) {
				requirementForm.setRequirementType(RequirementType.LOCAL);
			} else if (requirement.getCvr().equals(Constants.DEFAULT_CVR)) {
				requirementForm.setRequirementType(RequirementType.GLOBAL);
			} else {
				requirementForm.setRequirementType(RequirementType.COMMUNITY);
			}
			
			// in case we have local extensions, make sure to copy them here
			RequirementExtension extension = requirementService.getRequirementExtension(requirement);
			if (extension != null) {
				requirementForm.setDescription(requirement.getDescription() + "\n\n" + extension.getDescription());
				requirementForm.setOriginalDescription(requirement.getDescription() + "\n\n" + extension.getDescription());
				requirementForm.setHelpText(extension.getHelpText());
			}

			//Filter out disabled global requirements
			if (extension == null || !extension.isDisableRequirement()) {
				requirementForms.add(requirementForm);
			}
		});

		if (purchase.getStatus() == Status.ACTIVE) {
			requirementForms.removeIf(r -> r.isSelected() == false);
		}

		List<String> userRoles = SecurityUtil.getRoles();

		model.addAttribute("onlyEditorsCanRemoveMinimumRequirements", settingService.getBooleanValueByKey(CustomerSetting.ONLY_EDITORS_CAN_DESELECT_REQUIREMENT));
		model.addAttribute("isEditor", userRoles.contains("ROLE_http://kravmotoren.dk/editor"));
		model.addAttribute("purchase", purchase);//modelMapper.map(purchase, Purchase.class));
		model.addAttribute("domains", purchase.getDomains());
		model.addAttribute("categories", categoryDao.findAll());
		model.addAttribute("requirements", requirementForms);
		model.addAttribute("purchaseid", purchase.getId());
		model.addAttribute("possibleStatuses", getPossibleStatuses(purchase.getStatus().getValue()));

		// no errors
		return null;
	}

	@Deprecated
	@RequestMapping(path = { "purchase/viewanswer/{id}" }, method = RequestMethod.GET)
	public String viewAnswer(Model model, @PathVariable("id") long id) {
		PurchaseVendor answer = answerDao.getById(id);
		if (answer == null) {
			log.warn("Requested Answer with ID:"+id+ " not found.");
			return "error/notfound";
		}

		Purchase purchase = answer.getPurchase();
		if (purchase.getStatus() != Status.COMPLETED) {
			log.warn("Requested Answer with ID:" + id + ", Belonging to a purchase project, which is not completed");
			return "error/notpermitted";
		}
		
		// we use the service to reload the purchase, as it does access control for us ;)
		purchase = purchaseService.getById(purchase.getId());
		if (purchase == null) {
			log.warn("Access to Answer with ID:" + id + " was rejected");
			return "error/notpermitted";
		}

		List<PurchaseVendorAnswer> answerDetails = new ArrayList<PurchaseVendorAnswer>(answer.getDetails());
		
		// we need a list of requirements from purchase
		List<PurchaseRequirement> requirements = new ArrayList<PurchaseRequirement>(purchase.getRequirements());

		// and a list of requirements from answered
		List<PurchaseRequirement> answeredRequirements = answer.getDetails().stream().map(d -> d.getRequirement()).collect(Collectors.toList());
		
		requirements.removeAll(answeredRequirements);
		
		for (PurchaseRequirement purchaseRequirement : requirements) {
			PurchaseVendorAnswer e = new PurchaseVendorAnswer();
			e.setRequirement(purchaseRequirement);

			answerDetails.add(e);
		}

		
		model.addAttribute("answer", answer);
		model.addAttribute("answerDetails", answerDetails);
		model.addAttribute("showPrice", settingService.getBooleanValueByKey(CustomerSetting.ASK_VENDOR_FOR_PRICE));

		return "purchase/viewanswer";
	}

	@RequestMapping(path = { "purchase/viewpurchaseanswer/{purchaseAnswerId}" }, method = RequestMethod.GET)
	public String viewPurchaseAnswer(Model model, @PathVariable("purchaseAnswerId") long purchaseAnswerId) {
		var purchaseAnswer = purchaseService.getPurchaseAnswer(purchaseAnswerId);
		Purchase purchase = purchaseAnswer.getPurchase();
		
		if (purchase.getStatus() != Status.COMPLETED && purchase.getStatus() != Status.ARCHIVED) {
			if (!purchaseAnswer.isDoneAnswering()) {
				log.warn("Requested PurchaseAnswer with ID:" + purchaseAnswerId + ", belonging to a purchase project, which is not completed or archived.");
				return "error/notpermitted";
			}
		}

		model.addAttribute("purchaseAnswer", purchaseAnswer);
		model.addAttribute("showPrice", settingService.getBooleanValueByKey(CustomerSetting.ASK_VENDOR_FOR_PRICE));
		model.addAttribute("showPriority", purchase.isRequirementPriorityEnabled());

		return "purchase/viewpurchaseanswer";
	}

	@RequestMapping(value = "purchase/download/requirements/excel/{purchaseId}", method = RequestMethod.GET)
	public ModelAndView downloadRequirementsAsExcel(HttpServletResponse response, @PathVariable("purchaseId") long purchaseId, Locale loc) {
		Purchase purchase = purchaseService.getById(purchaseId);
		if (purchase == null) {
			log.warn("Cannot find purchase project with id = " + purchaseId);
			return new ModelAndView("redirect:../answer/");
		}

		List<PurchaseRequirement> requirements = purchase.getRequirements();
		Map<Long, String> helpTexts = getHelpTexts(purchase);

		Map<String, Object> model = new HashMap<>();
		model.put("requirements", requirements);
		model.put("messagesBundle", messageSource);
		model.put("locale", loc);
		model.put("helpTexts", helpTexts);
		model.put("requirementPriorityEnabled", purchase.isRequirementPriorityEnabled());

		response.setContentType("application/ms-excel");
		response.setHeader("Content-Disposition", "attachment; filename=\"krav.xls\"");

		return new ModelAndView(new RequirementXlsView(), model);
	}

	@Deprecated
	@RequestMapping(value = "purchase/download/answers/pdf/{answerId}", method = RequestMethod.GET)
	public ModelAndView downloadAnswerAsPdf(HttpServletResponse response, @PathVariable("answerId") long answerId, Locale loc) {
		PurchaseVendor answer = answerDao.getById(answerId);
		if (answer == null) {
			log.warn("Cannot find purchase project with id = " + answerId);
			return new ModelAndView("redirect:../answer/");
		}

		Purchase purchase = answer.getPurchase();
		if (purchase.getStatus() != Status.COMPLETED && purchase.getStatus() != Status.ARCHIVED) {
			log.warn("Requested Answer with ID:" + answerId + ", belonging to a purchase project, which is not completed or archived.");
			return new ModelAndView("error/notpermitted");
		}

		if (!purchase.getCvr().equals(securityUtil.getCvr())) {
			log.warn("Preventing access to answer with id " + answerId + ", belonging to CVR " + purchase.getCvr() + ", for user " + SecurityUtil.getUser());
			return new ModelAndView("error/notpermitted");
		}

		Map<Long, String> helpTexts = getHelpTexts(purchase);

		Map<String, Object> model = new HashMap<>();
		model.put("answers", answer);
		model.put("showPrice", settingService.getBooleanValueByKey(CustomerSetting.ASK_VENDOR_FOR_PRICE));
		model.put("messagesBundle", messageSource);
		model.put("locale", loc);
		model.put("kitosURL", kitosURL);
		model.put("helpTexts", helpTexts);

		response.setContentType("application/pdf");
		response.setHeader("Content-Disposition", "attachment; filename=\"besvarelse.pdf\"");

		return new ModelAndView(new AnswerPdfView(), model);
	}

	@RequestMapping(value = "purchase/download/purchaseanswer/{fileType}/{purchaseAnswerId}", method = RequestMethod.GET)
	public ModelAndView downloadPurchaseAnswerAsPdf(HttpServletResponse response,@PathVariable("fileType") String fileType, @PathVariable("purchaseAnswerId") long purchaseAnswerId, Locale loc) {
		var purchaseAnswer = purchaseService.getPurchaseAnswer(purchaseAnswerId);
		Purchase purchase = purchaseAnswer.getPurchase();

		if (purchase.getStatus() != Status.COMPLETED && purchase.getStatus() != Status.ARCHIVED) {
			if (!purchaseAnswer.isDoneAnswering()) {
				log.warn("Requested PurchaseAnswer with ID:" + purchaseAnswerId + ", belonging to a purchase project, which is not completed or archived.");
				return new ModelAndView("error/notpermitted");
			}
		}

		Map<Long, String> helpTexts = getHelpTexts(purchase);

		Map<String, Object> model = new HashMap<>();
		model.put("purchaseAnswer", purchaseAnswer);
		model.put("showPrice", settingService.getBooleanValueByKey(CustomerSetting.ASK_VENDOR_FOR_PRICE));
		model.put("messagesBundle", messageSource);
		model.put("locale", loc);
		model.put("kitosURL", kitosURL);
		model.put("helpTexts", helpTexts);

		if( fileType.equalsIgnoreCase("pdf"))
		{
			response.setContentType("application/pdf");
			response.setHeader("Content-Disposition", "attachment; filename=\"besvarelse.pdf\"");

			return new ModelAndView(new PurchaseAnswerPdfView(), model);
		}
		if( fileType.equalsIgnoreCase("excel"))
		{
			response.setContentType("application/ms-excel");
			response.setHeader("Content-Disposition", "attachment; filename=\"besvarelse.xls\"");

			return new ModelAndView(new PurchaseAnswerXlsView(), model);

		}
		return new ModelAndView("error/notfound");
	}

	@RequestMapping(value = "purchase/download/all/pdf/{purchaseId}", method = RequestMethod.GET)
	public ModelAndView downloadPurchasePdf(HttpServletResponse response, @PathVariable("purchaseId") long purchaseId, Locale loc) {
		Purchase purchase = purchaseService.getById(purchaseId);
		if (purchase == null) {
			log.warn("Cannot find purchase project with id = " + purchaseId);
			return new ModelAndView("redirect:../answer/");
		}

		if (purchase.getStatus() != Status.COMPLETED && purchase.getStatus() != Status.ARCHIVED) {
			log.warn("Requested Purchase with ID:" + purchaseId + " is not completed or archived.");
			return new ModelAndView("error/notpermitted");
		}

		if (!purchase.getCvr().equals(securityUtil.getCvr())) {
			log.warn("Preventing access to purchase with id " + purchaseId + ", belonging to CVR " + purchase.getCvr() + ", for user " + SecurityUtil.getUser());
			return new ModelAndView("error/notpermitted");
		}


		// *** OLD VendorModel ***
		if( purchaseService.isOldVendorModel(purchase))
		{
			List<PurchaseVendor> answers = answerDao.findAllByPurchase(purchase);
			Map<Long, String> helpTexts = getHelpTexts(purchase);

			Map<String, Object> model = new HashMap<>();
			model.put("purchase", purchase);
			model.put("answers", answers);
			model.put("showPrice", settingService.getBooleanValueByKey(CustomerSetting.ASK_VENDOR_FOR_PRICE));
			model.put("messagesBundle", messageSource);
			model.put("locale", loc);
			model.put("kitosURL", kitosURL);
			model.put("helpTexts", helpTexts);

			response.setContentType("application/pdf");
			response.setHeader("Content-Disposition", "attachment; filename=\"besvarelse.pdf\"");

			return new ModelAndView(new PurchasePdfViewOld(), model);
		}
		// *** NEW VendorModel ***
		else
		{
			List<PurchaseAnswer> purchaseAnswers = purchaseService.getPurchaseAnswers(purchase);
			Map<Long, String> helpTexts = getHelpTexts(purchase);

			Map<String, Object> model = new HashMap<>();
			model.put("purchase", purchase);
			model.put("purchaseAnswers", purchaseAnswers);
			model.put("showPrice", settingService.getBooleanValueByKey(CustomerSetting.ASK_VENDOR_FOR_PRICE));
			model.put("messagesBundle", messageSource);
			model.put("locale", loc);
			model.put("kitosURL", kitosURL);
			model.put("helpTexts", helpTexts);

			response.setContentType("application/pdf");
			response.setHeader("Content-Disposition", "attachment; filename=\"besvarelse.pdf\"");

			return new ModelAndView(new PurchasePdfView(), model);
		}


	}

	@Deprecated
	@RequestMapping(value = "purchase/download/answers/excel/{answerId}", method = RequestMethod.GET)
	public ModelAndView downloadAnswerAsExcel(HttpServletResponse response, @PathVariable("answerId") long answerId, Locale loc) {
		PurchaseVendor answer = answerDao.getById(answerId);
		if (answer == null) {
			log.warn("Cannot find purchase project with id = " + answerId);
			return new ModelAndView("redirect:../answer/");
		}

		Purchase purchase = answer.getPurchase();
		if (purchase.getStatus() != Status.COMPLETED && purchase.getStatus() != Status.ARCHIVED) {
			log.warn("Requested Answer with ID:" + answerId + ", belonging to a purchase project, which is not completed.");
			return new ModelAndView("error/notpermitted");
		}

		if (!purchase.getCvr().equals(securityUtil.getCvr())) {
			log.warn("Preventing access to answer with id " + answerId + ", belonging to CVR " + purchase.getCvr() + ", for user " + SecurityUtil.getUser());
			return new ModelAndView("error/notpermitted");
		}

		Map<Long, String> helpTexts = getHelpTexts(purchase);

		Map<String, Object> model = new HashMap<>();
		model.put("answers", answer);
		model.put("showPrice", settingService.getBooleanValueByKey(CustomerSetting.ASK_VENDOR_FOR_PRICE));
		model.put("messagesBundle", messageSource);
		model.put("locale", loc);
		model.put("kitosURL", kitosURL);
		model.put("helpTexts", helpTexts);
		model.put("requirementPriorityEnabled", purchase.isRequirementPriorityEnabled());

		response.setContentType("application/ms-excel");
		response.setHeader("Content-Disposition", "attachment; filename=\"besvarelse.xls\"");

		return new ModelAndView(new AnswerXlsView(), model);
	}
	
	@GetMapping("/purchase/fragments/answermodal/{id}")
	public String getModalFragment(Model model, @PathVariable Long id) {
		PurchaseRequirementAnswer answer = purchaseRequirementService.getPurchaseRequirementAnswerById(id);
		if (answer == null) {
			return "redirect:/error";
		}
		model.addAttribute("answer", answer);
		return "purchase/fragments/viewpurchaseanswermodal :: answerModal";
	}

	private Map<Long, String> getHelpTexts(Purchase purchase) {
		Map<Long, String> helpTexts = new HashMap<>();

		for (PurchaseRequirement requirement : purchase.getRequirements()) {
			Requirement req = requirementService.getById(requirement.getRequirementId());
			if (req != null) {
				if (req.getCvr().equals(Constants.DEFAULT_CVR)) {
					RequirementExtension extension = requirementService.getRequirementExtensionByRequirementID(requirement.getId());

					if (extension != null) {
						helpTexts.put(extension.getRequirement().getId(),extension.getHelpText());
					}
				} // the cvr check is to take care of "kommunale fællesskaber", which does not support helpText yet
				else if (securityUtil.getCvr().equals(req.getCvr()) && req.getHelpText() != null) {
					helpTexts.put(req.getId(), req.getHelpText());
				}
			}
		}

		return helpTexts;
	}

	private String getEmailDomain(String email) {
		return email.substring(email.indexOf("@") + 1);
	}

	private Set<Requirement> getFavoriteRequirements(Purchase purchase) {
		final Set<Requirement> requirements = new HashSet<>();

		List<Community> communities = communityService.getCommunities(SecurityUtil.getMunicipalityCvr());
		Set<String> communityCvrs = communities.stream().map(c -> c.getCommunityCvr()).collect(Collectors.toSet());

		for (Requirement req : requirementService.getAllRequirements()) {
			if (req.getCvr().equals(Constants.DEFAULT_CVR) || communityCvrs.contains(req.getCvr())) {
				RequirementExtension extension = requirementService.getRequirementExtensionByRequirementID(req.getId());
				if (extension != null && extension.isFavorite()) {
					requirements.add(req);
				}
			}
			else if (req.isFavorite()) {
				requirements.add(req);
			}
		}

		return requirements;
	}
}
