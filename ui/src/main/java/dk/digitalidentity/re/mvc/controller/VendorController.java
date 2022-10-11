package dk.digitalidentity.re.mvc.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import dk.digitalidentity.re.dao.PurchaseRequirementDao;
import dk.digitalidentity.re.dao.RequirementExtensionDao;
import dk.digitalidentity.re.dao.model.ItSystem;
import dk.digitalidentity.re.dao.model.Purchase;
import dk.digitalidentity.re.dao.model.PurchaseRequirement;
import dk.digitalidentity.re.dao.model.PurchaseVendor;
import dk.digitalidentity.re.dao.model.PurchaseVendorAnswer;
import dk.digitalidentity.re.dao.model.Requirement;
import dk.digitalidentity.re.dao.model.RequirementExtension;
import dk.digitalidentity.re.dao.model.enums.AnswerChoice;
import dk.digitalidentity.re.dao.model.enums.CMSKey;
import dk.digitalidentity.re.dao.model.enums.CustomerSetting;
import dk.digitalidentity.re.dao.model.enums.Status;
import dk.digitalidentity.re.mvc.form.AnswerPurchaseRequirementForm;
import dk.digitalidentity.re.mvc.form.LoginForm;
import dk.digitalidentity.re.mvc.form.VendorAnswerForm;
import dk.digitalidentity.re.mvc.form.VendorRequirementAnswerForm;
import dk.digitalidentity.re.mvc.view.RequirementXlsView;
import dk.digitalidentity.re.security.RequireVendorRole;
import dk.digitalidentity.re.service.CmsMessageService;
import dk.digitalidentity.re.service.ItSystemService;
import dk.digitalidentity.re.service.MailSenderService;
import dk.digitalidentity.re.service.PurchaseVendorService;
import dk.digitalidentity.re.service.RequirementService;
import dk.digitalidentity.re.service.SettingService;
import lombok.extern.log4j.Log4j;

@Controller
@Log4j
public class VendorController {

    @Value("${email.sender}")
    private String senderEmailAddress;

    @Autowired
    private PurchaseVendorService purchaseVendorService;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private SettingService settingService;

    @Autowired
    private MailSenderService mailSenderService;

    @Autowired
    private RequirementService requirementService;

    @Autowired
    private PurchaseRequirementDao purchaseRequirementDao;

    @Autowired
    private RequirementExtensionDao requirementExtensionDao;

    @Autowired
    private ItSystemService itSystemService;

	@Autowired
	private CmsMessageService cmsMessageService;

    @RequestMapping(path = "/vendor/login")
    public String loginVendor(Model model) {
        model.addAttribute("loginForm", new LoginForm());

        return "vendor/login";
    }

    @RequestMapping(path = "/vendor/help")
    public String help(Model model) {
    	model.addAttribute("helppage", cmsMessageService.getByCmsKey(CMSKey.HELPPAGE.toString()).getCmsValue());
        return "vendor/help";
    }

    @RequireVendorRole
    @RequestMapping(path = "/vendor/exit")
    public String exitPurchase(Locale loc, Model model) {
        PurchaseVendor purchaseVendor = purchaseVendorService.getPurchaseVendor();
        if (purchaseVendor == null) {
            log.warn("Vendor should have assigned purchaseVendor object but it was not found.");
            return "redirect:/vendor";
        }

        Purchase purchase = purchaseVendor.getPurchase();
        if (purchase == null) {
            log.warn("PurchaseVendor:" + purchaseVendor.getUsername() + " does not have Purchase assigned.");
            return "redirect:/vendor";
        }

        if (!purchase.getStatus().equals(Status.ACTIVE)) {
            log.warn("PurchaseVendor:" + purchaseVendor.getUsername() + " rejected access to purchase project in status(DRAFT, CANCELLED).");
            model.addAttribute("status", purchase.getStatus());
            return "vendor/notactive";
        }

        purchaseVendorService.delete(purchaseVendor);

        String subject = messageSource.getMessage("email.vendor.exit.title", new String[]{purchase.getTitle(), purchaseVendor.getName()}, loc);
        String body = messageSource.getMessage("email.vendor.exit.body", new String[]{purchase.getTitle(), purchaseVendor.getName()}, loc);
        try {
            mailSenderService.sendMessage(senderEmailAddress, purchase.getEmail(), subject, body);
        } catch (Exception ex) {
            log.warn("Error occured while trying to send email. ", ex);
        }

        return "redirect:/saml/logout";
    }

