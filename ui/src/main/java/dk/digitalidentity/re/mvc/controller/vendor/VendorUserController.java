package dk.digitalidentity.re.mvc.controller.vendor;

import dk.digitalidentity.re.dao.VendorUserDao;
import dk.digitalidentity.re.dao.model.PurchaseAnswer;
import dk.digitalidentity.re.dao.model.VendorUser;
import dk.digitalidentity.re.mvc.form.PasswordForm;
import dk.digitalidentity.re.mvc.form.RequestNewPasswordForm;
import dk.digitalidentity.re.mvc.form.VendorUserForm;
import dk.digitalidentity.re.mvc.validator.PasswordFormValidator;
import dk.digitalidentity.re.mvc.validator.RequestNewPasswordFormValidator;
import dk.digitalidentity.re.mvc.validator.VendorUserFormValidator;
import dk.digitalidentity.re.security.RequireVendorAdministratorRole;
import dk.digitalidentity.re.security.RequireVendorUserRole;
import dk.digitalidentity.re.service.MailSenderService;
import dk.digitalidentity.re.service.VendorOrganizationService;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
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
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Locale;

@Controller
@Slf4j
public class VendorUserController {

    @Value("${email.sender}")
    private String senderEmailAddress;

    @Autowired
    private MailSenderService emailService;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    VendorOrganizationService vendorOrganizationService;

    @Autowired
    MailSenderService mailSenderService;

    @Autowired
    VendorUserDao vendorUserDao;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private VendorUserFormValidator vendorUserFormValidator;

    @Autowired
    private PasswordFormValidator passwordFormValidator;

    @Autowired
    private RequestNewPasswordFormValidator requestNewPasswordFormValidator;

    @InitBinder("passwordForm")
    public void initBinderChangePassword(WebDataBinder binder) {
        binder.addValidators(passwordFormValidator);
    }


    @InitBinder("vendorUserForm")
    public void initBinderInviteVendor(WebDataBinder binder) {
        binder.addValidators(vendorUserFormValidator);
    }

    @InitBinder("requestNewPasswordForm")
    public void initBinderRequestNewPassword(WebDataBinder binder) {
        binder.addValidators(requestNewPasswordFormValidator);
    }

    @RequestMapping(path = {"/vendor/user/requestnewpassword"}, method = RequestMethod.GET)
    public String requestNewPassword(Model model) {
        model.addAttribute("requestNewPasswordForm", new RequestNewPasswordForm());
        return "vendor/user/requestnewpassword";
    }

    @RequestMapping(path = {"/vendor/user/requestnewpassword"}, method = RequestMethod.POST)
    public String requestNewPassword(Model model, @ModelAttribute("requestNewPasswordForm") @Valid RequestNewPasswordForm requestNewPasswordForm, BindingResult bindingResult, Locale loc) {
        if (bindingResult.hasErrors()) {
            model.addAttribute(bindingResult.getAllErrors());
            model.addAttribute("requestNewPasswordForm", requestNewPasswordForm);
            return "vendor/user/requestnewpassword";
        }

        var vendorUser = vendorUserDao.getByEmail(requestNewPasswordForm.getEmail());
        if (vendorUser == null) {
            log.warn("Vendoruser should have been found, but was not.");
            return "redirect:/vendor";
        }
        var newPassword = vendorOrganizationService.getRandomPassword();
        var encoder = new BCryptPasswordEncoder();
        vendorUser.setPassword(encoder.encode(newPassword));

        String subject = messageSource.getMessage("email.vendor.newpassword.subject", null, loc);
        String body = messageSource.getMessage("email.vendor.newpassword.body", new String[]{vendorUser.getEmail(), newPassword}, loc);
        try {
            mailSenderService.sendMessage(senderEmailAddress, vendorUser.getEmail(), subject, body);
        } catch (Exception ex) {
            log.warn("Error occured while trying to send email. ", ex);
        }

        return "redirect:/vendor/login?passwordreset";
    }

    @RequireVendorUserRole
    @RequestMapping(path = {"/vendor/user/list"}, method = RequestMethod.GET)
    public String vendorUserList(Model model) {
        model.addAttribute("vendorUsers", vendorOrganizationService.getVendorOrganization().getVendorUsers());
        return "vendor/user/list";
    }

