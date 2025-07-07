package com.mayfly.imfs.exception;

public class InvalidOperationException extends FileSystemException {
    private static final long serialVersionUID = 1L;

	public InvalidOperationException(String message) { super(message); }
}