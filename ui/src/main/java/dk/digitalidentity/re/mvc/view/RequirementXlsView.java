package dk.digitalidentity.re.mvc.view;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import dk.digitalidentity.re.dao.model.PurchaseRequirement;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.servlet.view.document.AbstractXlsView;

public class RequirementXlsView extends AbstractXlsView {

	@SuppressWarnings("unchecked")
	@Override
	protected void buildExcelDocument(Map<String, Object> model, Workbook workbook, HttpServletRequest request, HttpServletResponse response) throws Exception {
		List<PurchaseRequirement> requirementsList = (List<PurchaseRequirement>) model.get("requirements");
		ResourceBundleMessageSource messageSource = (ResourceBundleMessageSource) model.get("messagesBundle");
		Locale locale = (Locale) model.get("locale");
		Map<Long, String> helpTexts = (Map<Long, String>) model.get("helpTexts");
		boolean requirementPriorityEnabled = (boolean) model.get("requirementPriorityEnabled");

		//localized headers
		String hReqId = messageSource.getMessage("xls.requirement.id", null, locale);
		String hRequirement = messageSource.getMessage("xls.requirement.name", null, locale);
		String hDescription = messageSource.getMessage("xls.requirement.description", null, locale);
		String hRationale = messageSource.getMessage("xls.requirement.rationale", null, locale);
		String hCategory = messageSource.getMessage("xls.requirement.category", null, locale);
		String hImportance = messageSource.getMessage("xls.requirement.importance", null, locale);
		String hFileCreationTime = messageSource.getMessage("xls.requirement.metadata.creationtime", null, locale);
		String hHelpText = messageSource.getMessage("pdf.purchase.view.helpText", null, locale);

		// create excel xls sheet
		Sheet sheet = workbook.createSheet(messageSource.getMessage("xls.requirement.sheetname", null, locale));

		Font headerFont = workbook.createFont();
		headerFont.setBold(true);

		CellStyle headerStyle = workbook.createCellStyle();
		headerStyle.setFont(headerFont);

		// create header row
		Row header = sheet.createRow(0);
		createCell(header, 0, hReqId, headerStyle);
		createCell(header, 1, hRequirement, headerStyle);
		createCell(header, 2, hDescription, headerStyle);
		createCell(header, 3, hRationale, headerStyle);
		createCell(header, 4, hCategory, headerStyle);
		
		if (requirementPriorityEnabled) {
			createCell(header, 5, hImportance, headerStyle);

			if (helpTexts != null && !helpTexts.isEmpty()) {
				createCell(header, 6, hHelpText, headerStyle);
			}
		} else {
			if (helpTexts != null && !helpTexts.isEmpty()) {
				createCell(header, 5, hHelpText, headerStyle);
			}
		}


		requirementsList = requirementsList.stream()
				.sorted((o1, o2) -> Long.compare(o1.getRequirementId(), o2.getRequirementId()))
				.collect(Collectors.toList());

		// Create data cells
		int rowCount = 1;
		for (PurchaseRequirement requirement : requirementsList) {
			Row courseRow = sheet.createRow(rowCount++);
			courseRow.createCell(0).setCellValue(requirement.getRequirementId());
			courseRow.createCell(1).setCellValue(requirement.getName());
			courseRow.createCell(2).setCellValue(requirement.getDescription());
			courseRow.createCell(3).setCellValue(requirement.getRationale());
			courseRow.createCell(4).setCellValue(requirement.getCategory() != null ? requirement.getCategory().getName() : "");
			
			if (requirementPriorityEnabled) {
				courseRow.createCell(5).setCellValue(messageSource.getMessage(requirement.getImportance().getValue(), null, locale));
				
				if (helpTexts != null && helpTexts.get(requirement.getRequirementId()) != null) {
					courseRow.createCell(6).setCellValue(helpTexts.get(requirement.getRequirementId()));
				}
			} else {
				if (helpTexts != null && helpTexts.get(requirement.getRequirementId()) != null) {
					courseRow.createCell(5).setCellValue(helpTexts.get(requirement.getRequirementId()));
				}
			}
			

			
		}

		sheet.autoSizeColumn(0);
		sheet.autoSizeColumn(1);
		sheet.autoSizeColumn(2);
		sheet.autoSizeColumn(3);
		sheet.autoSizeColumn(4);
		
		if (requirementPriorityEnabled) {
			sheet.autoSizeColumn(5);

			if (helpTexts != null && !helpTexts.isEmpty()) {
				sheet.autoSizeColumn(6);
			}
		} else {
			if (helpTexts != null && !helpTexts.isEmpty()) {
				sheet.autoSizeColumn(5);
			}
		}
		
		//Metadata sheet
		Sheet metaDataSheet = workbook.createSheet(messageSource.getMessage("xls.requirement.metadata.sheetname", null, locale));

		Row metaDataHeader = metaDataSheet.createRow(0);
		createCell(metaDataHeader, 0, hFileCreationTime, headerStyle);

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
