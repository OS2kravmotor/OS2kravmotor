package dk.digitalidentity.re.dao.model.enums;

import lombok.Getter;

@Getter
public enum EmailTemplateType {
	INVITE_USER("enum.email.message.type.invite_user"),
	PURCHASE_POSTPONEMENT("enum.email.message.type.purchase_postponement"),
	PURCHASE_CANCELLATION("enum.email.message.type.purchase_cancellation"),
	NOTIFY_VENDOR_ELABORATION("enum.email.message.type.notify_vendor_elaboration"),
	NOTIFY_CUSTOMER("enum.email.message.type.notify_customer");
	
	private String message;
	
	private EmailTemplateType(String message) {
		this.message = message;
	}
}