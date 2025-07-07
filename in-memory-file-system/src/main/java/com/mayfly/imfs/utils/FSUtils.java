package com.mayfly.imfs.utils;

import java.util.Map;

import com.mayfly.imfs.exception.EntityNotFoundException;
import com.mayfly.imfs.model.Entity;
import com.mayfly.imfs.model.FileSystemEntity;

public class FSUtils {

	
	private FSUtils() {
		
	}
	
	
	public static Entity findEntity(String path,  Map<String, FileSystemEntity> drives) {
		String[] parts = path.split("\\\\");
		if (parts.length == 0)
			throw new IllegalArgumentException("Invalid path");
		FileSystemEntity current = drives.get(parts[0]);
		if (current == null)
			throw new EntityNotFoundException("Drive not found: " + parts[0]);
		Entity cur = current;
		for (int i = 1; i < parts.length; i++) {
			if (!(cur instanceof FileSystemEntity))
				throw new EntityNotFoundException("Not a file system: " + cur.getPath());
			cur = ((FileSystemEntity) cur).getChild(parts[i]);
		}
		return cur;
	}
	
	
}
