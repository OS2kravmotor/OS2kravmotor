package dk.digitalidentity.re.mvc.view;

import java.awt.Color;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.util.StringUtils;

import com.lowagie.text.Anchor;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;

import dk.digitalidentity.re.dao.model.ItSystem;
import dk.digitalidentity.re.dao.model.Purchase;
import dk.digitalidentity.re.dao.model.PurchaseAnswer;
import dk.digitalidentity.re.dao.model.PurchaseRequirement;

public class PurchaseAnswerPdfView extends AbstractITextPdfView {
	private Font regular = new Font(Font.HELVETICA, 10);
	private Font bold = new Font(Font.HELVETICA, 12, Font.BOLD);
	private Font head = new Font(Font.HELVETICA, 24, Font.BOLD);
	private Font subhead = new Font(Font.HELVETICA, 16, Font.ITALIC);

	@SuppressWarnings("unchecked")
	@Override
	protected void buildPdfDocument(Map<String, Object> model, Document document, PdfWriter writer, HttpServletRequest request, HttpServletResponse response) throws Exception {
		float marginTopAndBottom = 15;
		float marginRightAndLeft = 10;

		ResourceBundleMessageSource messageSource = (ResourceBundleMessageSource) model.get("messagesBundle");
		Locale locale = (Locale) model.get("locale");
		boolean showPrice = (boolean) model.get("showPrice");
		String kitosURL = (String) model.get("kitosURL");
		PurchaseAnswer purchaseAnswer = (PurchaseAnswer) model.get("purchaseAnswer");
		Map<Long, String> helpTexts = (Map<Long, String>) model.get("helpTexts");
		Purchase purchase = purchaseAnswer.getPurchase();
		boolean requirementPriorityEnabled = purchase.isRequirementPriorityEnabled();

		String headlinePrefix = messageSource.getMessage("pdf.purchase.view.headlinePrefix", null, locale);
		String sublinePrefix = messageSource.getMessage("pdf.purchase.view.sublinePrefix", null, locale);
		String projectDescriptionLabel = messageSource.getMessage("pdf.purchase.view.project.description.label", null, locale);
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
		String customerAnswerLabel = messageSource.getMessage("pdf.purchase.view.customer.answer", null, locale);
		String customerCommentLabel = messageSource.getMessage("pdf.purchase.view.customer.comment", null, locale);

		document.setPageSize(PageSize.A3.rotate());
		document.setMargins(marginRightAndLeft, marginRightAndLeft, marginTopAndBottom, marginTopAndBottom);

		PdfWriter.getInstance(document, response.getOutputStream());

		document.open();

		Paragraph headline = new Paragraph(headlinePrefix + ": " + purchaseAnswer.getVendorOrganization().getDomain(), head);
		Paragraph subline = new Paragraph(sublinePrefix + " '" + purchase.getTitle() + "'", subhead);

		subline.setSpacingAfter(15f);

		document.add(headline);
		document.add(subline);

		if (purchaseAnswer.getItSystem() != null) {
			addItSystemParagraphElement(document, itSystemLabel, kitosLabel, purchaseAnswer.getItSystem(), kitosURL + purchaseAnswer.getItSystem().getSystemId() + "/main");
		}

		addListParagraphElement(document, projectDescriptionLabel, purchase.getDescription());

		for (var purchaseRequirementAnswer : purchaseAnswer.getPurchaseRequirementAnswers()) {
			PurchaseRequirement requirement = purchaseRequirementAnswer.getRequirement();

			addListParagraphElement(document,requirementIdLabel, Long.toString(requirement.getRequirementId()));

			addListParagraphElement(document, requirementLabel,requirement.getName());

			addListParagraphElement(document,descriptionLabel, requirement.getDescription());

			addListParagraphElement(document, rationaleLabel, requirement.getRationale());

			addListParagraphElement(document, categoryLabel, requirement.getCategory().getName());

			if (requirementPriorityEnabled) {
				String importanceValue = messageSource.getMessage(requirement.getImportance().getValue(), null, locale);
				addListParagraphElement(document, priorityLabel, importanceValue);
			}

			String helpText = helpTexts.get(requirement.getRequirementId());
			if (helpText != null) {
				addListParagraphElement(document, helpTextLabel, helpText);
			}

			// kundens uddybende spørgsmål, hvis nogen
			if (purchaseRequirementAnswer.getCustomerAnswer() != null && !StringUtils.isEmpty(purchaseRequirementAnswer.getCustomerAnswer().getValue())) {
				String answerValue = messageSource.getMessage(purchaseRequirementAnswer.getCustomerAnswer().getValue(), null, locale);
				addListParagraphElement(document, customerAnswerLabel, answerValue);
			}
			
			if (!StringUtils.isEmpty(purchaseRequirementAnswer.getCustomerComment())) {
				addListParagraphElement(document, customerCommentLabel, purchaseRequirementAnswer.getCustomerComment());
			}
			
			// leverandørens svar
			String choiceValue = "";
			if (purchaseRequirementAnswer.getChoice() != null) {
				choiceValue = messageSource.getMessage(purchaseRequirementAnswer.getChoice().getDisplayName(), null, locale);
			}
			addListParagraphElement(document, answerLabel, choiceValue);

			addListParagraphElement(document, answerDetailLabel, purchaseRequirementAnswer.getDetail());

			if (showPrice) {
				String price = "";
				if (purchaseRequirementAnswer.getPrice() != null) {
					price = purchaseRequirementAnswer.getPrice();
				}

				addListParagraphElement(document, priceLabel, price);
			}
			
			document.newPage();
		}

		document.close();
	}

	private void addItSystemParagraphElement(Document document, String itSystemLabel, String kitosLabel, ItSystem itSystem, String kitosURL) throws Exception {
		Paragraph pItSystemLabel = new Paragraph(itSystemLabel + ": ", bold);
		Paragraph pItSystemValue = new Paragraph(itSystem.getName() + " (" + itSystem.getVendor() + ")", regular);

		pItSystemValue.setSpacingAfter(10f);

		document.add(pItSystemLabel);
		document.add(pItSystemValue);

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

	private void addListParagraphElement(Document document, String label, String value) throws Exception {
		Paragraph pLabel = new Paragraph(label + ": ", bold);
		Paragraph pValue = new Paragraph(value, regular);

		pValue.setSpacingAfter(10f);

		document.add(pLabel);
		document.add(pValue);
	}
}
