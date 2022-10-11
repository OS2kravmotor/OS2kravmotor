package dk.digitalidentity.re.mvc.controller.vendor;

import static dk.digitalidentity.re.utility.NullChecker.getValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import dk.digitalidentity.re.dao.PurchaseDao;
import dk.digitalidentity.re.dao.model.Purchase;
import dk.digitalidentity.re.dao.model.PurchaseAnswer;
import dk.digitalidentity.re.dao.model.PurchaseRequirement;
import dk.digitalidentity.re.dao.model.PurchaseRequirementAnswer;
import dk.digitalidentity.re.dao.model.Requirement;
import dk.digitalidentity.re.dao.model.enums.AnswerChoice;
import dk.digitalidentity.re.dao.model.enums.CustomerAnswer;
import dk.digitalidentity.re.dao.model.enums.CustomerSetting;
import dk.digitalidentity.re.dao.model.enums.SolutionType;
import dk.digitalidentity.re.dao.model.enums.Status;
import dk.digitalidentity.re.mvc.dto.OtherPurchaseAnswerDTO;
import dk.digitalidentity.re.mvc.form.VendorPurchaseForm;
import dk.digitalidentity.re.mvc.view.PurchaseAnswerPdfView;
import dk.digitalidentity.re.mvc.view.RequirementXlsView;
import dk.digitalidentity.re.security.RequireVendorUserRole;
import dk.digitalidentity.re.service.ItSystemService;
import dk.digitalidentity.re.service.MailSenderService;
import dk.digitalidentity.re.service.PurchaseService;
import dk.digitalidentity.re.service.RequirementService;
import dk.digitalidentity.re.service.SettingService;
import dk.digitalidentity.re.service.VendorOrganizationService;
import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
@RequireVendorUserRole
public class VendorPurchaseController {

    @Value("${email.sender}")
    private String senderEmailAddress;

	@Value("${kitos.base.url}")
	private String kitosURL;

    @Autowired
    private MailSenderService mailSenderService;

    @Autowired
    private VendorOrganizationService vendorOrganizationService;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private PurchaseService purchaseService;

    @Autowired
    private SettingService settingService;

    @Autowired
    private ItSystemService itSystemService;

    @Autowired
    private PurchaseDao purchaseDao;

    @Autowired
	private RequirementService requirementService;

    @RequestMapping(path = "/vendor/purchase/list", method = RequestMethod.GET)
    public String list(Model model) {
        var purchaseAnswers = vendorOrganizationService.getVendorUser().getPurchaseAnswers();
        var vendorPurchaseForms = new ArrayList<VendorPurchaseForm>();
        for (var purchaseAnswer : purchaseAnswers) {
            var vendorPurchaseForm = new VendorPurchaseForm();
            vendorPurchaseForm.setPurchaseAnswer(purchaseAnswer);
            vendorPurchaseForm.setCustomerName(purchaseService.getCustomerNameByCvr(purchaseAnswer.getPurchase().getCvr()));
            vendorPurchaseForms.add(vendorPurchaseForm);
        }
        model.addAttribute("purchaseForms", vendorPurchaseForms);
        return "vendor/purchase/list";
    }

    @RequestMapping(path = "/vendor/purchase/edit/{purchaseAnswerId}", method = RequestMethod.GET)
    public String editPurchaseAnswer(Model model, @PathVariable("purchaseAnswerId") long purchaseAnswerId, @RequestParam(name = "fromSolutionType", required = false, defaultValue = "false") boolean fromSolutionType) {
        PurchaseAnswer purchaseAnswer = vendorOrganizationService.getPurchaseAnswerById(purchaseAnswerId);

        var otherPurchaseAnswerDTOS = new ArrayList<OtherPurchaseAnswerDTO>();
        var otherPurchaseAnswers = vendorOrganizationService.getVendorUser().getPurchaseAnswers().stream().filter(pa -> pa.getId() != purchaseAnswer.getId()).collect(Collectors.toList());
        for (PurchaseAnswer otherPurchaseAnswer : otherPurchaseAnswers) {
            var otherPurchaseAnswerDTO = new OtherPurchaseAnswerDTO();
            otherPurchaseAnswerDTO.setId(otherPurchaseAnswer.getId());
            otherPurchaseAnswerDTO.setTitle(otherPurchaseAnswer.getPurchase().getTitle());
            otherPurchaseAnswerDTO.setCustomer(purchaseService.getCustomerNameByCvr(otherPurchaseAnswer.getPurchase().getCvr()));
            if (otherPurchaseAnswer.getItSystem() != null) {
                otherPurchaseAnswerDTO.setItSystem(otherPurchaseAnswer.getItSystem().getName() + " (" + otherPurchaseAnswer.getItSystem().getVendor() + ")");
            }
            otherPurchaseAnswerDTOS.add(otherPurchaseAnswerDTO);
        }
        model.addAttribute("purchaseAnswer", purchaseAnswer);
        model.addAttribute("otherPurchaseAnswers", otherPurchaseAnswerDTOS);
        model.addAttribute("itsystems", itSystemService.findAll());
        model.addAttribute("fromSolutionType", fromSolutionType);
        return "vendor/purchase/edit";
    }

