package com.redhat.parodos.sdkutils;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.ClassPathResource;

import java.util.Properties;

/**
 * @author Gloria Ciavarrini (Github: gciavarrini)
 */
@Slf4j
@Data
public class CustomPropertiesReader {

	String serverIp;

	String serverPort;

	public CustomPropertiesReader() {

		// Load the properties from application.yml

		// Create an Environment instance and set the default properties
		StandardEnvironment env = new StandardEnvironment();
		Properties props = readCustomProperties();
		env.getPropertySources().addLast(new PropertiesPropertySource("serverProperties", props));

		// Retrieve the values of app.serverport and app.serverip
		serverIp = env.resolvePlaceholders("${app.serverip}");
		serverPort = env.resolvePlaceholders("${app.serverport}");
		evalPortNumber();
	}

	private void evalPortNumber() {
		try {
			int port = Integer.parseInt(serverPort);
			if (port < 0 || port > 65535) {
				throw new IllegalArgumentException("Invalid port number: " + serverPort);
			}
		}
		catch (NumberFormatException e) {
			throw new IllegalArgumentException("Invalid serverPort value: " + serverPort, e);
		}
	}

	public Properties readCustomProperties() {
		YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean();
		yaml.setResources(new ClassPathResource("application.yml"));
		return yaml.getObject();
	}

}