    @RequireVendorRole
    @RequestMapping(path = "/vendor/download")
    public ModelAndView downloadRequirements(HttpServletResponse response, Locale loc) {
        PurchaseVendor purchaseVendor = purchaseVendorService.getPurchaseVendor();
        if (purchaseVendor == null) {
            log.warn("Vendor should have assigned purchaseVendor object but it was not found.");
            return new ModelAndView("redirect:/vendor");
        }

        Purchase purchase = purchaseVendor.getPurchase();
        if (purchase == null) {
            log.warn("PurchaseVendor:" + purchaseVendor.getUsername() + " does not have Purchase assigned.");
            return new ModelAndView("redirect:/vendor");
        }

        List<PurchaseRequirement> requirements = purchase.getRequirements();

        Map<String, Object> model = new HashMap<>();
        model.put("requirements", requirements);
        model.put("messagesBundle", messageSource);
        model.put("locale", loc);

        response.setContentType("application/ms-excel");
        response.setHeader("Content-Disposition", "attachment; filename=\"krav.xls\"");

        return new ModelAndView(new RequirementXlsView(), model);
    }

    @RequireVendorRole
    @RequestMapping(path = "/vendor", method = RequestMethod.GET)
    public String vendorRoot(Model model) {
        PurchaseVendor purchaseVendor = purchaseVendorService.getPurchaseVendor();
        if (purchaseVendor == null) {
            log.warn("Vendor should have assigned purchaseVendor object but it was not found.");

            return "error/notfound";
        }

        Purchase purchase = purchaseVendor.getPurchase();
        if (purchase == null) {
            log.warn("PurchaseVendor:" + purchaseVendor.getUsername() + " does not have Purchase assigned.");

            return "error/notfound";
        }

        if (purchase.getStatus().equals(Status.DRAFT)) {
            log.warn("PurchaseVendor:" + purchaseVendor.getUsername() + " rejected access to purchase project in status(DRAFT).");
            model.addAttribute("status", purchase.getStatus());

            return "vendor/notactive";
        }

        // we need a list of requirements from purchase
        List<PurchaseRequirement> requirements = purchase.getRequirements();

        // and a list of requirements from answered
        List<PurchaseRequirement> answeredRequirements = purchaseVendor.getDetails().stream().map(d -> d.getRequirement()).collect(Collectors.toList());

        long reqTotal = 0, reqAnswered = 0;
        VendorAnswerForm vendorAnswerForm = new VendorAnswerForm();
        vendorAnswerForm.setRequirementAnswers(new ArrayList<>());
        for (PurchaseRequirement purchaseRequirement : requirements) {
            VendorRequirementAnswerForm vendorRequirementAnswerForm = new VendorRequirementAnswerForm();
            vendorRequirementAnswerForm.setName(purchaseRequirement.getName());
            vendorRequirementAnswerForm.setImportance(purchaseRequirement.getImportance());
            vendorRequirementAnswerForm.setPurchaseRequirementId(purchaseRequirement.getId());
            vendorRequirementAnswerForm.setDescription(purchaseRequirement.getDescription());
            vendorRequirementAnswerForm.setRationale(purchaseRequirement.getRationale());
            vendorRequirementAnswerForm.setInfoRequirement(purchaseRequirement.isInfoRequirement());
            Requirement requirement = requirementService.getById(purchaseRequirement.getRequirementId());
            RequirementExtension extension = requirementExtensionDao.getByRequirementAndCvr(requirement, purchaseRequirement.getPurchase().getCvr());

            if (extension != null && extension.getAttachments() != null && extension.getAttachments().size() > 0) {
                vendorRequirementAnswerForm.setLocalAttachments(extension.getAttachments());
            } else if (requirement != null) {
                vendorRequirementAnswerForm.setAttachments(requirement.getAttachments());
            }

            boolean isAnswered = answeredRequirements.contains(purchaseRequirement);
            if (isAnswered) {
                Optional<PurchaseVendorAnswer> answerOptional = purchaseVendor.getDetails().stream().filter(d -> d.getRequirement().equals(purchaseRequirement)).findAny();

                if (answerOptional.isPresent()) {
                    reqAnswered++;
                    PurchaseVendorAnswer answer = answerOptional.get();

                    vendorRequirementAnswerForm.setChoice(answer.getChoice());
                    vendorRequirementAnswerForm.setDetail(answer.getDetail());
                    vendorRequirementAnswerForm.setPrice(answer.getPrice());
                }
            }
            reqTotal++;

            vendorAnswerForm.getRequirementAnswers().add(vendorRequirementAnswerForm);
        }

        model.addAttribute("purchaseVendor", purchaseVendor);
        model.addAttribute("purchase", purchase);
        model.addAttribute("status", purchase.getStatus());
        model.addAttribute("vendorAnswerForm", vendorAnswerForm);
        model.addAttribute("reqAnswered", reqAnswered);
        model.addAttribute("reqTotal", reqTotal);
        model.addAttribute("askForPrice", settingService.getBooleanValueByKeyForVendor(CustomerSetting.ASK_VENDOR_FOR_PRICE));
        model.addAttribute("itsystems", itSystemService.findAll());

        return "vendor/vendorpage";
    }


