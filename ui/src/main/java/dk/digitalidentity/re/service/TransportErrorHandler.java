package dk.digitalidentity.re.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.event.TransportEvent;
import javax.mail.event.TransportListener;

public class TransportErrorHandler implements TransportListener {
	private static final Logger log = LoggerFactory.getLogger(TransportErrorHandler.class);

	@Override
	public void messageDelivered(TransportEvent e) {
		//Success. Do nothing
	}

	@Override
	public void messageNotDelivered(TransportEvent e) {
		log.warn("Message NOT delivered!");
	}

	@Override
	public void messagePartiallyDelivered(TransportEvent e) {
		log.warn("Message partialy delivered!");
	}
}
