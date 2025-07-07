package com.mayfly.imfs.model;

import com.mayfly.imfs.constants.EntityType;

public class TextFile extends Entity {
	private String content = "";

	public TextFile(String name, Entity parent) {
		super(name, EntityType.TEXT_FILE, parent);
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
		updateLastModified();
	}
}