    @RequireVendorUserRole
    @RequestMapping(path = "/vendor/user/{id}", method = RequestMethod.DELETE)
    @ResponseBody
    @RequireVendorAdministratorRole
    public ResponseEntity<String> deleteVendorUser(@PathVariable("id") long id) {
        try {
            vendorOrganizationService.deleteVendorUserById(id);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            log.warn("Cannot delete vendorUser with id = " + id);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(path = "/vendor/user/new", method = RequestMethod.GET)
    @RequireVendorAdministratorRole
    public String newVendorUser(Model model) {
        var vendorUser = new VendorUser();
        var vendorUserForm = modelMapper.map(vendorUser, VendorUserForm.class);

        model.addAttribute("vendorUserForm", vendorUserForm);
        model.addAttribute("organizationPurchaseAnswers", vendorOrganizationService.getVendorOrganization().getPurchaseAnswers());

        return "vendor/user/edit";
    }


    @RequestMapping(path = "/vendor/user/edit/{id}", method = RequestMethod.GET)
    @RequireVendorAdministratorRole
    public String editVendorUser(Model model, @ModelAttribute @PathVariable("id") long id) {
        var vendorUser = vendorOrganizationService.getVendorUserById(id);
        if (vendorUser == null) {
            log.warn("Requested VendorUser with ID:" + id + " not found.");
            return "redirect:/vendororganization/";
        }

        var vendorUserForm = modelMapper.map(vendorUser, VendorUserForm.class);

        vendorUserForm.getPurchaseAnswerIds().stream().forEach(
                purchaseAnswerId -> vendorUserForm.getPurchaseAnswers()
                        .add(vendorOrganizationService.getPurchaseAnswerById(purchaseAnswerId)));

        model.addAttribute("vendorUserForm", vendorUserForm);
        model.addAttribute("organizationPurchaseAnswers", vendorUser.getVendorOrganization().getPurchaseAnswers());

        return "vendor/user/edit";
    }

    @RequestMapping(path = {"/vendor/user/edit"}, method = RequestMethod.POST)
    @RequireVendorAdministratorRole
    public String editVendorUser(Model model, @ModelAttribute("vendorUserForm") @Valid VendorUserForm vendorUserForm, BindingResult bindingResult, Locale loc) {
        if (bindingResult.hasErrors()) {
            model.addAttribute(bindingResult.getAllErrors());
            vendorUserForm.getPurchaseAnswerIds().stream()
                    .forEach(purchaseAnswerId -> vendorUserForm.getPurchaseAnswers()
                            .add(vendorOrganizationService.getPurchaseAnswerById(purchaseAnswerId)));

            model.addAttribute("vendorUserForm", vendorUserForm);
            model.addAttribute("organizationPurchaseAnswers", vendorOrganizationService.getVendorOrganization().getPurchaseAnswers());
            return "vendor/user/edit";
        }

        boolean isNewVendorUser = vendorUserForm.getId() == 0;
        String rawPassword = "";
        VendorUser vendorUser;
        if (isNewVendorUser) {
            vendorUser = new VendorUser();
            vendorUser.setVendorOrganization(vendorOrganizationService.getVendorOrganization());

            rawPassword = vendorOrganizationService.getRandomPassword();
            var encoder = new BCryptPasswordEncoder();
            vendorUser.setPassword(encoder.encode(rawPassword));
        } else {
            vendorUser = vendorOrganizationService.getVendorUserById(vendorUserForm.getId());
            if (vendorUser == null) {
                log.warn("Requested VendorUser with ID:" + vendorUserForm.getId() + " not found");
                return "redirect:/vendor/user/list";
            }
        }

        vendorUser.setAdmin(vendorUserForm.isAdmin());
        vendorUser.setEmail(vendorUserForm.getEmail());

        var organizationPurchaseAnswers = vendorUser.getVendorOrganization().getPurchaseAnswers();

        vendorUser.getPurchaseAnswers().clear();
        // add all selected purchaseAnswerIds to this vendor user, making sure that only purchaseAnswers belonging to the vendor is added
        for (PurchaseAnswer organizationPurchaseAnswer : organizationPurchaseAnswers) {
            for (Long purchaseAnswerId : vendorUserForm.getPurchaseAnswerIds()) {
                if (organizationPurchaseAnswer.getId() == purchaseAnswerId) {
                    vendorUser.getPurchaseAnswers().add(organizationPurchaseAnswer);
                }
            }
        }

        vendorOrganizationService.saveVendorUser(vendorUser);

        if (isNewVendorUser) {
            var emailSubject = messageSource.getMessage("email.vendoradmin.newuser.subject", null, loc);
            var emailBody = messageSource.getMessage("email.vendoradmin.newuser.body.template", new String[]{vendorUser.getEmail(), rawPassword}, loc);
            try {
                emailService.sendMessage(senderEmailAddress, vendorUser.getEmail(), emailSubject, emailBody);
            } catch (Exception ex) {
                log.warn("Error occured while trying to send email. ", ex);
            }
        }
        return "redirect:/vendor/user/list";
    }

    @RequestMapping(path = {"/vendor/user/changepassword"}, method = RequestMethod.GET)
    @RequireVendorUserRole
    public String changePassword(Model model) {
        model.addAttribute("passwordForm", new PasswordForm());
        return "vendor/user/changepassword";
    }

    @RequestMapping(path = {"/vendor/user/changepassword"}, method = RequestMethod.POST)
    @RequireVendorUserRole
    public String changePassword(Model model, @ModelAttribute("passwordForm") @Valid PasswordForm passwordForm, BindingResult bindingResult, Locale loc) {
        if (bindingResult.hasErrors()) {
            model.addAttribute(bindingResult.getAllErrors());
            model.addAttribute("passwordForm", passwordForm);
            return "vendor/user/changepassword";
        }

        vendorOrganizationService.changePassword(passwordForm.getNewPassword());
        model.addAttribute("saved", true);
        return "vendor/user/changepassword";
    }


}
