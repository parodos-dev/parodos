// @formatter:off
package com.redhat.parodos.examples;

import javax.jms.JMSException;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.redhat.parodos.tasks.tibco.TibcoJmsServiceImpl;
import com.redhat.parodos.tasks.tibco.TibcoMessageService;
import com.redhat.parodos.tasks.tibco.TibcoWorkFlowTask;
import com.redhat.parodos.workflow.annotation.Infrastructure;
import com.redhat.parodos.workflows.workflow.SequentialFlow;
import com.redhat.parodos.workflows.workflow.WorkFlow;
import com.tibco.tibjms.TibjmsConnectionFactory;

/**
 * 
 * Sample configuration for Tibco Task
 *
 */
@Configuration
public class TibcoWorkFlowConfiguration {
	
	@Bean
	TibjmsConnectionFactory connectionFactory(@Value("${url:ssl://server:7243}") String url) {
		return new TibjmsConnectionFactory(url);
	}
	
	// TIBCO's default installation:
	//  1. creates a CA cert PEM file at the location specified below
	//  2. creates a server cert with hostname 'server'
	// Add the hostname 'server' to 127.0.0.1 entry in /etc/hosts
	// Set the admin password in TIBCO to 'admin' or edit the username and password below
	@Bean
	TibcoMessageService tibcoMessageService(TibjmsConnectionFactory tibjmsConnectionFactory, @Value("${caFile:/opt/tibco/ems/10.2/samples/certs/server_root.cert.pem}") String caFile, @Value("${username:admin}") String username, @Value("${password:admin}") String password) throws JMSException {
		return new TibcoJmsServiceImpl(tibjmsConnectionFactory, caFile, username, password);
	}

	@Bean
	TibcoWorkFlowTask tibcoTask(TibcoMessageService tibcoMessageService) {
		return new TibcoWorkFlowTask(tibcoMessageService);
	}

	@Bean
	@Infrastructure
	WorkFlow tibcoWorkFlow(@Qualifier("tibcoTask") TibcoWorkFlowTask tibcoTask) {
		// @formatter:off
		return SequentialFlow.Builder
				.aNewSequentialFlow()
				.named("tibcoWorkFlow")
				.execute(tibcoTask)
				.build();
		// @formatter:on
	}

}
