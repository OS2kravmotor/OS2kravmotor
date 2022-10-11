package dk.digitalidentity.re.mvc.form;

import dk.digitalidentity.re.dao.model.PurchaseAnswer;
import lombok.Data;

@Data
public class VendorPurchaseForm {
	private String customerName;
	private PurchaseAnswer purchaseAnswer;
}
