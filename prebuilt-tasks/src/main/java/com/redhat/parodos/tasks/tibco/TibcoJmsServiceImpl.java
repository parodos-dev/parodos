/*
 * Copyright (c) 2022 Red Hat Developer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.redhat.parodos.tasks.tibco;

import com.tibco.tibjms.TibjmsConnectionFactory;

import javax.jms.JMSException;
import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.Session;
import javax.jms.MessageProducer;
import javax.jms.TextMessage;

/**
 * Implementation of a TibcoMessageService to send JMS messages
 *
 */
public class TibcoJmsServiceImpl implements TibcoMessageService {

	TibjmsConnectionFactory factory;

	Connection connection;

	// Create the connection on construction
	public TibcoJmsServiceImpl(TibjmsConnectionFactory factory, String caFile, String username, String password)
			throws JMSException {
		createConnection(factory, caFile, username, password);
	}

	@Override
	public void sendMessage(String topic, String message) throws JMSException {
		try (Session session = connection.createSession(javax.jms.Session.AUTO_ACKNOWLEDGE)) {
			Destination destination = session.createTopic(topic);
			MessageProducer producer = session.createProducer(destination);
			TextMessage textMessage = session.createTextMessage(message);
			producer.send(textMessage);
		}

	}

	@Override
	public void createConnection(TibjmsConnectionFactory factory, String caFile, String username, String password)
			throws JMSException {
		if (connection == null) {
			if (caFile != null && !caFile.isEmpty()) {
				factory.setSSLTrustedCertificate(caFile);
			}
			connection = factory.createConnection(username, password);
		}
	}

	@Override
	public void closeConnection() throws JMSException {
		connection.close();
	}

}
