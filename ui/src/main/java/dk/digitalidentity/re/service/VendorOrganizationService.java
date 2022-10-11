package dk.digitalidentity.re.service;

import dk.digitalidentity.re.dao.*;
import dk.digitalidentity.re.dao.model.*;
import dk.digitalidentity.re.security.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class VendorOrganizationService {

    @Autowired
    VendorOrganizationDao vendorOrganizationDao;

    @Autowired
    VendorUserDao vendorUserDao;

    @Autowired
    PurchaseAnswerDao purchaseAnswerDao;

    @Autowired
    PurchaseRequirementAnswerDao purchaseRequirementAnswerDao;

    @Autowired
    PurchaseDao purchaseDao;

    @Autowired
    SecurityUtil securityUtil;

    @Autowired
    RequirementService requirementService;

    @Autowired
    RequirementExtensionDao requirementExtensionDao;

    private static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz!@#$?_";
    private static final String special = "!@#$?_";
    private static SecureRandom rnd = new SecureRandom();
    private static final int length = 10;

    public String getRandomPassword() {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            char c = AB.charAt(rnd.nextInt(AB.length()));
            if (special.indexOf(c) != -1) {
                long count = sb.toString().chars().mapToObj(cx -> (char) cx).collect(Collectors.toList()).stream().filter(cc -> special.indexOf(cc) != -1).count();
                if (count >= 2) {
                    --i;
                    continue;
                }
            }
            sb.append(c);
        }
        return sb.toString();
    }

    public VendorOrganization getVendorOrganization() {
        return vendorOrganizationDao.getById(securityUtil.getVendorOrganizationId());
    }

    public VendorUser getVendorUser() {
        var vendorUserId = securityUtil.getVendorUserId();
        return vendorUserDao.getById(vendorUserId);
    }

    public VendorUser getVendorUserById(long id) {
        var vendorUser = vendorUserDao.getById(id);
        if (vendorUser.getVendorOrganization().getId() != securityUtil.getVendorOrganizationId())
            throw new IllegalAccessError("User " + SecurityUtil.getUser() + " is not allowed to get vendor user with id " + vendorUser.getId());

        return vendorUser;
    }

    public void saveVendorUser(VendorUser vendorUser) {
        if (vendorUser.getVendorOrganization().getId() != securityUtil.getVendorOrganizationId())
            throw new IllegalAccessError("User " + SecurityUtil.getUser() + " is not allowed to save vendor user with id " + vendorUser.getId());

        vendorUserDao.save(vendorUser);
    }

    public void deleteVendorUserById(long id) {
        var vendorUser = vendorUserDao.getById(id);
        if (vendorUser.getVendorOrganization().getId() != securityUtil.getVendorOrganizationId())
            throw new IllegalAccessError("User " + SecurityUtil.getUser() + " is not allowed to delete vendor user with id " + id);

        vendorUserDao.delete(vendorUser);
    }

    public void changePassword(String password) {
        var encoder = new BCryptPasswordEncoder();
        var vendorUser = vendorUserDao.getById(securityUtil.getVendorUserId());
        vendorUser.setPassword(encoder.encode(password));
        vendorUserDao.save(vendorUser);
    }

    public PurchaseAnswer getPurchaseAnswerById(long purchaseAnswerId) {
        var purchaseAnswer = purchaseAnswerDao.getById(purchaseAnswerId);
        CheckAllowedPurchaseAnswer(purchaseAnswer);
        return purchaseAnswer;
    }

    public void savePurchaseAnswer(PurchaseAnswer purchaseAnswer) {
        purchaseAnswerDao.save(purchaseAnswer);
    }

    public PurchaseRequirementAnswer getPurchaseRequirementAnswerById(long purchaseRequirementAnswerId) {
        var purchaseRequirementAnswer = purchaseRequirementAnswerDao.getById(purchaseRequirementAnswerId);
        CheckAllowedPurchaseAnswer(purchaseRequirementAnswer.getPurchaseAnswer());
        return purchaseRequirementAnswer;
    }

    public void savePurchaseRequirementAnswer(PurchaseRequirementAnswer existingAnswer) {
        CheckAllowedPurchaseAnswer(existingAnswer.getPurchaseAnswer());
        purchaseRequirementAnswerDao.save(existingAnswer);
    }

    private void CheckAllowedPurchaseAnswer(PurchaseAnswer purchaseAnswer) {
        if (getVendorUser().getPurchaseAnswers().stream().anyMatch(pa -> pa.getId() == purchaseAnswer.getId())) {
            return;
        } else {
            throw new IllegalAccessError("User " + SecurityUtil.getUser() + " does not have access to PurchaseAnswer with id " + purchaseAnswer.getId());
        }
    }

    public List<? extends IAttachment> getAttachments(PurchaseRequirementAnswer purchaseRequirementAnswer) {
        long requirementId = purchaseRequirementAnswer.getRequirement().getRequirementId();

        // custom purchase requirements have requirementId = 0 and no attachments
		if (requirementId != 0) {
            // local attachments take precedence over normal attachments, so we return those if there are any
            String cvr = purchaseRequirementAnswer.getPurchaseAnswer().getPurchase().getCvr();
            var requirementExtention = requirementExtensionDao.getByRequirementIdAndCvr(requirementId, cvr);
			if (requirementExtention != null && requirementExtention.getAttachments() != null && !requirementExtention.getAttachments().isEmpty()) {
				return requirementExtention.getAttachments();
			}
            
			// return normal attachments if there are any
			var requirement = requirementService.getById(requirementId);
			if (requirement != null && requirement.getAttachments() != null) {
				return requirement.getAttachments();
			}
        }

        // return empty list as default
        return new ArrayList<>();
    }

    public void deletePurchaseAnswer(PurchaseAnswer purchaseAnswer) {
        CheckAllowedPurchaseAnswer(purchaseAnswer);
        purchaseAnswerDao.deleteById(purchaseAnswer.getId());
    }
}
