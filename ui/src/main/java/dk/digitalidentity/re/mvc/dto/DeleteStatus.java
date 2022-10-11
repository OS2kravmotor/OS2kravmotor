package dk.digitalidentity.re.mvc.dto;

import lombok.Data;

@Data
public class DeleteStatus {
	private boolean success;
	private long requirementQuantity;
	private long purchaseQuantity;
}