    @RequestMapping(path = "/vendor/purchase/purchasemaster/{purchaseAnswerId}", method = RequestMethod.GET)
    public String getPurchaseMaster(Model model, @PathVariable("purchaseAnswerId") long purchaseAnswerId) {
        var purchaseAnswer = vendorOrganizationService.getPurchaseAnswerById(purchaseAnswerId);
        model.addAttribute("purchaseAnswer", purchaseAnswer);
        model.addAttribute("reqAnswered", purchaseAnswer.getPurchaseRequirementAnswers().stream().filter(pa -> pa.isAnswered()).count());
        model.addAttribute("reqTotal", purchaseAnswer.getPurchaseRequirementAnswers().size());
        model.addAttribute("reqDirty", purchaseAnswer.getPurchaseRequirementAnswers().stream().filter(pa -> pa.isDirtyCopy()).count());
        model.addAttribute("askForPrice", settingService.getBooleanValueByKeyAndCvr(CustomerSetting.ASK_VENDOR_FOR_PRICE, purchaseAnswer.getPurchase().getCvr()));
        return "vendor/purchase/purchasemaster";
    }

    @RequestMapping(path = "/vendor/purchase/purchaserequirementlist/{purchaseAnswerId}", method = RequestMethod.GET)
    public String getPurchaseRequirementList(Model model, @PathVariable("purchaseAnswerId") long purchaseAnswerId) {
        var purchaseAnswer = vendorOrganizationService.getPurchaseAnswerById(purchaseAnswerId);
        model.addAttribute("purchaseAnswer", purchaseAnswer);
        model.addAttribute("askForPrice", settingService.getBooleanValueByKeyAndCvr(CustomerSetting.ASK_VENDOR_FOR_PRICE, purchaseAnswer.getPurchase().getCvr()));
        return "vendor/purchase/purchaserequirementlist";
    }

    @ResponseBody
    @RequestMapping(path = "/vendor/purchase/edit/{purchaseAnswerId}/setitsystem/{itSystemId}", method = RequestMethod.POST)
    public ResponseEntity<String> setItSystem(@PathVariable("purchaseAnswerId") long purchaseAnswerId, @PathVariable("itSystemId") long itSystemId) {
        try {
            var purchaseAnswer = vendorOrganizationService.getPurchaseAnswerById(purchaseAnswerId);
            if (purchaseAnswer == null) {
                throw new Exception("Attempt to set it-system on purchase that does not exist");            	
            }
            	
            if (!purchaseAnswer.getPurchase().getStatus().equals(Status.ACTIVE)) {
                throw new Exception("Attempt to set it-system on inactive purchase");
            }
            
            if (purchaseAnswer.isDoneAnswering()) {
                throw new Exception("Attempt to set it-system after done answering was set");
            }
            
            var itSystem = itSystemService.getById(itSystemId);
            purchaseAnswer.setItSystem(itSystem);
            vendorOrganizationService.savePurchaseAnswer(purchaseAnswer);
            return new ResponseEntity<String>(HttpStatus.OK);
        } catch (Exception e) {
            log.error("Could not set it system om purchase.", e);
            return new ResponseEntity<>("Error while processing request.", HttpStatus.BAD_REQUEST);
        }
    }

