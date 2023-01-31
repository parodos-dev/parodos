package com.redhat.parodos.workflows;

import org.springframework.shell.component.flow.ComponentFlow;
import org.springframework.shell.component.flow.ComponentFlow.Builder;
import org.springframework.shell.component.flow.ComponentFlow.ComponentFlowResult;
import org.springframework.shell.standard.AbstractShellComponent;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

/**
 * Sets up:
 * 
 * - working directory where code will be generated
 * - base package name
 * - artifact name
 * - description
 * 
 * @author Luke Shannon (Github: lshannon)
 *
 */
@ShellComponent 
public class SessionInitialization extends AbstractShellComponent {

	
	private ComponentFlow.Builder componentFlowBuilder;
	
	private CLIWorkContext context;
	
	public SessionInitialization(Builder componentFlowBuilder, CLIWorkContext context) {
		this.componentFlowBuilder = componentFlowBuilder;
		this.context = context;
	}

	@ShellMethod(key = "init")
	public void getSessionInformation() {
		ComponentFlow flow = componentFlowBuilder.clone().reset()
				.withPathInput("Working Directory (where code will be generated):")
					.name("workingDir")
					.and()
				.withStringInput("Base Package")
					.name("basePackage")
					.and()
				.withStringInput("Artifact Name")
					.name("artifactName")
					.and()
				.withStringInput("Description")
					.name("description")
					.and()
				.build();
		flow.run();
	}

}
