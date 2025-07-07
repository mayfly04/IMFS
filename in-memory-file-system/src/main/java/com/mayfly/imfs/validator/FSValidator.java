package com.mayfly.imfs.validator;

import java.util.Map;

import com.mayfly.imfs.constants.EntityType;
import com.mayfly.imfs.constants.FileSystemConstants;
import com.mayfly.imfs.exception.NameConflictException;
import com.mayfly.imfs.model.Entity;
import com.mayfly.imfs.model.FileSystemEntity;
import com.mayfly.imfs.utils.FSUtils;

public class FSValidator {
	
	private FSValidator() {
		
	}
	
	private static final String FS_NAME_REGEX = "[a-zA-Z0-9]+";
	private static final String FS_FILE_NAME_REGEX = "[a-zA-Z0-9]+(\\.[a-zA-Z0-9]+)?";
	
	public static void validateNameAlreadyExists(Map<String, Entity> children, Entity entity) {
		if (children.containsKey(entity.getName())) {
            throw new NameConflictException(FileSystemConstants.NAME_CONFLICT_EXCP);
        }
	}
	
	public static void validateNameAlreadyExists(Map<String, Entity> children, String entityName) {
		if (children.containsKey(entityName)) {
            throw new NameConflictException(FileSystemConstants.NAME_CONFLICT_EXCP);
        }
	}

	public static void validateEntityName(EntityType entityType, String name) {
		if (EntityType.TEXT_FILE.equals(entityType)) {
            validateFileName(name);
        }else {
        	validateEntityName(name);
        }
	}
	
	public static void validateEntityName(String entityName) {
		if (!entityName.matches(FS_NAME_REGEX)) {
            throw new NameConflictException(FileSystemConstants.ENTITY_NAME_VALIDATION_EXCP);
        }
	}
	
	public static void validateFileName(String fileName) {
		if (!fileName.matches(FS_FILE_NAME_REGEX)) {
			throw new NameConflictException(FileSystemConstants.FILE_NAME_VALIDATION_EXCP);
		}
	}
	
	
	public static boolean isPathFileSystem(String fullPath, Map<String, FileSystemEntity> drive) {
		 return FSUtils.findEntity(fullPath, drive).isFileSystemEntity();	
	}
}
