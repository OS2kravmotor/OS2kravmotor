package dk.digitalidentity.re.api.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import dk.digitalidentity.re.api.dto.PurchaseDTO;
import dk.digitalidentity.re.api.dto.PurchaseWinnerDTO;
import dk.digitalidentity.re.api.dto.RequirementDTO;
import dk.digitalidentity.re.dao.model.Purchase;
import dk.digitalidentity.re.dao.model.PurchaseRequirement;
import dk.digitalidentity.re.service.PurchaseService;

@RestController
@RequestMapping("/api")
public class PurchaseApi {

	@Autowired
	private PurchaseService purchaseService;

	@RequestMapping(value = "/purchase")
	@ResponseBody
	public ResponseEntity<List<PurchaseDTO>> getPurchases() {
		List<Purchase> purchases = purchaseService.getAllPurchases();
		List<PurchaseDTO> purchaseDTOs = new ArrayList<>();

		for (Purchase purchase : purchases) {
			PurchaseDTO purchaseDTO = new PurchaseDTO();
			purchaseDTO.setId(purchase.getId());
			purchaseDTO.setTitle(purchase.getTitle());
			purchaseDTO.setDescription(purchase.getDescription());
			purchaseDTO.setEmail(purchase.getEmail());
			purchaseDTO.setStatus(purchase.getStatus().toString());
			purchaseDTO.setStartTime(purchase.getStartTime());
			purchaseDTO.setEndTime(purchase.getEndTime());
			
			purchaseDTOs.add(purchaseDTO);
		}

		return new ResponseEntity<>(purchaseDTOs, HttpStatus.OK);
	}

	@RequestMapping(value = "/purchase/{id}")
	@ResponseBody
	public ResponseEntity<PurchaseDTO> getPurchase(@PathVariable("id") long id) {
		Purchase purchase = purchaseService.getById(id);
		if (purchase == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}

		PurchaseDTO purchaseDTO = new PurchaseDTO();
		purchaseDTO.setId(purchase.getId());
		purchaseDTO.setTitle(purchase.getTitle());
		purchaseDTO.setDescription(purchase.getDescription());
		purchaseDTO.setEmail(purchase.getEmail());
		purchaseDTO.setStatus(purchase.getStatus().toString());
		purchaseDTO.setStartTime(purchase.getStartTime());
		purchaseDTO.setEndTime(purchase.getEndTime());

		return new ResponseEntity<PurchaseDTO>(purchaseDTO, HttpStatus.OK);
	}

	@RequestMapping(value = "/purchase/{id}/requirements")
	@ResponseBody
	public ResponseEntity<List<RequirementDTO>> getPurchaseRequirements(@PathVariable("id") long id) {
		Purchase purchase = purchaseService.getById(id);
		if (purchase == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}

		List<RequirementDTO> requirementDTOs = new ArrayList<>();
		for (PurchaseRequirement purchaseRequirement : purchase.getRequirements()) {
			RequirementDTO dto = new RequirementDTO();
			dto.setDescription(purchaseRequirement.getDescription());
			dto.setId(purchaseRequirement.getRequirementId());
			dto.setImportance(purchaseRequirement.getImportance());
			dto.setName(purchaseRequirement.getName());
			
			requirementDTOs.add(dto);
		}

		return new ResponseEntity<>(requirementDTOs, HttpStatus.OK);
	}

	@RequestMapping(value = "/purchase/{id}/winner")
	@ResponseBody
	public ResponseEntity<PurchaseWinnerDTO> getPurchaseWinner(@PathVariable("id") long id) {
		Purchase purchase = purchaseService.getById(id);
		if (purchase == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}

		var winnerDTO = purchaseService.getWinnerDTO(purchase);

		return new ResponseEntity<>(winnerDTO, HttpStatus.OK);
	}
}
