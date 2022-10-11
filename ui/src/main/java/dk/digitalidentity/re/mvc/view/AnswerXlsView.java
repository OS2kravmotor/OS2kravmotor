package dk.digitalidentity.re.mvc.view;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.servlet.view.document.AbstractXlsView;

import dk.digitalidentity.re.dao.model.PurchaseRequirement;
import dk.digitalidentity.re.dao.model.PurchaseVendor;
import dk.digitalidentity.re.dao.model.PurchaseVendorAnswer;

@Deprecated
public class AnswerXlsView extends AbstractXlsView {

	@SuppressWarnings("unchecked")
	@Override
	protected void buildExcelDocument(Map<String, Object> model, Workbook workbook, HttpServletRequest request, HttpServletResponse response) throws Exception {
		ResourceBundleMessageSource messageSource = (ResourceBundleMessageSource) model.get("messagesBundle");
		Locale locale = (Locale) model.get("locale");
		boolean showPrice = (boolean) model.get("showPrice");
		String kitosURL = (String) model.get("kitosURL");
		PurchaseVendor answers = (PurchaseVendor) model.get("answers");
		Map<Long, String> helpTexts = (Map<Long, String>) model.get("helpTexts");
		boolean requirementPriorityEnabled = (boolean) model.get("requirementPriorityEnabled");

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

		// create excel xls sheet
		Sheet sheet = workbook.createSheet(messageSource.getMessage("xls.answer.sheetname", null, locale));

		Font headerFont = workbook.createFont();
		headerFont.setBold(true);
		CellStyle headerStyle = workbook.createCellStyle();
		headerStyle.setFont(headerFont);
		
		// ITSYSTEM
		if (answers.getItSystem() != null) {
			Row itsystemRow = sheet.createRow(1);
			createCell(itsystemRow, 0, itSystemLabel, headerStyle);
			createCell(itsystemRow, 1, answers.getItSystem().getName() + " (" + answers.getItSystem().getVendor() + ")", workbook.createCellStyle());

			// KITOS LINK
			Row kitosRow = sheet.createRow(3);
			createCell(kitosRow, 0, kitosLabel, headerStyle);
			createCell(kitosRow, 1, kitosURL + answers.getItSystem().getSystemId() + "/main", workbook.createCellStyle());
		}

		int rowoffset = (answers.getItSystem() != null) ? 7 : 0;

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
			}
		} else {
			createCell(header, 3, descriptionLabel, headerStyle);
			createCell(header, 4, rationaleLabel, headerStyle);
			createCell(header, 5, helpTextLabel, headerStyle);
			createCell(header, 6, answerLabel, headerStyle);
			createCell(header, 7, answerDetailLabel, headerStyle);
			if (showPrice) {
				createCell(header, 8, priceLabel, headerStyle);
			}
		}
		
		List<PurchaseVendorAnswer> allAnswers = new ArrayList<PurchaseVendorAnswer>(answers.getDetails());

		// we need a list of requirements from purchase
		List<PurchaseRequirement> requirements = new ArrayList<PurchaseRequirement>(answers.getPurchase().getRequirements());
		// and a list of requirements from answered
		List<PurchaseRequirement> answeredRequirements = answers.getDetails().stream().map(d -> d.getRequirement()).collect(Collectors.toList());
		
		requirements.removeAll(answeredRequirements);
		
		for (PurchaseRequirement purchaseRequirement : requirements) {
			PurchaseVendorAnswer e = new PurchaseVendorAnswer();
			e.setRequirement(purchaseRequirement);
			allAnswers.add(e);
		}
		
		allAnswers = allAnswers.stream()
				.sorted((o1, o2) -> Long.compare(o1.getRequirement().getRequirementId(), o2.getRequirement().getRequirementId()))
				.collect(Collectors.toList());

		
		// Create data cells
		int rowCount = rowoffset+1;
		for (PurchaseVendorAnswer answer : allAnswers) {
			Row courseRow = sheet.createRow(rowCount++);
			courseRow.setHeightInPoints((10 * sheet.getDefaultRowHeightInPoints()));

			courseRow.createCell(0).setCellValue(answer.getRequirement().getRequirementId() + "");
			courseRow.createCell(1).setCellValue(answer.getRequirement().getName());
			courseRow.createCell(2).setCellValue(answer.getRequirement().getCategory().getName());
			
			if (requirementPriorityEnabled) {
				courseRow.createCell(3).setCellValue(messageSource.getMessage(answer.getRequirement().getImportance().getValue(), null, locale));
				courseRow.createCell(4).setCellValue(answer.getRequirement().getDescription());
				courseRow.createCell(5).setCellValue(answer.getRequirement().getRationale());

				String helpText = helpTexts.get(answer.getRequirement().getRequirementId());
				courseRow.createCell(6).setCellValue(helpText != null ? helpText : "");

				courseRow.createCell(7).setCellValue(answer.getChoice()!=null?messageSource.getMessage(answer.getChoice().getDisplayName(), null, locale):"");
				courseRow.createCell(8).setCellValue(answer.getDetail());
				if (showPrice) {
					courseRow.createCell(9).setCellValue(answer.getPrice());
				}
			} else {
				courseRow.createCell(3).setCellValue(answer.getRequirement().getDescription());
				courseRow.createCell(4).setCellValue(answer.getRequirement().getRationale());

				String helpText = helpTexts.get(answer.getRequirement().getRequirementId());
				courseRow.createCell(5).setCellValue(helpText != null ? helpText : "");

				courseRow.createCell(6).setCellValue(answer.getChoice()!=null?messageSource.getMessage(answer.getChoice().getDisplayName(), null, locale):"");
				courseRow.createCell(7).setCellValue(answer.getDetail());
				if (showPrice) {
					courseRow.createCell(8).setCellValue(answer.getPrice());
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
			}
		} else {
			sheet.autoSizeColumn(3);
			sheet.autoSizeColumn(4);
			sheet.autoSizeColumn(5);
			sheet.autoSizeColumn(6);
			sheet.autoSizeColumn(7);
			sheet.autoSizeColumn(8);
			if (showPrice) {
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
