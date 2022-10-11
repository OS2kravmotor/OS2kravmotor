package dk.digitalidentity.re.mvc.view;

import java.text.SimpleDateFormat;
import java.util.Date;
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

import dk.digitalidentity.re.dao.model.ArchitecturePrinciple;
import dk.digitalidentity.re.dao.model.Domain;
import dk.digitalidentity.re.dao.model.Requirement;
import dk.digitalidentity.re.dao.model.Tag;

public class HistoryRequirementXlsView extends AbstractXlsView {

	@Override
	protected void buildExcelDocument(Map<String, Object> model, Workbook workbook, HttpServletRequest request, HttpServletResponse response) throws Exception {
		Requirement requirement = (Requirement) model.get("requirement");
		ResourceBundleMessageSource messageSource = (ResourceBundleMessageSource) model.get("messagesBundle");
		Locale locale = (Locale) model.get("locale");

		//localized headers
		String hReqId = messageSource.getMessage("xls.requirement.id", null, locale);
		String hRequirement = messageSource.getMessage("xls.requirement.name", null, locale);
		String hImportance = messageSource.getMessage("xls.requirement.importance", null, locale);
		String hCategory = messageSource.getMessage("xls.requirement.category", null, locale);
		String hDescription = messageSource.getMessage("xls.requirement.description", null, locale);
		String hRationale = messageSource.getMessage("xls.requirement.rationale", null, locale);
		String hLastChanged = messageSource.getMessage("xls.requirement.lastchanged", null, locale);
		String hHelpText = messageSource.getMessage("xls.requirement.helptext", null, locale);
		String hDomains = messageSource.getMessage("xls.requirement.domains", null, locale);
		String hTags = messageSource.getMessage("xls.requirement.tags", null, locale);
		String hPrinciples = messageSource.getMessage("xls.requirement.principles", null, locale);
		String hFileCreationTime = messageSource.getMessage("xls.requirement.metadata.creationtime", null, locale);

		// create excel xls sheet
		Sheet sheet = workbook.createSheet(messageSource.getMessage("xls.requirement.sheetname", null, locale));

		Font headerFont = workbook.createFont();
		headerFont.setBold(true);

		CellStyle headerStyle = workbook.createCellStyle();
		headerStyle.setFont(headerFont);

		// create header row
		Row rRegId = sheet.createRow(0);
		createCell(rRegId, 0, hReqId, headerStyle);
		createCell(rRegId, 1, requirement.getId() + "", null);
		
		Row rRequirement = sheet.createRow(1);
		createCell(rRequirement, 0, hRequirement, headerStyle);
		createCell(rRequirement, 1, requirement.getName(), null);
		
		Row rImportance = sheet.createRow(2);
		createCell(rImportance, 0, hImportance, headerStyle);
		createCell(rImportance, 1, requirement.getImportance() != null ? messageSource.getMessage(requirement.getImportance().getValue(), null, locale) : "", null);

		Row rCategory = sheet.createRow(3);
		createCell(rCategory, 0, hCategory, headerStyle);
		createCell(rCategory, 1, requirement.getCategory() != null ? requirement.getCategory().getName() : "", null);
		
		Row rDescription = sheet.createRow(4);
		createCell(rDescription, 0, hDescription, headerStyle);
		createCell(rDescription, 1, requirement.getDescription(), null);
		
		Row rRationale = sheet.createRow(5);
		createCell(rRationale, 0, hRationale, headerStyle);
		createCell(rRationale, 1, requirement.getRationale(), null);
		
		Row rLastChanged = sheet.createRow(6);
		createCell(rLastChanged, 0, hLastChanged, headerStyle);
		createCell(rLastChanged, 1, requirement.getLastChanged() != null ? new SimpleDateFormat("yyyy/MM/dd - HH:mm").format(requirement.getLastChanged()) : "", null);

		Row rHelpText = sheet.createRow(7);
		createCell(rHelpText, 0, hHelpText, headerStyle);
		createCell(rHelpText, 1, requirement.getHelpText(), null);

		Row rDomains = sheet.createRow(8);
		createCell(rDomains, 0, hDomains, headerStyle);
		createCell(rDomains, 1, requirement.getDomains().stream().map(Domain::getName).collect(Collectors.joining("\n")), null);

		Row rTags = sheet.createRow(9);
		createCell(rTags, 0, hTags, headerStyle);
		createCell(rTags, 1, requirement.getTags().stream().map(Tag::getName).collect(Collectors.joining("\n")), null);

		Row rPrinciples = sheet.createRow(10);
		createCell(rPrinciples, 0, hPrinciples, headerStyle);
		createCell(rPrinciples, 1, requirement.getPrinciples().stream().map(ArchitecturePrinciple::getName).collect(Collectors.joining("\n")), null);

		sheet.autoSizeColumn(0);
		sheet.autoSizeColumn(1);

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
