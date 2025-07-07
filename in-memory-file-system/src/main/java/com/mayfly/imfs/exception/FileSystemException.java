package com.mayfly.imfs.exception;

public class FileSystemException extends RuntimeException {
	private static final long serialVersionUID = 3488727755521599653L;

	public FileSystemException(String message) {
		super(message);
	}
}