    @RequireVendorRole
    @ResponseBody
    @RequestMapping(path = "/vendor/setitsystem/{id}", method = RequestMethod.POST)
    public ResponseEntity<String> setItSystem(@PathVariable("id") long itSystemId) {
        PurchaseVendor purchaseVendor = purchaseVendorService.getPurchaseVendor();
        if (purchaseVendor == null) {
            log.warn("Vendor should have assigned purchaseVendor object but it was not found.");
            return new ResponseEntity<>("Error while processing request.", HttpStatus.BAD_REQUEST);
        }

        ItSystem itSystem = itSystemService.getById(itSystemId);
        if (itSystem == null) {
            log.warn("Could not find requested ItSystem with ID:" + itSystemId);
            return new ResponseEntity<>("Error while processing request.", HttpStatus.BAD_REQUEST);
        }

        Purchase purchase = purchaseVendor.getPurchase();
        if (purchase == null) {
            log.warn("PurchaseVendor:" + purchaseVendor.getUsername() + " does not have Purchase assigned.");
            return new ResponseEntity<>("Error while processing request.", HttpStatus.BAD_REQUEST);
        }

        if (!purchase.getStatus().equals(Status.ACTIVE)) {
            log.warn("PurchaseVendor:" + purchaseVendor.getUsername() + " rejected access to purchase project in status != ACTIVE.");
            return new ResponseEntity<>("Error while processing request.", HttpStatus.BAD_REQUEST);
        }

        purchaseVendor.setItSystem(itSystem);
        purchaseVendorService.save(purchaseVendor);

        return new ResponseEntity<String>(HttpStatus.OK);
    }

    @RequireVendorRole
    @ResponseBody
    @RequestMapping(path = "/vendor/clearitsystem/", method = RequestMethod.POST)
    public ResponseEntity<String> clearItSystem() {
        PurchaseVendor purchaseVendor = purchaseVendorService.getPurchaseVendor();
        if (purchaseVendor == null) {
            log.warn("Vendor should have assigned purchaseVendor object but it was not found.");
            return new ResponseEntity<>("Error while processing request.", HttpStatus.BAD_REQUEST);
        }

        Purchase purchase = purchaseVendor.getPurchase();
        if (purchase == null) {
            log.warn("PurchaseVendor:" + purchaseVendor.getUsername() + " does not have Purchase assigned.");
            return new ResponseEntity<>("Error while processing request.", HttpStatus.BAD_REQUEST);
        }

        if (!purchase.getStatus().equals(Status.ACTIVE)) {
            log.warn("PurchaseVendor:" + purchaseVendor.getUsername() + " rejected access to purchase project in status != ACTIVE.");
            return new ResponseEntity<>("Error while processing request.", HttpStatus.BAD_REQUEST);
        }

        purchaseVendor.setItSystem(null);
        purchaseVendorService.save(purchaseVendor);

        return new ResponseEntity<String>(HttpStatus.OK);
    }

