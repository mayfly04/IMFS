package com.mayfly.imfs.model;

import java.time.LocalDateTime;

import com.mayfly.imfs.constants.EntityType;
import com.mayfly.imfs.validator.FSValidator;

public abstract class Entity {
	protected String name;
	protected EntityType type;
	protected Entity parent;

	// Additional thing added just for testing sake
	protected LocalDateTime lastModified;

	public Entity(String name, EntityType type, Entity parent) {
		if (EntityType.TEXT_FILE.equals(type)) {
			FSValidator.validateFileName(name);
		} else {
			FSValidator.validateEntityName(name);
		}
		this.name = name;
		this.type = type;
		this.parent = parent;
		this.lastModified = LocalDateTime.now();
	}

	public String getName() {
		return name;
	}

	public EntityType getType() {
		return type;
	}

	public Entity getParent() {
		return parent;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setParent(Entity parent) {
		this.parent = parent;
	}

	public LocalDateTime getLastModified() {
		return lastModified;
	}

	public void updateLastModified() {
		lastModified = LocalDateTime.now();
	}

	public String getPath() {
		if (type == EntityType.DRIVE)
			return name;
		return parent.getPath() + "\\" + name;
	}

	public boolean isFileSystemEntity() {
		return type == EntityType.DRIVE || type == EntityType.FOLDER || type == EntityType.ZIP_FILE;
	}
}