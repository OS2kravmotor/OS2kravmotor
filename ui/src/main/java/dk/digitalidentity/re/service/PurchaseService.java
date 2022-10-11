package dk.digitalidentity.re.service;

import dk.digitalidentity.re.api.dto.AnswerDTO;
import dk.digitalidentity.re.api.dto.PurchaseWinnerDTO;
import dk.digitalidentity.re.dao.*;
import dk.digitalidentity.re.dao.model.*;
import dk.digitalidentity.re.dao.model.enums.CustomerSetting;
import dk.digitalidentity.re.security.SecurityUtil;
import lombok.SneakyThrows;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Service
public class PurchaseService {
    private static final Logger logger = Logger.getLogger(PurchaseService.class);

    @Autowired
    private PurchaseDao purchaseDao;

    @Autowired
    private PurchaseRequirementDao purchaseRequirementDao;

    @Autowired
    private IdentityProviderDao identityProviderDao;

    @Autowired
    private CommunityDao communityDao;

    @Autowired
    private PurchaseVendorService purchaseVendorService;

    @Autowired
    private PurchaseAnswerDao purchaseAnswerDao;

    @Autowired
    private SettingService settingService;

    @Autowired
    private SecurityUtil securityUtil;

    @Autowired
    private RequirementService requirementService;

    public Purchase getById(long id) {
        Purchase purchase = purchaseDao.getById(id);
        if (purchase != null) {
            String cvr = securityUtil.getCvr();

            if (purchase.getCvr().equals(cvr)) {
                return purchase;
            }
        }

        return null;
    }

    public List<Purchase> getAllPurchases() {
        String cvr = securityUtil.getCvr();
        if (cvr != null) {
            return purchaseDao.findByCvr(cvr);
        }

        logger.warn("Tried to access all purchaseAnswers, but no user was logged in");

        return new ArrayList<>();
    }

    public Purchase save(Purchase purchase) {
        // access control on existing purchaseAnswers
        if (purchase.getId() > 0) {
            Purchase purchaseFromDB = getById(purchase.getId());

            if (purchaseFromDB == null) {
                throw new IllegalAccessError("User " + SecurityUtil.getUser() + " is not allowed to modify purchase " + purchase.getId());
            }
        }

        // make sure CVR number is kept when updating an existing purchase
        purchase.setCvr(securityUtil.getCvr());

        return purchaseDao.save(purchase);
    }

    public synchronized void addRequirementsWithMutex(long purchaseId, Set<Requirement> requirements) {
        Purchase purchase = getById(purchaseId);
        if (purchase != null && purchase.getRequirements().size() > 0) {
            logger.warn("Ignoring call to store requirements on purchase project, as requirements already exists!");
            return;
        }

        if (purchase != null) {
            for (Requirement requirement : requirements) {
                PurchaseRequirement purchaseRequirement = new PurchaseRequirement();
                purchaseRequirement.setPurchase(purchase);
                purchaseRequirement.setRequirementId(requirement.getId());
                purchaseRequirement.setImportance(requirement.getImportance());
                purchaseRequirement.setName(requirement.getName());
                purchaseRequirement.setCategory(requirement.getCategory());
                purchaseRequirement.setSubcategory(requirement.getSubcategory());
                purchaseRequirement.setLastChanged(requirement.getLastChanged());

				RequirementExtension extension = requirementService.getRequirementExtension(requirement);
				if (extension != null) {
					purchaseRequirement.setDescription(requirement.getDescription() + "\n\n" + extension.getDescription());
				}
				else {
					purchaseRequirement.setDescription(requirement.getDescription());
				}

				if (extension != null && extension.isDisableRequirement()) {
					continue;
				}
                
                purchase.getRequirements().add(purchaseRequirement);

                purchaseRequirementDao.save(purchaseRequirement);
            }

            purchase.setQuestionnaireFilledOut(true);
            save(purchase);
        }
    }

    public void delete(Purchase purchase) {
        purchaseDao.delete(purchase);
    }

