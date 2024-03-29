package dk.digitalidentity.re.mvc.view;

import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.support.ResourceBundleMessageSource;

import com.lowagie.text.Anchor;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfWriter;

import dk.digitalidentity.re.dao.model.ItSystem;
import dk.digitalidentity.re.dao.model.Purchase;
import dk.digitalidentity.re.dao.model.PurchaseRequirement;
import dk.digitalidentity.re.dao.model.PurchaseVendor;
import dk.digitalidentity.re.dao.model.PurchaseVendorAnswer;

@Deprecated
public class PurchasePdfViewOld extends AbstractITextPdfView {
	private Font regular = new Font(Font.HELVETICA, 10);
	private Font bold = new Font(Font.HELVETICA, 12, Font.BOLD);
	private Font head = new Font(Font.HELVETICA, 24, Font.BOLD);
	private Font subhead = new Font(Font.HELVETICA, 16, Font.ITALIC);
	private Font label = new Font(Font.HELVETICA, 16, Font.BOLD);
	private Font detail = new Font(Font.HELVETICA, 16);
	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd - HH:mm");

	@SuppressWarnings("unchecked")
	@Override
	protected void buildPdfDocument(Map<String, Object> model, Document document, PdfWriter writer, HttpServletRequest request, HttpServletResponse response) throws Exception {
		float marginTopAndBottom = 15;
		float marginRightAndLeft = 10;

		ResourceBundleMessageSource messageSource = (ResourceBundleMessageSource) model.get("messagesBundle");
		Locale locale = (Locale) model.get("locale");
		boolean showPrice = (boolean) model.get("showPrice");
		List<PurchaseVendor> purchaseVendors = (List<PurchaseVendor>) model.get("answers");
		Purchase purchase = (Purchase) model.get("purchase");
		String kitosURL = (String) model.get("kitosURL");
		Map<Long, String> helpTexts = (Map<Long, String>) model.get("helpTexts");
		boolean requirementPriorityEnabled = purchase.isRequirementPriorityEnabled();

		String projectHeadlineMsg = messageSource.getMessage("pdf.purchase.all.projectheadline", null, locale);
		String answersHeadlineMsg = messageSource.getMessage("pdf.purchase.all.answersheadline", null, locale);
		String titleMsg = messageSource.getMessage("pdf.purchase.all.title", null, locale);
		String descriptionMsg = messageSource.getMessage("pdf.purchase.all.description", null, locale);
		String emailMsg = messageSource.getMessage("pdf.purchase.all.email", null, locale);
		String deadlineMsg = messageSource.getMessage("pdf.purchase.all.deadline", null, locale);
		String domainsMsg = messageSource.getMessage("pdf.purchase.all.domains", null, locale);

		document.setPageSize(PageSize.A3.rotate());
		document.setMargins(marginRightAndLeft, marginRightAndLeft, marginTopAndBottom, marginTopAndBottom);

		PdfWriter.getInstance(document, response.getOutputStream());

		document.open();

		// Purchase details

		Paragraph projectHeadline = new Paragraph(projectHeadlineMsg, head);
		projectHeadline.setSpacingAfter(15f);
		document.add(projectHeadline);

		Paragraph pTitle = createParagraphWithTwoChunks(titleMsg + ": ", purchase.getTitle());
		document.add(pTitle);

		Paragraph pDescriptionLabel = new Paragraph(descriptionMsg + ": ", label);
		Paragraph pDescriptionValue = new Paragraph(purchase.getDescription(), detail);
		document.add(pDescriptionLabel);
		document.add(pDescriptionValue);

		Paragraph pEmail = createParagraphWithTwoChunks(emailMsg + ": ", purchase.getEmail());
		document.add(pEmail);

		Paragraph pDeadline = createParagraphWithTwoChunks(deadlineMsg + ": ", dateFormat.format(purchase.getEndTime()));
		document.add(pDeadline);

		Paragraph pDomains = createParagraphWithTwoChunks(domainsMsg + ": ", String.join(", ", purchase.getDomains().stream().map(d -> d.getName()).collect(Collectors.toList())));
		document.add(pDomains);

		// All answers
		Paragraph answersHeadline = new Paragraph(answersHeadlineMsg, head);
		answersHeadline.setSpacingBefore(15f);
		answersHeadline.setSpacingAfter(15f);
		document.add(answersHeadline);

		for (PurchaseVendor vendor : purchaseVendors) {
			addPurchaseVendorToDocument(document, messageSource, locale, showPrice, vendor, kitosURL, helpTexts, requirementPriorityEnabled);
		}

		document.close();
	}

	private Paragraph createParagraphWithTwoChunks(String l, String value) {
		return createParagraphWithTwoChunks(l, value, label, detail);
	}

	private Paragraph createParagraphWithTwoChunks(String l, String value, Font font1, Font font2) {
		Phrase phrase = new Phrase();
		phrase.add(new Chunk(l, font1));
		phrase.add(new Chunk(value, font2));

		Paragraph paragraph = new Paragraph(phrase);
		paragraph.setSpacingBefore(5f);
		paragraph.setSpacingAfter(5f);

		return paragraph;
	}
	
