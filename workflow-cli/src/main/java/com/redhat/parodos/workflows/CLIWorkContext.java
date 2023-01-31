package com.redhat.parodos.workflows;

import org.springframework.stereotype.Component;
import lombok.Data;

/**
 * Reference for storing Session Information related to code generation
 * 
 * @author Luke Shannon (Github: lshannon)
 *
 */
@Component
@Data
public class CLIWorkContext {
	
	public String workingDirectory;
	public String basePackage;
	public String artifactName;
	public String description;

}
