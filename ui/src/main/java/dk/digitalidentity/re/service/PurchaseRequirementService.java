package dk.digitalidentity.re.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import dk.digitalidentity.re.dao.PurchaseRequirementAnswerDao;
import dk.digitalidentity.re.dao.model.PurchaseRequirement;
import dk.digitalidentity.re.dao.model.PurchaseRequirementAnswer;

@Service
public class PurchaseRequirementService {
	
	@Autowired
    private PurchaseRequirementAnswerDao purchaseRequirementAnswerDao;
	
	@Autowired
	private RequirementService requirementService;
	
	public PurchaseRequirementAnswer getPurchaseRequirementAnswerById(long purchaseRequirementAnswerId) {
        return purchaseRequirementAnswerDao.getById(purchaseRequirementAnswerId);
    }
	
	public PurchaseRequirementAnswer savePurchaseRequirementAnswer(PurchaseRequirementAnswer purchaseRequirementAnswer) {
		return purchaseRequirementAnswerDao.save(purchaseRequirementAnswer);
	}
	
	public boolean hasHelpText(PurchaseRequirement purchaseRequirement) {
		var requirement = requirementService.getById(purchaseRequirement.getRequirementId());
		
		if(requirement != null && requirement.getHelpText() != null) {
			return true;
		}
		
		return false;
	}

	public String getHelpText(PurchaseRequirement purchaseRequirement) {
		var requirement = requirementService.getById(purchaseRequirement.getRequirementId());

		if(requirement != null) {
			return requirement.getHelpText();
		}

		return null;
	}
}
