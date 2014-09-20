/*
 * Encog(tm) Core v3.2 - Java Version
 * http://www.heatonresearch.com/encog/
 * https://github.com/encog/encog-java-core
 
 * Copyright 2008-2013 Heaton Research, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *   
 * For more information on Heaton Research copyrights, licenses 
 * and trademarks visit:
 * http://www.heatonresearch.com/copyright
 */
package org.encog.util.normalize.input;

import java.io.File;

/**
 * An input field based on a CSV file.
 */
public class InputFieldCSV extends BasicInputField {

	/**
	 * The file to read.
	 */
	private File file;

	/**
	 * The CSV column represented by this field.
	 */	
	private int offset;

	/**
	 * Construct an InputFieldCSV with the default constructor.  This is mainly
	 * used for reflection.
	 */
	public InputFieldCSV() {

	}

	/**
	 * Construct a input field for a CSV file.
	 * @param usedForNetworkInput True if this field is used for actual input
	 * to the neural network, as opposed to segregation only.
	 * @param file The tile to read.
	 * @param offset The CSV file column to read.
	 */
	public InputFieldCSV(final boolean usedForNetworkInput, final File file,
			final int offset) {
		this.file = file;
		this.offset = offset;
		setUsedForNetworkInput(usedForNetworkInput);
	}

	/**
	 * @return The file being read.
	 */
	public File getFile() {
		return this.file;
	}

	/**
	 * @return The column in this CSV file to read.
	 */
	public int getOffset() {
		return this.offset;
	}
}
