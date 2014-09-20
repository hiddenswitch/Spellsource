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
package org.encog.neural.data.basic;

import org.encog.ml.data.basic.BasicMLDataPair;
import org.encog.neural.data.NeuralData;

/**
 * This is an alias class for Encog 2.5 compatibility. This class aliases
 * BasicMLDataPair. Newer code should use BasicMLDataPair in place of this
 * class.
 */
public class BasicNeuralDataPair extends BasicMLDataPair {

	/**
	 * 
	 */
	private static final long serialVersionUID = 93850721522898707L;

	/**
	 * Construct with input only.
	 * 
	 * @param theInput
	 *            The input.
	 */
	public BasicNeuralDataPair(final NeuralData theInput) {
		super(theInput);
	}

	/**
	 * Construct from input and ideal.
	 * 
	 * @param theInput
	 *            The input.
	 * @param theIdeal
	 *            The ideal.
	 */
	public BasicNeuralDataPair(final NeuralData theInput,
			final NeuralData theIdeal) {
		super(theInput, theIdeal);
	}

}
