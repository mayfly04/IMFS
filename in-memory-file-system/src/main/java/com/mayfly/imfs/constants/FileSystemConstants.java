package com.mayfly.imfs.constants;

public final class FileSystemConstants {

	private FileSystemConstants() {
		
	}
	
	public static final String NAME_CONFLICT_EXCP = "Name already exists in parent.";
	
	public static final String ENTITY_NAME_VALIDATION_EXCP = "Name must only contain be alphanumeric";
	
	public static final String FILE_NAME_VALIDATION_EXCP = "Invalid file name. Only alphanumeric names and a single extension are allowed (e.g., file.txt).";

}
