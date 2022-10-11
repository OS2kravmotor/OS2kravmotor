package dk.digitalidentity.re.test;

import org.springframework.stereotype.Service;

import dk.digitalidentity.re.service.MailSenderService;

@Service
public class StubbedMailSender implements MailSenderService {

	@Override
	public void sendMessage(String from, String to, String subject, String body) throws Exception {
		; // do nothing
	}
}
