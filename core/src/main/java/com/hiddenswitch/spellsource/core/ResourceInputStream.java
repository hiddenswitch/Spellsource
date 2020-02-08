package com.hiddenswitch.spellsource.core;

import java.io.InputStream;

/**
 * Data object that holds a filename and an input stream.
 */
public class ResourceInputStream implements CardResource {

	private final String fileName;
	private final InputStream inputStream;

	public ResourceInputStream(String fileName, InputStream inputStream) {
		this.fileName = fileName;
		this.inputStream = inputStream;
	}

	@Override
	public String getFileName() {
		return fileName;
	}

	@Override
	public InputStream getInputStream() {
		return inputStream;
	}

}