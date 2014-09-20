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
package org.encog.neural.pattern;

import org.encog.engine.network.activation.ActivationFunction;
import org.encog.ml.MLMethod;
import org.encog.ml.svm.KernelType;
import org.encog.ml.svm.SVM;
import org.encog.ml.svm.SVMType;

/**
 * A pattern to create support vector machines.
 *
 */
public class SVMPattern implements NeuralNetworkPattern {
	/**
	 * The number of neurons in the first layer.
	 */
	private int inputNeurons;

	/**
	 * The number of neurons in the second layer.
	 */
	private int outputNeurons;

	/**
	 * True, if using regression.
	 */
	private boolean regression = true;

	/**
	 * The kernel type.
	 */
	private KernelType kernelType = KernelType.RadialBasisFunction;
	
	/**
	 * The SVM type.
	 */
	private SVMType svmType = SVMType.EpsilonSupportVectorRegression;

	/**
	 * Unused, a BAM has no hidden layers.
	 * 
	 * @param count
	 *            Not used.
	 */
	@Override
	public void addHiddenLayer(final int count) {
		throw new PatternError("A SVM network has no hidden layers.");
	}

	/**
	 * Clear any settings on the pattern.
	 */
	@Override
	public void clear() {
		this.inputNeurons = 0;
		this.outputNeurons = 0;

	}

	/**
	 * @return The generated network.
	 */
	@Override
	public MLMethod generate() {
		if (this.outputNeurons != 1) {
			throw new PatternError("A SVM may only have one output.");
		}
		final SVM network = new SVM(this.inputNeurons, this.svmType,
				this.kernelType);
		return network;
	}

	/**
	 * @return The input neuron count.
	 */
	public int getInputNeurons() {
		return this.inputNeurons;
	}

	/**
	 * @return The input output count.
	 */
	public int getOutputNeurons() {
		return this.outputNeurons;
	}

	/**
	 * @return True, if this is regression.
	 */
	public boolean isRegression() {
		return this.regression;
	}

	/**
	 * Not used, the BAM uses a bipoloar activation function.
	 * 
	 * @param activation
	 *            Not used.
	 */
	@Override
	public void setActivationFunction(
			final ActivationFunction activation) {
		throw new PatternError(
				"A SVM network can't specify a custom activation function.");
	}

	/**
	 * Set the number of input neurons.
	 * 
	 * @param count
	 *            The number of input neurons.
	 */
	@Override
	public void setInputNeurons(final int count) {
		this.inputNeurons = count;
	}

	/**
	 * Set the kernel type.
	 * @param kernelType The kernel type.
	 */
	public void setKernelType(final KernelType kernelType) {
		this.kernelType = kernelType;
	}

	/**
	 * Set the number of output neurons.
	 * 
	 * @param count
	 *            The output neuron count.
	 */
	@Override
	public void setOutputNeurons(final int count) {
		this.outputNeurons = count;
	}

	/**
	 * Set if regression is used.
	 * @param regression True if regression is used.
	 */
	public void setRegression(final boolean regression) {
		this.regression = regression;
	}

	/**
	 * Set the SVM type.
	 * @param svmType The SVM type.
	 */
	public void setSVMType(final SVMType svmType) {
		this.svmType = svmType;
	}
}
