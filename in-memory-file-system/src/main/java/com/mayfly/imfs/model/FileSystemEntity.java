package com.mayfly.imfs.model;

import java.util.*;

import com.mayfly.imfs.constants.EntityType;
import com.mayfly.imfs.exception.EntityNotFoundException;
import com.mayfly.imfs.exception.NameConflictException;

public class FileSystemEntity extends Entity {
	protected Map<String, Entity> children = Collections.synchronizedMap(new LinkedHashMap<>());

	public FileSystemEntity(String name, EntityType type, Entity parent) {
		super(name, type, parent);
	}

	public void addChild(Entity child) {
		if (children.containsKey(child.getName())) {
			throw new NameConflictException(
					"Name '" + child.getName() + "' already exists in '" + this.getPath() + "'");
		}
		children.put(child.getName(), child);
		updateLastModified();
	}

	public void removeChild(String name) {
		if (!children.containsKey(name))
			throw new EntityNotFoundException("No such child: " + name);
		children.remove(name);
		updateLastModified();
	}

	public Entity getChild(String name) {
		Entity e = children.get(name);
		if (e == null)
			throw new EntityNotFoundException("No such child: " + name);
		return e;
	}

	public Map<String, Entity> getChildren() {
		return children;
	}
}