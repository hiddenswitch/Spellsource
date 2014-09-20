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
package org.encog.app.analyst.csv.segregate;

import java.io.File;

/**
 * Specifies a segregation target, and what percent that target should need.
 * 
 */
public class SegregateTargetPercent {

	/**
	 * Percent that this target should get.
	 */
	private int percent;

	/**
	 * Used internally to track the number of items remaining for this target.
	 */
	private int numberRemaining;

	/**
	 * Used internally to hold the target filename.
	 */
	private File filename;

	/**
	 * Construct the object.
	 * 
	 * @param outputFile
	 *            The output filename.
	 * @param thePercent
	 *            The target percent.
	 */
	public SegregateTargetPercent(final File outputFile, 
			final int thePercent) {
		this.percent = thePercent;
		this.filename = outputFile;
	}

	/**
	 * @return the filename
	 */
	public File getFilename() {
		return this.filename;
	}

	/**
	 * @return the numberRemaining
	 */
	public int getNumberRemaining() {
		return this.numberRemaining;
	}

	/**
	 * @return the percent
	 */
	public int getPercent() {
		return this.percent;
	}

	/**
	 * @param theFilename
	 *            the filename to set
	 */
	public void setFilename(final File theFilename) {
		this.filename = theFilename;
	}

	/**
	 * @param theNumberRemaining
	 *            the numberRemaining to set
	 */
	public void setNumberRemaining(final int theNumberRemaining) {
		this.numberRemaining = theNumberRemaining;
	}

	/**
	 * @param thePercent
	 *            the percent to set
	 */
	public void setPercent(final int thePercent) {
		this.percent = thePercent;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder("[");
		result.append(getClass().getSimpleName());
		result.append(" filename=");
		result.append(this.filename.toString());
		result.append(", percent=");
		result.append(this.percent);

		result.append("]");
		return result.toString();
	}

}
