package dk.digitalidentity.re.service;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import lombok.extern.log4j.Log4j;

@Log4j
@Service
@Profile("!test")
public class EmailService implements MailSenderService {

	@Value("${email.username}")
	private String smtpUsername;
	
	@Value("${email.password}")
	private String smtpPassword;
	
	@Value("${email.host}")
	private String smtpHost;

	@Override
	public void sendMessage(String from, String to, String subject, String body) throws Exception {
		if (!isConfigured()) {
			log.warn("\"!test\" is not configured with a username/password - not sending emails!");
			return;
		}

		Properties props = System.getProperties();
		props.put("mail.transport.protocol", "smtp");
		props.put("mail.smtp.port", 25);
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.starttls.required", "true");

		Session session = Session.getDefaultInstance(props);

		MimeMessage msg = new MimeMessage(session);
		msg.setFrom(new InternetAddress(from));
		msg.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
		msg.setSubject(subject, "UTF-8");
		msg.setText(body, "UTF-8");
		msg.setHeader("Content-Type", "text/html; charset=UTF-8");

		Transport transport = session.getTransport();
		try {
			transport.connect(smtpHost, smtpUsername, smtpPassword);		
			transport.addTransportListener(new TransportErrorHandler());
			transport.sendMessage(msg, msg.getAllRecipients());
		}
		finally {
			try {
				transport.close();
			}
			catch (Exception ex) {
				log.warn("Error occured while trying to terminate connection", ex);
			}
		}
	}

	private boolean isConfigured() {
		if (smtpHost == null || smtpHost.length() == 0 || smtpPassword == null || smtpPassword.length() == 0 || smtpUsername == null || smtpUsername.length() == 0) {
			return false;
		}
		
		return true;
	}
}