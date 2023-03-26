package com.redhat.parodos.tasks.tibco;

public interface Tibjms {

	public void sendMessage(String url, String caFile, String username, String password, String topic, String message)
			throws javax.jms.JMSException;

}