    @SneakyThrows
    public String getCustomerNameByCvr(String cvr) {
        // first try to get as identityprovider (normal customer)
        var identityProvider = identityProviderDao.getByCvr(cvr);
        if (identityProvider != null)
            return identityProvider.getName();
        // then try to get as community
        var community = communityDao.getByCommunityCvr(cvr);
        if (community != null)
            return community.getName();

        throw new Exception("Failed to get customer name with cvr:" + cvr);
    }

    public boolean isOldVendorModel(Purchase purchase) {
        var purchaseVendors = purchaseVendorService.findByPurchase(purchase);
        return !purchaseVendors.isEmpty();
    }

    @SneakyThrows
    public List<PurchaseAnswer> getPurchaseAnswers(Purchase purchase) {
        if (!purchase.getCvr().equalsIgnoreCase(securityUtil.getCvr()))
        {
            throw new Exception("Tried to access purchaseAnswers but cvr did not match logged in user");
        }
        return purchaseAnswerDao.findByPurchaseId(purchase.getId());
    }

    @SneakyThrows
    public PurchaseAnswer getPurchaseAnswer(long purchaseAnswerId) {
        var purchaseAnswer = purchaseAnswerDao.getById(purchaseAnswerId);
        if( purchaseAnswer == null )
        {
            throw new Exception("PurchaseAnswer with id " + purchaseAnswerId + " not found");
        }
        if (!purchaseAnswer.getPurchase().getCvr().equalsIgnoreCase(securityUtil.getCvr()))
        {
            throw new Exception("Tried to access purchaseAnswer with id " + purchaseAnswerId + " but cvr did not match logged in user ");
        }
        return purchaseAnswer;
    }
    
    public PurchaseAnswer savePurchaseAnswer(PurchaseAnswer purchaseAnswer) {
    	return purchaseAnswerDao.save(purchaseAnswer);
    }

    public PurchaseWinnerDTO getWinnerDTO(Purchase purchase) {
        var winnerDTO = new PurchaseWinnerDTO();
        // *** OLD VendorModel ***
        if (purchase.hasWinner() && purchase.getWinner() != null ) {
            PurchaseVendor winner = purchase.getWinner();
            winnerDTO.setName(winner.getName());
            winnerDTO.setAnswers(new ArrayList<>());

            for (PurchaseVendorAnswer answer : winner.getDetails()) {
                AnswerDTO dto = new AnswerDTO();
                dto.setChoice(answer.getChoice());
                dto.setDetail(answer.getDetail());
                dto.setRequirementId(answer.getRequirement().getRequirementId());

                if (settingService.getBooleanValueByKey(CustomerSetting.ASK_VENDOR_FOR_PRICE) == true) {
                    dto.setPrice(answer.getPrice());
                }
                winnerDTO.getAnswers().add(dto);
            }
        }
        // *** OLD VendorModel ***
        if (purchase.hasWinner() && purchase.getWinnerPurchaseAnswer() != null ) {
            var winnerPurchaseAnswer = purchase.getWinnerPurchaseAnswer();
            winnerDTO.setName(winnerPurchaseAnswer.getVendorOrganization().getDomain());
            winnerDTO.setAnswers(new ArrayList<>());

            for (PurchaseRequirementAnswer purchaseRequirementAnswer : winnerPurchaseAnswer.getPurchaseRequirementAnswers()) {
                AnswerDTO dto = new AnswerDTO();
                dto.setChoice(purchaseRequirementAnswer.getChoice());
                dto.setDetail(purchaseRequirementAnswer.getDetail());
                dto.setRequirementId(purchaseRequirementAnswer.getRequirement().getRequirementId());

                if (settingService.getBooleanValueByKey(CustomerSetting.ASK_VENDOR_FOR_PRICE) == true) {
                    dto.setPrice(purchaseRequirementAnswer.getPrice());
                }
                winnerDTO.getAnswers().add(dto);
            }
        }

        return winnerDTO;
    }

	public int countAllPurchasesInLast3Months() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MONTH, -3);
		Date date = cal.getTime();

		return purchaseDao.countByStartTimeAfter(date);
	}
}