package dk.digitalidentity.re.mvc.view;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.view.document.AbstractXlsView;

import dk.digitalidentity.re.dao.model.PurchaseAnswer;

public class PurchaseAnswerXlsView extends AbstractXlsView {

	@SuppressWarnings("unchecked")
	@Override
	protected void buildExcelDocument(Map<String, Object> model, Workbook workbook, HttpServletRequest request, HttpServletResponse response) throws Exception {
		ResourceBundleMessageSource messageSource = (ResourceBundleMessageSource) model.get("messagesBundle");
		Locale locale = (Locale) model.get("locale");
		boolean showPrice = (boolean) model.get("showPrice");
		String kitosURL = (String) model.get("kitosURL");
		var purchaseAnswer = (PurchaseAnswer) model.get("purchaseAnswer");
		Map<Long, String> helpTexts = (Map<Long, String>) model.get("helpTexts");
		boolean requirementPriorityEnabled = purchaseAnswer.getPurchase().isRequirementPriorityEnabled();

		//localized headers
		String requirementIdLabel = messageSource.getMessage("pdf.purchase.view.requirement.id.label", null, locale);
		String requirementLabel = messageSource.getMessage("pdf.purchase.view.requirement.label", null, locale);
		String descriptionLabel = messageSource.getMessage("pdf.purchase.view.description.label", null, locale);
		String rationaleLabel = messageSource.getMessage("pdf.purchase.view.rationale.label", null, locale);
		String answerLabel = messageSource.getMessage("pdf.purchase.view.answer.label", null, locale);
		String categoryLabel = messageSource.getMessage("pdf.purchase.view.category.label", null, locale);
		String priorityLabel = messageSource.getMessage("pdf.purchase.view.priority.label", null, locale);
		String answerDetailLabel = messageSource.getMessage("pdf.purchase.view.answer.detail.label", null, locale);
		String priceLabel = messageSource.getMessage("pdf.purchase.view.price.label", null, locale);
		String itSystemLabel = messageSource.getMessage("pdf.purchase.view.answer.itsystem.label", null, locale);
		String kitosLabel = messageSource.getMessage("pdf.purchase.view.answer.itsystem.kitos", null, locale);
		String fileCreationTimeLabel = messageSource.getMessage("xls.requirement.metadata.creationtime", null, locale);
		String helpTextLabel = messageSource.getMessage("pdf.purchase.view.helpText", null, locale);
		String customerAnswerLabel = messageSource.getMessage("pdf.purchase.view.customer.answer", null, locale);
		String customerCommentLabel = messageSource.getMessage("pdf.purchase.view.customer.comment", null, locale);

		// create excel xls sheet
		Sheet sheet = workbook.createSheet(messageSource.getMessage("xls.answer.sheetname", null, locale));

		Font headerFont = workbook.createFont();
		headerFont.setBold(true);
		CellStyle headerStyle = workbook.createCellStyle();
		headerStyle.setFont(headerFont);
		
		// ITSYSTEM
		if (purchaseAnswer.getItSystem() != null) {
			Row itsystemRow = sheet.createRow(1);
			createCell(itsystemRow, 0, itSystemLabel, headerStyle);
			createCell(itsystemRow, 1, purchaseAnswer.getItSystem().getName() + " (" + purchaseAnswer.getItSystem().getVendor() + ")", workbook.createCellStyle());

			// KITOS LINK
			Row kitosRow = sheet.createRow(3);
			createCell(kitosRow, 0, kitosLabel, headerStyle);
			createCell(kitosRow, 1, kitosURL + purchaseAnswer.getItSystem().getSystemId() + "/main", workbook.createCellStyle());
		}

		int rowoffset = (purchaseAnswer.getItSystem() != null) ? 7 : 0;

		// create header row
		Row header = sheet.createRow(rowoffset+0);
		createCell(header, 0, requirementIdLabel, headerStyle);
		createCell(header, 1, requirementLabel, headerStyle);
		createCell(header, 2, categoryLabel, headerStyle);
		
		if (requirementPriorityEnabled) {
			createCell(header, 3, priorityLabel, headerStyle);
			createCell(header, 4, descriptionLabel, headerStyle);
			createCell(header, 5, rationaleLabel, headerStyle);
			createCell(header, 6, helpTextLabel, headerStyle);
			createCell(header, 7, answerLabel, headerStyle);
			createCell(header, 8, answerDetailLabel, headerStyle);
			if (showPrice) {
				createCell(header, 9, priceLabel, headerStyle);
				createCell(header, 10, customerAnswerLabel, headerStyle);
				createCell(header, 11, customerCommentLabel, headerStyle);
			} else {
				createCell(header, 9, customerAnswerLabel, headerStyle);
				createCell(header, 10, customerCommentLabel, headerStyle);
			}
		} else {
			createCell(header, 3, descriptionLabel, headerStyle);
			createCell(header, 4, rationaleLabel, headerStyle);
			createCell(header, 5, helpTextLabel, headerStyle);
			createCell(header, 6, answerLabel, headerStyle);
			createCell(header, 7, answerDetailLabel, headerStyle);
			if (showPrice) {
				createCell(header, 8, priceLabel, headerStyle);
				createCell(header, 9, customerAnswerLabel, headerStyle);
				createCell(header, 10, customerCommentLabel, headerStyle);
			} else {
				createCell(header, 8, customerAnswerLabel, headerStyle);
				createCell(header, 9, customerCommentLabel, headerStyle);
			}
		}
		
		
		// Create data cells
		int rowCount = rowoffset+1;
		for (var purchaseRequirementAnswer : purchaseAnswer.getPurchaseRequirementAnswers()) {
			Row courseRow = sheet.createRow(rowCount++);
			courseRow.setHeightInPoints((10 * sheet.getDefaultRowHeightInPoints()));

			courseRow.createCell(0).setCellValue(purchaseRequirementAnswer.getRequirement().getRequirementId() + "");
			courseRow.createCell(1).setCellValue(purchaseRequirementAnswer.getRequirement().getName());
			courseRow.createCell(2).setCellValue(purchaseRequirementAnswer.getRequirement().getCategory().getName());
			
			if (requirementPriorityEnabled) {
				courseRow.createCell(3).setCellValue(messageSource.getMessage(purchaseRequirementAnswer.getRequirement().getImportance().getValue(), null, locale));
				courseRow.createCell(4).setCellValue(purchaseRequirementAnswer.getRequirement().getDescription());
				courseRow.createCell(5).setCellValue(purchaseRequirementAnswer.getRequirement().getRationale());

				String helpText = helpTexts.get(purchaseRequirementAnswer.getRequirement().getRequirementId());
				courseRow.createCell(6).setCellValue(helpText != null ? helpText : "");

				courseRow.createCell(7).setCellValue(purchaseRequirementAnswer.getChoice()!=null?messageSource.getMessage(purchaseRequirementAnswer.getChoice().getDisplayName(), null, locale):"");
				courseRow.createCell(8).setCellValue(purchaseRequirementAnswer.getDetail());
				if (showPrice) {
					courseRow.createCell(9).setCellValue(purchaseRequirementAnswer.getPrice());
					if(purchaseRequirementAnswer.getCustomerAnswer() != null) {
						courseRow.createCell(10).setCellValue(messageSource.getMessage(purchaseRequirementAnswer.getCustomerAnswer().getValue(), null, locale));
						if (!StringUtils.isEmpty(purchaseRequirementAnswer.getCustomerComment())) {
							courseRow.createCell(11).setCellValue(purchaseRequirementAnswer.getCustomerComment());
						}
					}
				} else {
					if(purchaseRequirementAnswer.getCustomerAnswer() != null) {
						courseRow.createCell(9).setCellValue(messageSource.getMessage(purchaseRequirementAnswer.getCustomerAnswer().getValue(), null, locale));
						if (!StringUtils.isEmpty(purchaseRequirementAnswer.getCustomerComment())) {
							courseRow.createCell(10).setCellValue(purchaseRequirementAnswer.getCustomerComment());
						}
					}
				}
			} else {
				courseRow.createCell(3).setCellValue(purchaseRequirementAnswer.getRequirement().getDescription());
				courseRow.createCell(4).setCellValue(purchaseRequirementAnswer.getRequirement().getRationale());

				String helpText = helpTexts.get(purchaseRequirementAnswer.getRequirement().getRequirementId());
				courseRow.createCell(5).setCellValue(helpText != null ? helpText : "");

				courseRow.createCell(6).setCellValue(purchaseRequirementAnswer.getChoice()!=null?messageSource.getMessage(purchaseRequirementAnswer.getChoice().getDisplayName(), null, locale):"");
				courseRow.createCell(7).setCellValue(purchaseRequirementAnswer.getDetail());
				if (showPrice) {
					courseRow.createCell(8).setCellValue(purchaseRequirementAnswer.getPrice());
					if(purchaseRequirementAnswer.getCustomerAnswer() != null) {
						courseRow.createCell(9).setCellValue(messageSource.getMessage(purchaseRequirementAnswer.getCustomerAnswer().getValue(), null, locale));
						if (!StringUtils.isEmpty(purchaseRequirementAnswer.getCustomerComment())) {
							courseRow.createCell(10).setCellValue(purchaseRequirementAnswer.getCustomerComment());
						}
					}
				} else {
					if(purchaseRequirementAnswer.getCustomerAnswer() != null) {
						courseRow.createCell(8).setCellValue(messageSource.getMessage(purchaseRequirementAnswer.getCustomerAnswer().getValue(), null, locale));
						if (!StringUtils.isEmpty(purchaseRequirementAnswer.getCustomerComment())) {
							courseRow.createCell(9).setCellValue(purchaseRequirementAnswer.getCustomerComment());
						}
					}
				}
			}
		}

		sheet.autoSizeColumn(1);
		sheet.autoSizeColumn(2);
		
		if (requirementPriorityEnabled) {
			sheet.autoSizeColumn(3);
			sheet.autoSizeColumn(4);
			sheet.autoSizeColumn(5);
			sheet.autoSizeColumn(6);
			sheet.autoSizeColumn(7);
			sheet.autoSizeColumn(8);
			sheet.autoSizeColumn(9);
			if (showPrice) {
				sheet.autoSizeColumn(10);
				sheet.autoSizeColumn(11);
				sheet.autoSizeColumn(12);
			} else {
				sheet.autoSizeColumn(10);
				sheet.autoSizeColumn(11);
			}
		} else {
			sheet.autoSizeColumn(3);
			sheet.autoSizeColumn(4);
			sheet.autoSizeColumn(5);
			sheet.autoSizeColumn(6);
			sheet.autoSizeColumn(7);
			if (showPrice) {
				sheet.autoSizeColumn(8);
				sheet.autoSizeColumn(9);
				sheet.autoSizeColumn(10);
			} else {
				sheet.autoSizeColumn(8);
				sheet.autoSizeColumn(9);
			}
		}

		//Metadata sheet
		Sheet metaDataSheet = workbook.createSheet(messageSource.getMessage("xls.requirement.metadata.sheetname", null, locale));

		Row metaDataHeader = metaDataSheet.createRow(0);
		createCell(metaDataHeader, 0, fileCreationTimeLabel, headerStyle);

		Row metaData = metaDataSheet.createRow(1);
		metaData.createCell(0).setCellValue(new SimpleDateFormat("yyyy/MM/dd - HH:mm").format(new Date()));

		metaDataSheet.autoSizeColumn(0);
	}

	private static void createCell(Row header, int column, String value, CellStyle style) {
		Cell cell = header.createCell(column);
		cell.setCellValue(value);
		cell.setCellStyle(style);
	}
}