	private void addItSystemParagraphElement(Document document, String itSystemLabel, String kitosLabel, ItSystem itSystem, String kitosURL) throws Exception {
		// NAME
		Paragraph pItSystemLabel = new Paragraph(itSystemLabel + ": ", bold);
		Paragraph pItSystemValue = new Paragraph(itSystem.getName() + " (" + itSystem.getVendor() + ")", regular);

		pItSystemValue.setSpacingAfter(10f);

		document.add(pItSystemLabel);
		document.add(pItSystemValue);
		// KITOS LINK
		Paragraph pKITOSLabel = new Paragraph(kitosLabel + ": ", bold);
		Anchor pKITOSValue = new Anchor(kitosURL, new Font(Font.HELVETICA, 10, Font.NORMAL, new Color(0, 0, 238)));
		pKITOSValue.setName(kitosURL);
		pKITOSValue.setReference(kitosURL);

		document.add(pKITOSLabel);
		document.add(pKITOSValue);

		Paragraph dummySpacingParagraph = new Paragraph();
		dummySpacingParagraph.setSpacingAfter(15f);

		document.add(dummySpacingParagraph);
	}

	private void addPurchaseVendorToDocument(Document document, ResourceBundleMessageSource messageSource, Locale locale, boolean showPrice, PurchaseVendor vendor, String kitosURL, Map<Long, String> helpTexts, boolean requirementPriorityEnabled) throws DocumentException, Exception {

		String headlinePrefix = messageSource.getMessage("pdf.purchase.view.headlinePrefix", null, locale);
		String requirementIdLabel = messageSource.getMessage("pdf.purchase.view.requirement.id.label", null, locale);
		String requirementLabel = messageSource.getMessage("pdf.purchase.view.requirement.label", null, locale);
		String descriptionLabel = messageSource.getMessage("pdf.purchase.view.description.label", null, locale);
		String rationaleLabel = messageSource.getMessage("pdf.purchase.view.rationale.label", null, locale);
		String answerLabel = messageSource.getMessage("pdf.purchase.view.answer.label", null, locale);
		String priceLabel = messageSource.getMessage("pdf.purchase.view.price.label", null, locale);
		String categoryLabel = messageSource.getMessage("pdf.purchase.view.category.label", null, locale);
		String priorityLabel = messageSource.getMessage("pdf.purchase.view.priority.label", null, locale);
		String answerDetailLabel = messageSource.getMessage("pdf.purchase.view.answer.detail.label", null, locale);
		String itSystemLabel = messageSource.getMessage("pdf.purchase.view.answer.itsystem.label", null, locale);
		String kitosLabel = messageSource.getMessage("pdf.purchase.view.answer.itsystem.kitos", null, locale);
		String helpTextLabel = messageSource.getMessage("pdf.purchase.view.helpText", null, locale);

		Paragraph headline = new Paragraph(headlinePrefix + ": " + vendor.getName(), subhead);

		headline.setSpacingBefore(15f);
		headline.setSpacingAfter(15f);

		document.add(headline);

		if (vendor.getItSystem() != null) {
			addItSystemParagraphElement(document, itSystemLabel, kitosLabel, vendor.getItSystem(), kitosURL + vendor.getItSystem().getSystemId() + "/main");
		}

		List<PurchaseVendorAnswer> allAnswers = new ArrayList<PurchaseVendorAnswer>(vendor.getDetails());

		// we need a list of requirements from purchase
		List<PurchaseRequirement> requirements = new ArrayList<PurchaseRequirement>(vendor.getPurchase().getRequirements());
		// and a list of requirements from answered
		List<PurchaseRequirement> answeredRequirements = vendor.getDetails().stream().map(d -> d.getRequirement()).collect(Collectors.toList());
		
		requirements.removeAll(answeredRequirements);
		
		for (PurchaseRequirement purchaseRequirement : requirements) {
			PurchaseVendorAnswer e = new PurchaseVendorAnswer();
			e.setRequirement(purchaseRequirement);
			allAnswers.add(e);
		}
		
		allAnswers = allAnswers.stream()
				.sorted((o1, o2) -> Long.compare(o1.getRequirement().getRequirementId(), o2.getRequirement().getRequirementId()))
				.collect(Collectors.toList());

		for (PurchaseVendorAnswer answer : allAnswers) {
			PurchaseRequirement requirement = answer.getRequirement();

			document.add(createParagraphWithTwoChunks(requirementIdLabel + ": ", Long.toString(requirement.getRequirementId()), bold, regular));

			document.add(createParagraphWithTwoChunks(requirementLabel + ": ", requirement.getName(), bold, regular));

			document.add(new Paragraph(descriptionLabel + ": ", bold));
			document.add(new Paragraph(requirement.getDescription(), regular));

			document.add(createParagraphWithTwoChunks(rationaleLabel + ": ", requirement.getRationale() != null ? requirement.getRationale() : "", bold, regular));

			document.add(createParagraphWithTwoChunks(categoryLabel + ": ", requirement.getCategory().getName(), bold, regular));

			if (requirementPriorityEnabled) {
				String importanceValue = messageSource.getMessage(requirement.getImportance().getValue(), null, locale);
				Paragraph pImportance = createParagraphWithTwoChunks(priorityLabel + ": ", importanceValue, bold, regular);
				document.add(pImportance);
			}

			String helpText = helpTexts.get(requirement.getRequirementId());
			if (helpText != null) {
				document.add(createParagraphWithTwoChunks(helpTextLabel + ": ", helpText, bold, regular));
			}

			String answerValue = "";
			if (answer.getChoice() != null) {
				answerValue = messageSource.getMessage(answer.getChoice().getDisplayName(), null, locale);
			}
			
			Paragraph pAnswer = createParagraphWithTwoChunks(answerLabel + ": ", answerValue, bold, regular);
			document.add(pAnswer);

			document.add(createParagraphWithTwoChunks(answerDetailLabel + ": ", (answer.getDetail() != null) ? answer.getDetail() : "", bold, regular));

			if (showPrice) {
				String price = "";
				if (answer.getPrice() != null) {
					price = answer.getPrice();
				}

				document.add(createParagraphWithTwoChunks(priceLabel + ": ", price, bold, regular));
			}

			document.newPage();
		}
	}
}
