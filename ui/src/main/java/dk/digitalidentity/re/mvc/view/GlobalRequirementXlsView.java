package dk.digitalidentity.re.mvc.view;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
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

import dk.digitalidentity.re.Constants;
import dk.digitalidentity.re.dao.model.Attachment;
import dk.digitalidentity.re.dao.model.LocalAttachment;
import dk.digitalidentity.re.dao.model.Requirement;
import dk.digitalidentity.re.dao.model.RequirementExtension;
import dk.digitalidentity.re.service.RequirementService;

public class GlobalRequirementXlsView extends AbstractXlsView {

	@Override
	protected void buildExcelDocument(Map<String, Object> model, Workbook workbook, HttpServletRequest request, HttpServletResponse response) throws Exception {
		DateFormat dateFormatNoTime = new SimpleDateFormat("yyyy-MM-dd");

		@SuppressWarnings("unchecked")
		List<Requirement> requirementsList = (List<Requirement>) model.get("requirements");

		ResourceBundleMessageSource messageSource = (ResourceBundleMessageSource) model.get("messagesBundle");
		RequirementService requirementService = (RequirementService) model.get("requirementService");
		Locale locale = (Locale) model.get("locale");
		boolean isLoggedIn = (boolean) model.get("isLoggedIn");
		boolean isEditor = (boolean) model.get("isEditor");

		// localized headers
		String hReqId = messageSource.getMessage("xls.requirement.id", null, locale);
		String hRequirement = messageSource.getMessage("xls.requirement.name", null, locale);
		String hRequirementType = messageSource.getMessage("xls.requirement.type", null, locale);
		String hDescription = messageSource.getMessage("xls.requirement.description", null, locale);
		String hLocalDescription = messageSource.getMessage("xls.requirement.localdescription", null, locale); // loggedIn
		String hRationale = messageSource.getMessage("xls.requirement.rationale", null, locale);
		String hCategory = messageSource.getMessage("xls.requirement.category", null, locale);
		String hImportance = messageSource.getMessage("xls.requirement.importance", null, locale);
		String hFileCreationTime = messageSource.getMessage("xls.requirement.metadata.creationtime", null, locale);
		String hHelpText = messageSource.getMessage("xls.requirement.helptext", null, locale); // loggedIn
		String hNotes = messageSource.getMessage("xls.requirement.notes", null, locale); //loggedIn && isEditor
		String hInterestedParty = messageSource.getMessage("xls.requirement.interestedparty", null, locale); // loggedIn
		String hAttachments = messageSource.getMessage("xls.requirement.attachments", null, locale); //loggedIn
		String hLastChanged = messageSource.getMessage("xls.requirement.lastchanged", null, locale);
		
		// create excel xls sheet
		Sheet sheet = workbook.createSheet(messageSource.getMessage("xls.requirement.sheetname", null, locale));

		Font headerFont = workbook.createFont();
		headerFont.setBold(true);

		CellStyle headerStyle = workbook.createCellStyle();
		headerStyle.setFont(headerFont);

		// create header row
		Row header = sheet.createRow(0);
		int headerCount = 0;

		createCell(header, headerCount++, hReqId, headerStyle);
		createCell(header, headerCount++, hRequirement, headerStyle);
		createCell(header, headerCount++, hRequirementType, headerStyle);
		createCell(header, headerCount++, hDescription, headerStyle);

		if (isLoggedIn) {
			createCell(header, headerCount++, hLocalDescription, headerStyle);
			createCell(header, headerCount++, hHelpText, headerStyle);
			createCell(header, headerCount++, hInterestedParty, headerStyle);
		}

		createCell(header, headerCount++, hCategory, headerStyle);
		createCell(header, headerCount++, hImportance, headerStyle);
		createCell(header, headerCount++, hRationale, headerStyle);
		createCell(header, headerCount++, hAttachments, headerStyle);
		createCell(header, headerCount++, hLastChanged, headerStyle);
		
		if (isEditor) {
			createCell(header, headerCount++, hNotes, headerStyle);
		}

		requirementsList = requirementsList.stream()
				.sorted((o1, o2) -> Long.compare(o1.getId(), o2.getId()))
				.collect(Collectors.toList());

		// Create data cells
		int rowCount = 1;
		for (Requirement requirement : requirementsList) {
			boolean isGlobalRequirement = Objects.equals(requirement.getCvr(), Constants.DEFAULT_CVR);
			RequirementExtension extension = null;
			if (isGlobalRequirement && isLoggedIn) {
				extension = requirementService.getRequirementExtension(requirement);
			}

			Row courseRow = sheet.createRow(rowCount++);

			int column = 0;
			courseRow.createCell(column++).setCellValue(requirement.getId());
			courseRow.createCell(column++).setCellValue(requirement.getName());
			courseRow.createCell(column++).setCellValue((isGlobalRequirement) ? "Tværkommunalt krav" : "Lokalt krav");
			courseRow.createCell(column++).setCellValue(requirement.getDescription());
			
			if (isLoggedIn) {
				courseRow.createCell(column++).setCellValue(getLocalDescription(requirement, extension));
				courseRow.createCell(column++).setCellValue(getHelpText(requirement, extension));
				courseRow.createCell(column++).setCellValue(getInterestedParty(requirement, extension));
			}

			courseRow.createCell(column++).setCellValue(requirement.getCategory() != null ? requirement.getCategory().getName() : "");
			courseRow.createCell(column++).setCellValue(messageSource.getMessage(requirement.getImportance().getValue(), null, locale));
			courseRow.createCell(column++).setCellValue(requirement.getRationale());

			String attachments = requirement.getAttachments().stream().map(Attachment::getUrl).collect(Collectors.joining("\n"));
			if (extension != null) {
				String localAttachments = extension.getAttachments().stream().map(LocalAttachment::getUrl).collect(Collectors.joining("\n"));
				attachments = String.join("\n", attachments, localAttachments);
			}
			courseRow.createCell(column++).setCellValue(attachments);
			
			courseRow.createCell(column++).setCellValue(dateFormatNoTime.format(requirement.getLastChanged()));

			if (isEditor) {
				courseRow.createCell(column++).setCellValue(requirement.getNotes());
			}
		}

		// Metadata sheet
		Sheet metaDataSheet = workbook.createSheet(messageSource.getMessage("xls.requirement.metadata.sheetname", null, locale));

		Row metaDataHeader = metaDataSheet.createRow(0);
		createCell(metaDataHeader, 0, hFileCreationTime, headerStyle);

		Row metaData = metaDataSheet.createRow(1);
		metaData.createCell(0).setCellValue(dateFormatNoTime.format(new Date()));

		metaDataSheet.autoSizeColumn(0);
	}

	private static void createCell(Row header, int column, String value, CellStyle style) {
		Cell cell = header.createCell(column);
		cell.setCellValue(value);
		cell.setCellStyle(style);
	}
	
	private String getHelpText(Requirement requirement, RequirementExtension extension) {
		if (extension != null) {
			return extension.getHelpText();
		}
		
		return requirement.getHelpText();
	}
	
	private String getLocalDescription(Requirement requirement, RequirementExtension extension) {
		if (extension != null) {
			return extension.getDescription();
		}
		
		return null;
	}
	
	private String getInterestedParty(Requirement requirement, RequirementExtension extension) {
		if (extension != null) {
			return extension.getInterestedParty();
		}
		
		return requirement.getInterestedParty();
	}
}
