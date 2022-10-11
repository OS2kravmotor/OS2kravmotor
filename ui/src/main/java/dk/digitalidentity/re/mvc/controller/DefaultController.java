package dk.digitalidentity.re.mvc.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opensaml.common.SAMLException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.web.WebAttributes;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;

import dk.digitalidentity.re.dao.IdentityProviderDao;
import dk.digitalidentity.re.dao.VendorOrganizationDao;
import dk.digitalidentity.re.dao.model.DownloadLog;
import dk.digitalidentity.re.dao.model.Requirement;
import dk.digitalidentity.re.dao.model.enums.CMSKey;
import dk.digitalidentity.re.dao.model.enums.RequirementType;
import dk.digitalidentity.re.mvc.view.GlobalRequirementXlsView;
import dk.digitalidentity.re.security.SecurityUtil;
import dk.digitalidentity.re.service.CmsMessageService;
import dk.digitalidentity.re.service.DownloadLogService;
import dk.digitalidentity.re.service.PurchaseService;
import dk.digitalidentity.re.service.RequirementService;

@Controller
public class DefaultController implements ErrorController {
	private ErrorAttributes errorAttributes = new DefaultErrorAttributes();

	@Autowired
	private RequirementService requirementService;

	@Autowired
	private MessageSource messageSource;

	@Autowired
	private CmsMessageService cmsMessageService;

	@Autowired
	private PurchaseService purchaseService;

	@Autowired
	private DownloadLogService downloadLogService;

	@Autowired
	private VendorOrganizationDao vendorOrganizationDao;

	@Autowired
	private IdentityProviderDao identityProviderDao;

	@Autowired
	private SecurityUtil securityUtil;

	@Value(value = "${error.showtrace:false}")
	private boolean showStackTrace;

	@RequestMapping(value = { "/", "/ui/" })
	public String index(Model model) {
		int purchases = purchaseService.countAllPurchasesInLast3Months();
		int downloads = downloadLogService.countAllDownloadsInLast3Months();
		int organistions = (int) vendorOrganizationDao.count();
		int municipalities = (int) (identityProviderDao.count() - 1);

		List<Requirement> recentlyModifiedRequirements = requirementService.getRecentlyModified();

		model.addAttribute("purchases", purchases);
		model.addAttribute("downloads", downloads);
		model.addAttribute("organisations", organistions);
		model.addAttribute("municipalities", municipalities);
		model.addAttribute("recentRequirements", recentlyModifiedRequirements);
		model.addAttribute("frontpage", cmsMessageService.getByCmsKey(CMSKey.FRONTPAGE.toString()).getCmsValue());
		return "index";
	}

	@GetMapping("/logo")
	public String logoController() {
		if (!SecurityUtil.isLoggedIn()) {
			return "redirect:/";
		}
		else if ("00000000".equals(securityUtil.getCvr())) {
			return "redirect:/requirement/list";
		}
		else if (SecurityUtil.getRoles().contains("ROLE_http://kravmotoren.dk/vendor")
				|| SecurityUtil.getRoles().contains("ROLE_http://kravmotoren.dk/vendoruser")) {
			return "redirect:/vendor/purchase/list";
		}
		else if (SecurityUtil.getRoles().contains("ROLE_http://kravmotoren.dk/purchaser")
				|| SecurityUtil.getRoles().contains("ROLE_http://kravmotoren.dk/admin")
				|| SecurityUtil.getRoles().contains("ROLE_http://kravmotoren.dk/editor")
				|| SecurityUtil.getRoles().contains("ROLE_http://kravmotoren.dk/global")) {
			return "redirect:/purchase/list";
		}

		return "redirect:/";
	}

	@RequestMapping(value = "/error", produces = "text/html")
	public String errorPage(Model model, HttpServletRequest request) {
		Map<String, Object> body = getErrorAttributes(new ServletWebRequest(request), showStackTrace);

		// deal with SAML errors first
		Object status = body.get("status");
		if (status != null && status instanceof Integer && (Integer) status == 999) {
			Object authException = request.getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);

			// handle the forward case
			if (authException == null && request.getSession() != null) {
				authException = request.getSession().getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
			}

			if (authException != null && authException instanceof Throwable) {
				StringBuilder builder = new StringBuilder();
				Throwable t = (Throwable) authException;

				logThrowable(builder, t, false);
				model.addAttribute("exception", builder.toString());

				if (t.getCause() != null) {
					t = t.getCause();

					// deal with the known causes for this error
					if (t instanceof SAMLException) {
						if (t.getCause() != null && t.getCause() instanceof CredentialsExpiredException) {
							model.addAttribute("cause", "EXPIRED");
						}
						else if (t.getMessage() != null && t.getMessage().contains("Response issue time is either too old or with date in the future")) {
							model.addAttribute("cause", "SKEW");
						}
						else if (t.getMessage() != null && t.getMessage().contains("urn:oasis:names:tc:SAML:2.0:status:Responder")) {
							model.addAttribute("cause", "RESPONDER");
						}
						else {
							model.addAttribute("cause", "UNKNOWN");
						}
					}
					else {
						model.addAttribute("cause", "UNKNOWN");
					}
				}

				return "error/samlerror";
			}
		}

		// default to ordinary error message in case error is not SAML related
		model.addAllAttributes(body);

		return "error/error";
	}

	private void logThrowable(StringBuilder builder, Throwable t, boolean append) {
		StackTraceElement[] stackTraceElements = t.getStackTrace();

		builder.append((append ? "Caused by: " : "") + t.getClass().getName() + ": " + t.getMessage() + "\n");
		for (int i = 0; i < 5 && i < stackTraceElements.length; i++) {
			builder.append("  ... " + stackTraceElements[i].toString() + "\n");
		}

		if (t.getCause() != null) {
			logThrowable(builder, t.getCause(), true);
		}
	}

	@RequestMapping(value = "/error", produces = "application/json")
	public ResponseEntity<Map<String, Object>> errorJSON(HttpServletRequest request) {
		Map<String, Object> body = getErrorAttributes(new ServletWebRequest(request), showStackTrace);

		HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
		try {
			status = HttpStatus.valueOf((int) body.get("status"));
		}
		catch (Exception ex) {
			;
		}

		return new ResponseEntity<>(body, status);
	}

	@RequestMapping(path = "/download/requirements")
	public ModelAndView downloadGlobalRequirements(HttpServletResponse response, Locale loc) {
		List<Requirement> requirements = requirementService.getAllRequirementsOfType(RequirementType.GLOBAL);
		
		boolean loggedIn = SecurityUtil.isLoggedIn();
		if (loggedIn) {
			requirements.addAll(requirementService.getAllRequirementsOfType(RequirementType.LOCAL));
		}

		Map<String, Object> model = new HashMap<>();
		model.put("requirements", requirements);
		model.put("messagesBundle", messageSource);
		model.put("requirementService", requirementService);
		model.put("isEditor", SecurityUtil.getRoles().contains("ROLE_http://kravmotoren.dk/editor"));
		model.put("isLoggedIn", loggedIn);
		model.put("locale", loc);

		response.setContentType("application/ms-excel");
		response.setHeader("Content-Disposition", "attachment; filename=\"krav.xls\"");

		GlobalRequirementXlsView view = new GlobalRequirementXlsView();
		
		downloadLogService.save(new DownloadLog());
		
		return new ModelAndView(view, model);
	}
	
	@Override
	public String getErrorPath() {
		return "/_dummyErrorPage";
	}

	private Map<String, Object> getErrorAttributes(WebRequest request, boolean includeStackTrace) {
		return errorAttributes.getErrorAttributes(request, includeStackTrace);
	}
}