    @ResponseBody
    @RequestMapping(path = "/vendor/purchase/edit/{purchaseAnswerId}/copyanswers/{otherPurchaseAnswerId}", method = RequestMethod.POST)
    public ResponseEntity<String> copyAnswers(@PathVariable("purchaseAnswerId") long purchaseAnswerId, @PathVariable("otherPurchaseAnswerId") long otherPurchaseAnswerId) {
        try {
            var purchaseAnswer = vendorOrganizationService.getPurchaseAnswerById(purchaseAnswerId);
            if (purchaseAnswer == null) {
                throw new Exception("Attempt to modify purchase that does not exist");            	
            }

            if (!purchaseAnswer.getPurchase().getStatus().equals(Status.ACTIVE)) {
                throw new Exception("Attempt to copy answers to inactive purchase");
            }
            
            if (purchaseAnswer.isDoneAnswering()) {
                throw new Exception("Attempt to copy answers after done answering was set");
            }

            var otherPurchaseAnswer = vendorOrganizationService.getPurchaseAnswerById(otherPurchaseAnswerId);
            // copy answers to requirements that are not already answered and that are not custom requirements (requirementId 0)
            purchaseAnswer.getPurchaseRequirementAnswers().stream().filter(dest -> !dest.isAnswered() && dest.getRequirement().getRequirementId() != 0).forEach(dest -> {
                otherPurchaseAnswer.getPurchaseRequirementAnswers().stream()
                        .filter(source -> source.getRequirement().getRequirementId() == dest.getRequirement().getRequirementId() && source.isAnswered())
                        .findFirst().ifPresent(source -> {
                    dest.setAnswered(true);
                    dest.setChoice(source.getChoice());
                    dest.setDetail(source.getDetail());
                    dest.setPrice(source.getPrice());
                    // if the requirement being copied has been changed the copy should be marked as dirty
                    dest.setDirtyCopy(shouldFlagDirty(dest.getRequirement(), source.getRequirement()));
                });
            });
            vendorOrganizationService.savePurchaseAnswer(purchaseAnswer);
            return new ResponseEntity<String>(HttpStatus.OK);
        } catch (Exception e) {
            log.error("Could not copy answers.", e);
            return new ResponseEntity<>("Error while processing request.", HttpStatus.BAD_REQUEST);
        }
    }

    private boolean shouldFlagDirty(PurchaseRequirement destRequirement, PurchaseRequirement sourceRequirement) {
        if (!getValue(() -> destRequirement.getName(), "").equalsIgnoreCase(getValue(() -> sourceRequirement.getName(), ""))) {
            return true;
        }
        if (!getValue(() -> destRequirement.getDescription(), "").equalsIgnoreCase(getValue(() -> sourceRequirement.getDescription(), ""))) {
            return true;
        }
        if (!getValue(() -> destRequirement.getRationale(), "").equalsIgnoreCase(getValue(() -> sourceRequirement.getRationale(), ""))) {
            return true;
        }
        return false;
    }

