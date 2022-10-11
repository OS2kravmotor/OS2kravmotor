package dk.digitalidentity.re.config;

import java.util.ArrayList;
import java.util.List;

import dk.digitalidentity.re.dao.model.*;
import dk.digitalidentity.re.mvc.form.*;
import org.modelmapper.AbstractConverter;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.util.Pair;

import dk.digitalidentity.re.mvc.dto.AttachmentDTO;

@Configuration
public class ApplicationConfiguration {

	@Bean
	public ModelMapper modelMapper() {
		ModelMapper modelMapper = new ModelMapper();

		Converter<Requirement, RequirementForm> requirementConverter = new AbstractConverter<Requirement, RequirementForm>() {
			@Override
			protected RequirementForm convert(Requirement source) {
				RequirementForm destination = new RequirementForm();
				destination.setId(source.getId());
				destination.setName(source.getName());
				destination.setDescription(source.getDescription());
				destination.setRationale(source.getRationale());
				destination.setInfoRequirement(source.isInfoRequirement());
				destination.setNotes(source.getNotes());
				destination.setHelpText(source.getHelpText());
				destination.setInterestedParty(source.getInterestedParty());
				destination.setFavorite(source.isFavorite());
				destination.setImportance(source.getImportance());
				destination.setRequestedToBeShared(source.isRequestedToBeShared());
				destination.setAvailableForAllDomains(source.isAvailableForAllDomains());
				destination.setAvailableForAllTags(source.isAvailableForAllTags());
				destination.setLastChanged(source.getLastChanged());
				destination.setCvr(source.getCvr());
				destination.setAttachments(modelMapper.map(source.getAttachments(),new TypeToken<List<AttachmentDTO>>() {}.getType()));
				destination.setRelevantForOnPremise(source.isRelevantForOnPremise());
				destination.setRelevantForSaas(source.isRelevantForSaas());
				
				if (source.getCategory() != null) {
					destination.setCategory(source.getCategory().getId());
					CategoryForm categoryForm = new CategoryForm();
					categoryForm.setName(source.getCategory().getName());
					categoryForm.setId(source.getCategory().getId());
					destination.setCategoryRichObject(categoryForm);
				}
				
				if (source.getDomains() != null) {
					destination.setDomains(new ArrayList<>());
					for (Domain d : source.getDomains()) {
						destination.getDomains().add(d.getId());
					}
				}

				if (source.getTags() != null) {
					destination.setTags(new ArrayList<>());
					destination.setTagsRichObjects(new ArrayList<>());
					for (Tag t : source.getTags()) {
						destination.getTags().add(Long.toString(t.getId()));
						destination.getTagsRichObjects().add(new TagForm(t.getName(), t.getId(), t.getQuestion()));
					}
				}

				if (source.getPrinciples() != null) {
					destination.setPrinciples(new ArrayList<>());
					for (ArchitecturePrinciple p : source.getPrinciples()) {
						destination.getPrinciples().add(p.getId());
					}
				}

				return destination;
			}
		};
		
		// This converter is only to be used by the PurchaseControllers edit.html page
		Converter<PurchaseRequirement, RequirementForm> purchaseRequirementConverter = new AbstractConverter<PurchaseRequirement, RequirementForm>() {
			@Override
			protected RequirementForm convert(PurchaseRequirement source) {
				RequirementForm destination = new RequirementForm();
				destination.setId(source.getRequirementId()); // yes, we need to map to the original requirements ID
				destination.setName(source.getName());
				destination.setDescription(source.getDescription());
				destination.setRationale(source.getRationale());
				destination.setInfoRequirement(source.isInfoRequirement());
				destination.setImportance(source.getImportance());
				destination.setPurchaseRequirementId(source.getId()); // and here we store the purchase requirement ID instead ;)

				if (source.getCategory() != null) {
					destination.setCategory(source.getCategory().getId());
					CategoryForm categoryForm = new CategoryForm();
					categoryForm.setName(source.getCategory().getName());
					categoryForm.setId(source.getCategory().getId());
					destination.setCategoryRichObject(categoryForm);
				}

				return destination;
			}
		};

		Converter<PurchaseVendor, AnswerForm> answerConverter = new AbstractConverter<PurchaseVendor, AnswerForm>() {
			@Override
			protected AnswerForm convert(PurchaseVendor source) {
				AnswerForm destination = new AnswerForm();
				destination.setId(source.getId());
				destination.setVendorname(source.getName());
				destination.setAnswers(new ArrayList<>());
				destination.setAnswersRichObject(new ArrayList<>());
				
				if (source.getDetails() != null) {
					for (PurchaseVendorAnswer detail : source.getDetails()) {
						destination.getAnswersRichObject().add(Pair.of(Long.toString(detail.getId()), detail.getDetail()));
					}
				}

				return destination;
			}
		};

		var vendorUserConverter =  new AbstractConverter<VendorUser, VendorUserForm>() {
			@Override
			protected VendorUserForm convert(VendorUser source) {
				var destination = new VendorUserForm();
				destination.setId(source.getId());
				destination.setEmail(source.getEmail());
				destination.setAdmin(source.isAdmin());

				if (source.getPurchaseAnswers() != null) {
					destination.setPurchaseAnswerIds(new ArrayList<>());
					for (var purchaseAnswer : source.getPurchaseAnswers()) {
						destination.getPurchaseAnswerIds().add(purchaseAnswer.getId());
					}
				}
				return destination;
			}
		};

		modelMapper.addConverter(requirementConverter);
		modelMapper.addConverter(purchaseRequirementConverter);
		modelMapper.addConverter(answerConverter);
		modelMapper.addConverter(vendorUserConverter);

		return modelMapper;
	}
}
