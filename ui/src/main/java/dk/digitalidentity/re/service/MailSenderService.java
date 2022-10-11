package dk.digitalidentity.re.service;

public interface MailSenderService {

	void sendMessage(String from, String to, String subject, String body) throws Exception;

}