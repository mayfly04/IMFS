package com.mayfly.imfs.service;

import static com.mayfly.imfs.utils.FSUtils.findEntity;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.mayfly.imfs.constants.EntityType;
import com.mayfly.imfs.exception.InvalidOperationException;
import com.mayfly.imfs.exception.NameConflictException;
import com.mayfly.imfs.model.Entity;
import com.mayfly.imfs.model.FileSystemEntity;
import com.mayfly.imfs.model.TextFile;
import com.mayfly.imfs.validator.FSValidator;

public class FileSystemWorker {

	private final Map<String, FileSystemEntity> drives = new ConcurrentHashMap<>();

	public void create(EntityType type, String name, String parentPath) {
		System.out.println("Creating " + type + ": " + name + " under " + parentPath);
		if (type == null) {
			throw new InvalidOperationException("Unknown entity type");
		}
		if (type == EntityType.DRIVE) {
			if (drives.containsKey(name))
				throw new NameConflictException("Drive already exists: " + name);

			drives.put(name, new FileSystemEntity(name, EntityType.DRIVE, null));
			return;
		}
		FileSystemEntity parent = (FileSystemEntity) findEntity(parentPath, drives);
		if (!parent.isFileSystemEntity() || parent.getType() == EntityType.TEXT_FILE)
			throw new InvalidOperationException("Parent must be a File System");
		Entity entity;
		switch (type) {
		case FOLDER:
			entity = new FileSystemEntity(name, EntityType.FOLDER, parent);
			break;
		case ZIP_FILE:
			entity = new FileSystemEntity(name, EntityType.ZIP_FILE, parent);
			break;
		case TEXT_FILE:
			entity = new TextFile(name, parent);
			break;
		default:
			throw new InvalidOperationException("Unknown entity type");
		}
		parent.addChild(entity);
	}

	public void delete(String path) {
		Entity entity = findEntity(path, drives);
		if (entity.getType() == EntityType.DRIVE) {
			drives.remove(entity.getName());
		} else {
			FileSystemEntity parent = (FileSystemEntity) entity.getParent();
			parent.removeChild(entity.getName());
		}
		System.out.println("Deleted: " + path);
	}

	public void move(String srcPath, String destPath) {		
		if(!FSValidator.isPathFileSystem(srcPath, drives))
			throw new InvalidOperationException("Source Path is not a FileSystem");
		
		Entity entity = findEntity(srcPath, drives);
		if (entity.getType() == EntityType.DRIVE)
			throw new InvalidOperationException("Cannot move drive");
		
		FileSystemEntity dest = (FileSystemEntity) findEntity(destPath, drives);
		if (!dest.isFileSystemEntity())
			throw new InvalidOperationException("Destination Path is not a FileSystem");

		FSValidator.validateNameAlreadyExists(dest.getChildren(), entity);

		FileSystemEntity oldParent = (FileSystemEntity) entity.getParent();
		oldParent.removeChild(entity.getName());

		entity.setParent(dest);
		dest.addChild(entity);

		System.out.println("Moved " + entity.getName() + " to " + dest.getPath());
	}

	public void writeToFile(String path, String content) {
		Entity entity = findEntity(path, drives);
		if (!(entity instanceof TextFile))
			throw new InvalidOperationException("Not a text file");

		((TextFile) entity).setContent(content);
		System.out.println("Wrote to file: " + path);
	}

	//Thought about this while writing tests. add on feature!!
	public void rename(String path, String newName) {
		Entity entity = findEntity(path, drives);
		if (entity.getType() == EntityType.DRIVE)
			throw new InvalidOperationException("Cannot rename drive");

		FSValidator.validateEntityName(entity.getType(), newName);

		FileSystemEntity parent = (FileSystemEntity) entity.getParent();
		FSValidator.validateNameAlreadyExists(parent.getChildren(), newName);

		parent.getChildren().remove(entity.getName());
		entity.setName(newName);

		parent.getChildren().put(newName, entity);
		entity.updateLastModified();

		System.out.println("Renamed entity to: " + newName);
	}

	public Map<String, FileSystemEntity> getDrives() {
		return drives;
	}

}
