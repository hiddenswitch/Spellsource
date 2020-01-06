package com.hiddenswitch.spellsource.core;

import java.io.InputStream;

/**
 * Data object that holds a filename, inputstream and a boolean flag to indicate if the file is on the filesystem or in
 * the applications resource bundled dir.
 */
public class ResourceInputStream implements CardResource {

	private final String fileName;
	private final InputStream inputStream;
	private final boolean fromFilesystem;

	public ResourceInputStream(String fileName, InputStream inputStream, boolean filesystem) {
		this.fileName = fileName;
		this.inputStream = inputStream;
		this.fromFilesystem = filesystem;
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