    @RequireVendorUserRole
    @ResponseBody
    @RequestMapping(path = "/vendor/purchase/edit/{purchaseAnswerId}/clearitsystem", method = RequestMethod.POST)
    public ResponseEntity<String> clearItSystem(@PathVariable("purchaseAnswerId") long purchaseAnswerId) {
        try {
            var purchaseAnswer = vendorOrganizationService.getPurchaseAnswerById(purchaseAnswerId);
            if (purchaseAnswer == null) {
                throw new Exception("Attempt to modify purchase that does not exist");            	
            }

            if (!purchaseAnswer.getPurchase().getStatus().equals(Status.ACTIVE)) {
                throw new Exception("Attempt to clear it-system on inactive purchase");
            }

            if (purchaseAnswer.isDoneAnswering()) {
                throw new Exception("Attempt to clear it-system after done answering was set");
            }

            purchaseAnswer.setItSystem(null);
            vendorOrganizationService.savePurchaseAnswer(purchaseAnswer);
            return new ResponseEntity<String>(HttpStatus.OK);
        } catch (Exception e) {
            log.error("Could not clear it-system.", e);
            return new ResponseEntity<>("Error while processing request.", HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(path = "vendor/purchase/answer/{purchaseRequirementAnswerId}", method = RequestMethod.GET)
    @RequireVendorUserRole
    public String editPurchaseRequirementAnswer(Model model, @PathVariable("purchaseRequirementAnswerId") long purchaseRequirementAnswerId) {
        var purchaseRequirementAnswer = vendorOrganizationService.getPurchaseRequirementAnswerById(purchaseRequirementAnswerId);
        model.addAttribute("purchaseRequirementAnswer", purchaseRequirementAnswer);
        model.addAttribute("attachments", vendorOrganizationService.getAttachments(purchaseRequirementAnswer));
        model.addAttribute("askForPrice", settingService.getBooleanValueByKeyAndCvr(CustomerSetting.ASK_VENDOR_FOR_PRICE, purchaseRequirementAnswer.getPurchaseAnswer().getPurchase().getCvr()));
        model.addAttribute("next", getNextPurchaseRequirementAnswerId(purchaseRequirementAnswer));
        model.addAttribute("previous", getPreviousPurchaseRequirementAnswerId(purchaseRequirementAnswer));
        
        boolean vendorShouldElaborate = false;
        boolean shouldShowCustomerComment = false;
        if (purchaseRequirementAnswer.getPurchaseAnswer().getPurchase().getStatus().equals(Status.COMPLETED)) {
        	if (purchaseRequirementAnswer.getPurchaseAnswer().isVendorMustElaborate()) {
        		if(purchaseRequirementAnswer.getCustomerAnswer() != null) {
	        		if (!purchaseRequirementAnswer.getCustomerAnswer().equals(CustomerAnswer.ACCEPTED) || !purchaseRequirementAnswer.getCustomerAnswer().equals(CustomerAnswer.ACCEPTED_WITH_COMMENT) || !purchaseRequirementAnswer.getCustomerAnswer().equals(CustomerAnswer.REJECTED)) {
	        			vendorShouldElaborate = true;
	        		}
	        		if (purchaseRequirementAnswer.getCustomerAnswer().equals(CustomerAnswer.ELABORATION_NEEDED)) {
	        			if (purchaseRequirementAnswer.getCustomerComment() != null) {
	        				shouldShowCustomerComment = true;
	        			}
	        		}
	        	} else {
	        		vendorShouldElaborate = true;
	        	}
	        }
        }
        model.addAttribute("vendorShouldElaborate", vendorShouldElaborate);
        model.addAttribute("shouldShowCustomerComment", shouldShowCustomerComment);
        model.addAttribute("requirementPriorityEnabled", purchaseRequirementAnswer.getPurchaseAnswer().getPurchase().isRequirementPriorityEnabled());
        
        return "vendor/purchase/answer";
    }

    @ResponseBody
    @RequireVendorUserRole
    @RequestMapping(path = "/vendor/purchase/answer", method = RequestMethod.POST)
    public ResponseEntity<String> savePurchaseRequirementAnswer(Model model, @ModelAttribute("purchaseRequirementAnswer") @Valid PurchaseRequirementAnswer purchaseRequirementAnswerForm) {
        try {
            var purchaseRequirementAnswer = vendorOrganizationService.getPurchaseRequirementAnswerById(purchaseRequirementAnswerForm.getId());
            boolean vendorShouldElaborate = vendorShouldElaborate(purchaseRequirementAnswer);
            
            if (!purchaseRequirementAnswer.getPurchaseAnswer().getPurchase().getStatus().equals(Status.ACTIVE) && !vendorShouldElaborate) {
                throw new Exception("Attempt to save purchase requirement answer on inactive purchase");
            }
            if (purchaseRequirementAnswer.getPurchaseAnswer().isDoneAnswering()) {
                throw new Exception("Attempt to save purchase requirement answer after done answering was set");
            }
            
            if (!vendorShouldElaborate) {
            	purchaseRequirementAnswer.setAnswered(purchaseRequirementAnswerForm.getChoice() != null);
                purchaseRequirementAnswer.setPrice(purchaseRequirementAnswerForm.getPrice());
                purchaseRequirementAnswer.setDirtyCopy(false);
            } 
            
            purchaseRequirementAnswer.setChoice(purchaseRequirementAnswerForm.getChoice());
            purchaseRequirementAnswer.setDetail(purchaseRequirementAnswerForm.getDetail());
            
            vendorOrganizationService.savePurchaseRequirementAnswer(purchaseRequirementAnswer);
            
            return new ResponseEntity<String>("", HttpStatus.OK);
        } catch (Exception e) {
            log.error("Could not save purchase requirement answer.", e);
            return new ResponseEntity<>("Error while processing request.", HttpStatus.BAD_REQUEST);
        }
    }

    private Long getNextPurchaseRequirementAnswerId(PurchaseRequirementAnswer purchaseRequirementAnswer) {
        var purchaseRequirementAnswers = purchaseRequirementAnswer.getPurchaseAnswer().getPurchaseRequirementAnswers();
        var iterator = purchaseRequirementAnswers.listIterator(purchaseRequirementAnswers.indexOf(purchaseRequirementAnswer) + 1);
        if (iterator.hasNext()) {
        	PurchaseRequirementAnswer answer = iterator.next();
    		return answer.getId();
        }
        return null;
    }

    private Long getPreviousPurchaseRequirementAnswerId(PurchaseRequirementAnswer purchaseRequirementAnswer) {
        var purchaseRequirementAnswers = purchaseRequirementAnswer.getPurchaseAnswer().getPurchaseRequirementAnswers();
        var iterator = purchaseRequirementAnswers.listIterator(purchaseRequirementAnswers.indexOf(purchaseRequirementAnswer));
        if (iterator.hasPrevious()) {
        	PurchaseRequirementAnswer answer = iterator.previous();
    		return answer.getId();
        }
        return null;
    }

    @RequireVendorUserRole
    @RequestMapping(path = "/vendor/purchase/download/requirements/{purchaseAnswerId}", method = RequestMethod.GET)
    public ModelAndView downloadRequirements(@PathVariable("purchaseAnswerId") long purchaseAnswerId, HttpServletResponse response, Locale loc) {
        var purchaseAnswer = vendorOrganizationService.getPurchaseAnswerById(purchaseAnswerId);

        Map<String, Object> model = new HashMap<>();
        model.put("requirements", purchaseAnswer.getPurchase().getRequirements());
        model.put("messagesBundle", messageSource);
        model.put("locale", loc);
        model.put("requirementPriorityEnabled", purchaseAnswer.getPurchase().isRequirementPriorityEnabled());

        response.setContentType("application/ms-excel");
        response.setHeader("Content-Disposition", "attachment; filename=\"krav.xls\"");

        return new ModelAndView(new RequirementXlsView(), model);
    }

	@RequireVendorUserRole
	@RequestMapping(path = "/vendor/purchase/download/answers/{purchaseAnswerId}", method = RequestMethod.GET)
	public ModelAndView downloadAnswers(HttpServletResponse response, @PathVariable("purchaseAnswerId") long purchaseAnswerId, Locale loc) {
		PurchaseAnswer purchaseAnswer = vendorOrganizationService.getPurchaseAnswerById(purchaseAnswerId);

		Map<String, Object> model = new HashMap<>();
		model.put("purchaseAnswer", purchaseAnswer);
		model.put("showPrice", settingService.getBooleanValueByKey(CustomerSetting.ASK_VENDOR_FOR_PRICE));
		model.put("messagesBundle", messageSource);
		model.put("locale", loc);
		model.put("kitosURL", kitosURL);
		model.put("helpTexts", new HashMap<Long, String>());

		response.setContentType("application/pdf");
		response.setHeader("Content-Disposition", "attachment; filename=\"besvarelse.pdf\"");

		return new ModelAndView(new PurchaseAnswerPdfView(), model);
	}

    @RequireVendorUserRole
    @RequestMapping(path = "/vendor/purchase/exit/{purchaseAnswerId}", method = RequestMethod.GET)
    public String exitPurchase(@PathVariable("purchaseAnswerId") long purchaseAnswerId, Locale loc, Model model) {
        var purchaseAnswer = vendorOrganizationService.getPurchaseAnswerById(purchaseAnswerId);

        if (!purchaseAnswer.getPurchase().getStatus().equals(Status.ACTIVE)) {
            log.warn("Attempt to exit purchase with id " + purchaseAnswer.getPurchase().getId() + " that was not Active");
            model.addAttribute("status", purchaseAnswer.getPurchase().getStatus());
            return "vendor/notactive";
        }

        vendorOrganizationService.deletePurchaseAnswer(purchaseAnswer);

        String subject = messageSource.getMessage("email.vendor.exit.title", new String[]{purchaseAnswer.getPurchase().getTitle(), purchaseAnswer.getVendorOrganization().getDomain()}, loc);
        String body = messageSource.getMessage("email.vendor.exit.body", new String[]{purchaseAnswer.getPurchase().getTitle(), purchaseAnswer.getVendorOrganization().getDomain()}, loc);
        try {
            mailSenderService.sendMessage(senderEmailAddress, purchaseAnswer.getPurchase().getEmail(), subject, body);
        } catch (Exception ex) {
            log.warn("Error occured while trying to send email. ", ex);
        }

        return "redirect:/vendor/purchase/list";
    }

    @RequireVendorUserRole
    @RequestMapping(path = "/vendor/purchase/done/{purchaseAnswerId}", method = RequestMethod.GET)
    public String doneAnswering(@PathVariable("purchaseAnswerId") long purchaseAnswerId, Locale loc, Model model) {
        var purchaseAnswer = vendorOrganizationService.getPurchaseAnswerById(purchaseAnswerId);

        if (!purchaseAnswer.getPurchase().getStatus().equals(Status.ACTIVE)) {
            log.warn("Attempt to finish answering purchase with id " + purchaseAnswer.getPurchase().getId() + " that was not Active");
            model.addAttribute("status", purchaseAnswer.getPurchase().getStatus());
            return "vendor/notactive";
        }
        
        if (purchaseAnswer.isDoneAnswering()) {
            log.warn("Attempt to finish answering purchase with id " + purchaseAnswer.getPurchase().getId() + " that was already finished");
            return "redirect:/vendor/purchase/list";
        }

        purchaseAnswer.setDoneAnswering(true);
        vendorOrganizationService.savePurchaseAnswer(purchaseAnswer);

        try {
            String subject = messageSource.getMessage("email.vendor.done.title", new String[]{ purchaseAnswer.getPurchase().getTitle(), purchaseAnswer.getVendorOrganization().getDomain() }, loc);
            String body = messageSource.getMessage("email.vendor.done.body", new String[]{ purchaseAnswer.getPurchase().getTitle(), purchaseAnswer.getVendorOrganization().getDomain() }, loc);

            mailSenderService.sendMessage(senderEmailAddress, purchaseAnswer.getPurchase().getEmail(), subject, body);
        }
        catch (Exception ex) {
            log.warn("Error occured while trying to send email. ", ex);
        }

        Purchase purchase = purchaseAnswer.getPurchase();
        boolean purchaseReadyToFinish = purchase.getPurchaseAnswers().stream().allMatch(pa -> pa.isDoneAnswering());

        if (purchaseReadyToFinish) {
			log.info("Purchase:" + purchase.getTitle() + " has been answered by everyone. Changing status to COMPLETED.");

			String subject = messageSource.getMessage("email.vendor.allanswered.title", new String[] { purchase.getTitle() }, loc);
			String body = messageSource.getMessage("email.vendor.allanswered.body", new String[] { purchase.getTitle() }, loc);

			try {
				mailSenderService.sendMessage(senderEmailAddress, purchase.getEmail(), subject, body);
			}
			catch (Exception ex) {
				log.warn("Error occured while trying to send email. ", ex);
			}

			purchase.setStatus(Status.COMPLETED);
			//Has to be done by dao otherwise we have no access to purchase.
			//Unfortunately our eventlogger logs the last vendor as the one modifying purchase
			purchaseDao.save(purchase);
        }

        return "redirect:/vendor/purchase/list";
    }

	@RequireVendorUserRole
	@GetMapping(path = "/vendor/purchase/solutiontype/{purchaseAnswerId}")
	public String pickSolutionType(@PathVariable("purchaseAnswerId") long purchaseAnswerId, Locale loc, Model model) {
		PurchaseAnswer purchaseAnswer = vendorOrganizationService.getPurchaseAnswerById(purchaseAnswerId);
		if (purchaseAnswer == null) {
			log.warn("PurchaseAnswer with id " + purchaseAnswerId + " does not exist");
			return "redirect:/vendor/purchase/list";
		}

		if (!purchaseAnswer.getPurchase().getStatus().equals(Status.ACTIVE)) {
			log.warn("Attempt to change solution type on purchase with id " + purchaseAnswer.getPurchase().getId() + " that was not Active");
			model.addAttribute("status", purchaseAnswer.getPurchase().getStatus());
			return "vendor/notactive";
		}

		if (purchaseAnswer.getSolutionType() != SolutionType.NOT_SET) {
			log.warn("Attempt to change solution type on purchase with id " + purchaseAnswer.getPurchase().getId() + " but the solution type was already modified.");
			return "redirect:/vendor/purchase/list";
		}

		model.addAttribute("purchaseAnswer", purchaseAnswer);
		model.addAttribute("solutionType", SolutionType.NOT_SET);

		return "vendor/purchase/picksolutiontype";
	}

    @RequireVendorUserRole
    @PostMapping(path = "/vendor/purchase/solutiontype/{purchaseAnswerId}")
    public String setSolutionType(@PathVariable("purchaseAnswerId") long purchaseAnswerId, Locale loc, Model model, @ModelAttribute("solutionType") SolutionType solutionType) {
        var purchaseAnswer = vendorOrganizationService.getPurchaseAnswerById(purchaseAnswerId);
		if (purchaseAnswer == null) {
			log.warn("PurchaseAnswer with id " + purchaseAnswerId + " does not exist");
			return "redirect:/vendor/purchase/list";
		}

		if (!purchaseAnswer.getPurchase().getStatus().equals(Status.ACTIVE)) {
			log.warn("Attempt to exit purchase with id " + purchaseAnswer.getPurchase().getId() + " that was not Active");
			model.addAttribute("status", purchaseAnswer.getPurchase().getStatus());
			return "vendor/notactive";
		}

		if (purchaseAnswer.getSolutionType() != SolutionType.NOT_SET) {
			log.warn("Attempt to change solution type on purchase with id " + purchaseAnswer.getPurchase().getId() + " but the solution type was already modified.");
			return "redirect:/vendor/purchase/list";
		}

		purchaseAnswer.setSolutionType(solutionType);
		List<PurchaseRequirementAnswer> answers = purchaseAnswer.getPurchaseRequirementAnswers();
		for (PurchaseRequirementAnswer purchaseRequirementAnswer : answers) {
			PurchaseRequirement purchaseRequirement = purchaseRequirementAnswer.getRequirement();
			Requirement originalRequirement = requirementService.getById(purchaseRequirement.getRequirementId());

			if (originalRequirement != null && ((solutionType == SolutionType.SAAS && !originalRequirement.isRelevantForSaas()) || (solutionType == SolutionType.ON_PREMISE && !originalRequirement.isRelevantForOnPremise()))) {
				purchaseRequirementAnswer.setAnswered(true);
				purchaseRequirementAnswer.setDirtyCopy(false);
				purchaseRequirementAnswer.setChoice(AnswerChoice.NOT_RELEVANT);
				purchaseRequirementAnswer.setDetail("Kravet er ikke relevant for den tilbudte løsning, da den " + (solutionType == SolutionType.ON_PREMISE ? "driftes on-premise" : "driftes som en SaaS/cloud-løsning"));
			}
		}
		
		vendorOrganizationService.savePurchaseAnswer(purchaseAnswer);

		return "redirect:/vendor/purchase/edit/" + purchaseAnswer.getId() + "?fromSolutionType=true";
    }
    
    private boolean vendorShouldElaborate(PurchaseRequirementAnswer purchaseRequirementAnswer) {
		 boolean vendorShouldElaborate = false;
	     if (purchaseRequirementAnswer.getPurchaseAnswer().getPurchase().getStatus().equals(Status.COMPLETED)) {
	     	if (purchaseRequirementAnswer.getPurchaseAnswer().isVendorMustElaborate()) {
	     		if(purchaseRequirementAnswer.getCustomerAnswer() != null) {
	        		if (!purchaseRequirementAnswer.getCustomerAnswer().equals(CustomerAnswer.ACCEPTED) || !purchaseRequirementAnswer.getCustomerAnswer().equals(CustomerAnswer.ACCEPTED_WITH_COMMENT) || !purchaseRequirementAnswer.getCustomerAnswer().equals(CustomerAnswer.REJECTED)) {
	        			vendorShouldElaborate = true;
	        		}
	        	} else {
	        		vendorShouldElaborate = true;
	        	}
	        }
	     }
	     
	     return vendorShouldElaborate;
    }
}
