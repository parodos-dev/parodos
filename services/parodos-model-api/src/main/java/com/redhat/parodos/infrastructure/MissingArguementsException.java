package com.redhat.parodos.infrastructure;

/**
 * Thrown when the WorkContext is missing a argument required by a Task
 * 
 * @author Luke Shannon (Github: lshannon)
 *
 */
public class MissingArguementsException extends Exception {
	
	private static final long serialVersionUID = 1L;
	
	public MissingArguementsException(String errorMessage) {
		super(errorMessage);
	}

}
