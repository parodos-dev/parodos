package com.redhat.parodos.tasks.deploy;

public class ManifestDeployException extends RuntimeException {

	public ManifestDeployException(String error, Throwable e) {
		super(error, e);
	}

}