    @RequireVendorRole
    @ResponseBody
    @RequestMapping(path = "/vendor/answer/{id}", method = RequestMethod.POST)
    public ResponseEntity<String> vendorAnswer(@PathVariable("id") long purchaseRequirementId, @Valid @ModelAttribute("requirement") AnswerPurchaseRequirementForm answerPurchaseRequirement) {
        PurchaseVendor purchaseVendor = purchaseVendorService.getPurchaseVendor();
        if (purchaseVendor == null) {
            log.warn("Vendor should have assigned purchaseVendor object but it was not found.");
            return new ResponseEntity<>("Error while processing request.", HttpStatus.BAD_REQUEST);
        }

        if (!purchaseVendor.getPurchase().getRequirements().stream().anyMatch(r -> r.getId() == purchaseRequirementId)) {
            log.warn("Request Purchase's Requirement not found in this Purchase Project Requirements");
            return new ResponseEntity<>("Error while processing request.", HttpStatus.BAD_REQUEST);
        }

        if (!purchaseVendor.getPurchase().getStatus().equals(Status.ACTIVE)) {
            log.warn("PurchaseVendor:" + purchaseVendor + " rejected access to non-active purchase project.");
            return new ResponseEntity<>("Error while processing request.", HttpStatus.BAD_REQUEST);
        }

        Optional<PurchaseRequirement> optionalRequirement = purchaseVendor.getPurchase().getRequirements().stream().filter(r -> r.getId() == purchaseRequirementId).findAny();
        PurchaseRequirement requirement = optionalRequirement.get();

        AnswerChoice choice;
        try {
            choice = AnswerChoice.valueOf(answerPurchaseRequirement.getChoice());
        } catch (Exception ex) {
            log.error("Unable to parse: " + answerPurchaseRequirement.getChoice() + " into dk.digitalidentity.re.dao.model.enums.AnswerChoice", ex);
            return new ResponseEntity<>("Error while processing request. Unable to process selected AnswerChoice.", HttpStatus.BAD_REQUEST);
        }

        // Check if when YES_AS_AN_ADDON is selected price is given.
        String price = null;
        if (settingService.getBooleanValueByKeyForVendor(CustomerSetting.ASK_VENDOR_FOR_PRICE)) {
            price = answerPurchaseRequirement.getPrice();

            if (choice != null && choice == AnswerChoice.YES_AS_AN_ADDON && (price == null || price.isEmpty())) {
                log.warn(AnswerChoice.YES_AS_AN_ADDON.toString() + " was selected but price is empty or null");
                return new ResponseEntity<>("When YES_AS_AN_ADDON is selected the price cannot by empty.", HttpStatus.BAD_REQUEST);
            }
        }

        // Check if when YES_PARTIALL is selected, detail is given
        String detail = answerPurchaseRequirement.getDetail();
        if (choice != null && choice == AnswerChoice.YES_PARTIALLY && (detail == null || detail.isEmpty())) {
            log.warn(AnswerChoice.YES_PARTIALLY.toString() + " was selected but detail is empty or null");
            return new ResponseEntity<>("When YES_PARTIALLY is selected the detail cannot by empty.", HttpStatus.BAD_REQUEST);
        }

        // Check if details if given when Info Requirement is
        boolean infoRequirement = purchaseRequirementDao.getByIdAndPurchase(purchaseRequirementId, purchaseVendor.getPurchase()).isInfoRequirement();
        if (choice != null && (choice == AnswerChoice.YES || choice == AnswerChoice.YES_AS_AN_ADDON) && (detail == null || detail.isEmpty()) && infoRequirement) {
            log.warn("Info is a requirement but no detail was given");
            return new ResponseEntity<>("When Info is a requirement detail cannot be empty", HttpStatus.BAD_REQUEST);
        }

        // get existing detail or create new one
        Optional<PurchaseVendorAnswer> optionalAnswer = purchaseVendor.getDetails().stream()
                .filter(a -> a.getPurchaseVendor().equals(purchaseVendor) && a.getRequirement().equals(requirement))
                .findAny();

        PurchaseVendorAnswer purchaseVendorAnswer = new PurchaseVendorAnswer();

        if (optionalAnswer.isPresent()) {
            purchaseVendorAnswer = optionalAnswer.get();
        } else {
            purchaseVendorAnswer.setRequirement(requirement);
            purchaseVendorAnswer.setPurchaseVendor(purchaseVendor);
        }

        purchaseVendorAnswer.setChoice(choice);
        purchaseVendorAnswer.setPrice(price);
        purchaseVendorAnswer.setDetail(detail);

        if (!optionalAnswer.isPresent()) {
            purchaseVendor.getDetails().add(purchaseVendorAnswer);
        }

        purchaseVendorService.save(purchaseVendor);

        return new ResponseEntity<>(HttpStatus.OK);
    }
}
