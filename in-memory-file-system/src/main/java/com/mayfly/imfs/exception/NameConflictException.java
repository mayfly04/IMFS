package com.mayfly.imfs.exception;

public class NameConflictException extends FileSystemException {
	private static final long serialVersionUID = 8916043811085709603L;

	public NameConflictException(String message) {
		super(message);
	}
}