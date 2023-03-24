package com.redhat.parodos.tasks.tibco;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.tibco.tibjms.TibjmsConnectionFactory;

@ExtendWith(MockitoExtension.class)
class TibcoJmsServiceImplTest {

	private static final String TEST = "test";

	@Mock
	TibjmsConnectionFactory tibjmsConnectionFactory;

	@Mock
	Connection connection;

	@Mock
	Topic topic;

	@Mock
	MessageProducer producer;

	@Mock
	TextMessage textMessage;

	@Mock
	Session session;

	@Test
	void createInstance_success_shouldReturnReference() throws JMSException {
		TibcoJmsServiceImpl instance = getReferenceWithMockConnection();
		assertNotNull(instance);
	}

	@Test
	void sendMessage_success_shouldExecute() throws JMSException {
		TibcoJmsServiceImpl instance = getReferenceWithMockConnection();
		configureMockMessageObjects();
		instance.sendMessage(TEST, TEST);
		verify(producer, times(1)).send(any());
	}

	@Test()
	void sendMessage_fail_shouldThrowException() throws JMSException {
		TibcoJmsServiceImpl instance = getReferenceWithMockConnection();
		configureMockMessageObjects();
		Mockito.doThrow(JMSException.class).when(producer).send(any());
		assertThrows(JMSException.class, () -> instance.sendMessage(TEST, TEST));
	}

	private TibcoJmsServiceImpl getReferenceWithMockConnection() throws JMSException {
		Mockito.when(tibjmsConnectionFactory.createConnection(TEST, TEST)).thenReturn(connection);
		TibcoJmsServiceImpl instance = new TibcoJmsServiceImpl(tibjmsConnectionFactory, TEST, TEST, TEST);
		return instance;
	}

	private void configureMockMessageObjects() throws JMSException {
		Mockito.when(connection.createSession(javax.jms.Session.AUTO_ACKNOWLEDGE)).thenReturn(session);
		// the code is able to supply a Destination in place of a Topic - however the
		// Mocks do not seem to allow it
		Mockito.when(session.createTopic(any())).thenReturn(topic);
		Mockito.when(session.createProducer(any())).thenReturn(producer);
		Mockito.when(session.createTextMessage(any())).thenReturn(textMessage);
	}

}
