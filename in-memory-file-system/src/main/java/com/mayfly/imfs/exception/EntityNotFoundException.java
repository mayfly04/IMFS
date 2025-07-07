package com.mayfly.imfs.exception;

public class EntityNotFoundException extends FileSystemException {
	private static final long serialVersionUID = -8497806674992137092L;

	public EntityNotFoundException(String message) {
		super(message);
	}
}
