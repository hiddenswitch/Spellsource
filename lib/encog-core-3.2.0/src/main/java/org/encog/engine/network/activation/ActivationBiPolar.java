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
package org.encog.engine.network.activation;

import org.encog.ml.factory.MLActivationFactory;
import org.encog.util.obj.ActivationUtil;

/**
 * BiPolar activation function. This will scale the neural data into the bipolar
 * range. Greater than zero becomes 1, less than or equal to zero becomes -1.
 * 
 * @author jheaton
 * 
 */
public class ActivationBiPolar implements ActivationFunction {

	/**
	 * The serial id.
	 */
	private static final long serialVersionUID = -7166136514935838114L;

	/**
	 * The parameters.
	 */
	private final double[] params;

	/**
	 * Construct the bipolar activation function.
	 */
	public ActivationBiPolar() {
		this.params = new double[0];
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void activationFunction(final double[] x, final int start,
			final int size) {

		for (int i = start; i < start + size; i++) {
			if (x[i] > 0) {
				x[i] = 1;
			} else {
				x[i] = -1;
			}
		}
	}

	/**
	 * @return The object cloned.
	 */
	@Override
	public final ActivationFunction clone() {
		return new ActivationBiPolar();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final double derivativeFunction(double b, double a) {
		return 1;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final String[] getParamNames() {
		final String[] result = {};
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final double[] getParams() {
		return this.params;
	}

	/**
	 * @return Return true, bipolar has a 1 for derivative.
	 */
	@Override
	public final boolean hasDerivative() {
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void setParam(final int index, final double value) {
		this.params[index] = value;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getFactoryCode() {
		return ActivationUtil.generateActivationFactory(MLActivationFactory.AF_BIPOLAR, this);
	}
}
