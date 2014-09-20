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
package org.encog.neural.som.training.basic.neighborhood;

import org.encog.mathutil.rbf.GaussianFunction;
import org.encog.mathutil.rbf.InverseMultiquadricFunction;
import org.encog.mathutil.rbf.MexicanHatFunction;
import org.encog.mathutil.rbf.MultiquadricFunction;
import org.encog.mathutil.rbf.RBFEnum;
import org.encog.mathutil.rbf.RadialBasisFunction;
import org.encog.util.EngineArray;

/**
 * Implements a multi-dimensional RBF neighborhood function.  
 *
 */
public class NeighborhoodRBF implements NeighborhoodFunction {

	/**
	 * The radial basis function to use.
	 */
	private RadialBasisFunction rbf;

	/**
	 * The size of each dimension.
	 */
	private final int[] size;

	/**
	 * The displacement of each dimension, when mapping the dimensions
	 * to a 1d array.
	 */
	private int[] displacement;

	/**
	 * Construct a 2d neighborhood function based on the sizes for the
	 * x and y dimensions.
	 * @param type The RBF type to use.
	 * @param x The size of the x-dimension.
	 * @param y The size of the y-dimension.
	 */
	public NeighborhoodRBF(final RBFEnum type, final int x, final int y) {
		final int[] size = new int[2];
		size[0] = x;
		size[1] = y;

		final double[] centerArray = new double[2];
		centerArray[0] = 0;
		centerArray[1] = 0;

		final double[] widthArray = new double[2];
		widthArray[0] = 1;
		widthArray[1] = 1;

		switch (type) {
		case Gaussian:
			this.rbf = new GaussianFunction(2);
			break;
		case InverseMultiquadric:
			this.rbf = new InverseMultiquadricFunction(2);
			break;
		case Multiquadric:
			this.rbf = new MultiquadricFunction(2);
			break;
		case MexicanHat:
			this.rbf = new MexicanHatFunction(2);
			break;
		}

		this.rbf.setWidth(1);
		EngineArray.arrayCopy(centerArray, this.rbf.getCenters());

		this.size = size;

		calculateDisplacement();
	}

	/**
	 * Construct a multi-dimensional neighborhood function.
	 * @param size The sizes of each dimension.
	 * @param type The RBF type to use.
	 */
	public NeighborhoodRBF(final int[] size, final RBFEnum type) {
		switch (type) {
		case Gaussian:
			this.rbf = new GaussianFunction(size.length);
			break;
		case InverseMultiquadric:
			this.rbf = new InverseMultiquadricFunction(size.length);
			break;
		case Multiquadric:
			this.rbf = new MultiquadricFunction(size.length);
			break;
		case MexicanHat:
			this.rbf = new MexicanHatFunction(size.length);
			break;
		}
		this.size = size;
		calculateDisplacement();
	}

	/**
	 * Calculate all of the displacement values.
	 */
	private void calculateDisplacement() {
		this.displacement = new int[this.size.length];
		for (int i = 0; i < this.size.length; i++) {
			int value;

			if (i == 0) {
				value = 0;
			} else if (i == 1) {
				value = this.size[0];
			} else {
				value = this.displacement[i - 1] * this.size[i - 1];
			}

			this.displacement[i] = value;
		}
	}

	/**
	 * Calculate the value for the multi RBF function.
	 * @param currentNeuron The current neuron.
	 * @param bestNeuron The best neuron.
	 * @return A percent that determines the amount of training the current
	 * neuron should get.  Usually 100% when it is the bestNeuron.
	 */
	public double function(final int currentNeuron, final int bestNeuron) {
		final double[] vector = new double[this.displacement.length];
		final int[] vectorCurrent = translateCoordinates(currentNeuron);
		final int[] vectorBest = translateCoordinates(bestNeuron);
		for (int i = 0; i < vectorCurrent.length; i++) {
			vector[i] = vectorCurrent[i] - vectorBest[i];
		}
		return this.rbf.calculate(vector);

	}

	/**
	 * @return The radius.
	 */
	public double getRadius() {
		return this.rbf.getWidth();
	}

	/**
	 * @return The RBF to use.
	 */
	public RadialBasisFunction getRBF() {
		return this.rbf;
	}

	/**
	 * Set the radius.
	 * @param radius The radius.
	 */
	public void setRadius(final double radius) {
		this.rbf.setWidth(radius);
	}

	/**
	 * Translate the specified index into a set of multi-dimensional
	 * coordinates that represent the same index.  This is how the
	 * multi-dimensional coordinates are translated into a one dimensional
	 * index for the input neurons.
	 * @param index The index to translate.
	 * @return The multi-dimensional coordinates.
	 */
	private int[] translateCoordinates(final int index) {
		final int[] result = new int[this.displacement.length];
		int countingIndex = index;

		for (int i = this.displacement.length - 1; i >= 0; i--) {
			int value;
			if (this.displacement[i] > 0) {
				value = countingIndex / this.displacement[i];
			} else {
				value = countingIndex;
			}

			countingIndex -= this.displacement[i] * value;
			result[i] = value;

		}

		return result;
	}

}
