package com.redhat.parodos.tasks.tibco;

import com.tibco.tibjms.TibjmsConnectionFactory;

import javax.jms.JMSException;
import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.Session;
import javax.jms.MessageProducer;
import javax.jms.TextMessage;

public class TibjmsImpl implements Tibjms {

	@Override
	public void sendMessage(String url, String caFile, String username, String password, String topic, String message)
			throws JMSException {
		TibjmsConnectionFactory factory = new TibjmsConnectionFactory(url);
		if (!caFile.isEmpty()) {
			factory.setSSLTrustedCertificate(caFile);
		}
		try (Connection connection = factory.createConnection(username, password);
				Session session = connection.createSession(javax.jms.Session.AUTO_ACKNOWLEDGE);) {
			Destination destination = session.createTopic(topic);
			MessageProducer producer = session.createProducer(destination);
			TextMessage textMessage = session.createTextMessage(message);
			producer.send(textMessage);
		}
	}

}
