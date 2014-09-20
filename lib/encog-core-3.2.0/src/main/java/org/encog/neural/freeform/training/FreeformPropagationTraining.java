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
package org.encog.neural.freeform.training;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.encog.engine.network.activation.ActivationSigmoid;
import org.encog.mathutil.error.ErrorCalculation;
import org.encog.ml.MLMethod;
import org.encog.ml.TrainingImplementationType;
import org.encog.ml.data.MLData;
import org.encog.ml.data.MLDataPair;
import org.encog.ml.data.MLDataSet;
import org.encog.ml.train.BasicTraining;
import org.encog.neural.freeform.FreeformConnection;
import org.encog.neural.freeform.FreeformNetwork;
import org.encog.neural.freeform.FreeformNeuron;
import org.encog.neural.freeform.task.ConnectionTask;

/**
 * Provides basic propagation functions to other trainers.
 */
public abstract class FreeformPropagationTraining extends BasicTraining
		implements Serializable {

	/**
	 * The serial ID.
	 */
	private static final long serialVersionUID = 1L;

	public static final double FLAT_SPOT_CONST = 0.1;
	private final FreeformNetwork network;
	private final MLDataSet training;
	private int iterationCount;
	private double error;
	private final Set<FreeformNeuron> visited = new HashSet<FreeformNeuron>();
	private boolean fixFlatSopt = true;
	
	/**
	 * The batch size. Specify 1 for pure online training. Specify 0 for pure
	 * batch training (complete training set in one batch). Otherwise specify
	 * the batch size for batch training.
	 */
	private int batchSize = 0;

	/**
	 * Don't use this constructor, it is for serialization only.
	 */
	public FreeformPropagationTraining() {
		super(TrainingImplementationType.Iterative);
		this.network = null;
		this.training = null;
	}
	
	public FreeformPropagationTraining(final FreeformNetwork theNetwork,
			final MLDataSet theTraining) {
		super(TrainingImplementationType.Iterative);
		this.network = theNetwork;
		this.training = theTraining;
	}

	private void calculateNeuronGradient(final FreeformNeuron toNeuron) {

		// Only calculate if layer has inputs, because we've already handled the
		// output
		// neurons, this means a hidden layer.
		if (toNeuron.getInputSummation() != null) {

			// between the layer deltas between toNeuron and the neurons that
			// feed toNeuron.
			// also calculate all inbound gradeints to toNeuron
			for (final FreeformConnection connection : toNeuron
					.getInputSummation().list()) {

				// calculate the gradient
				final double gradient = connection.getSource().getActivation()
						* toNeuron.getTempTraining(0);
				connection.addTempTraining(0, gradient);

				// calculate the next layer delta
				final FreeformNeuron fromNeuron = connection.getSource();
				double sum = 0;
				for (final FreeformConnection toConnection : fromNeuron
						.getOutputs()) {
					sum += toConnection.getTarget().getTempTraining(0)
							* toConnection.getWeight();
				}
				final double neuronOutput = fromNeuron.getActivation();
				final double neuronSum = fromNeuron.getSum();
				double deriv = toNeuron.getInputSummation()
						.getActivationFunction()
						.derivativeFunction(neuronSum, neuronOutput);

				if (this.fixFlatSopt
						&& (toNeuron.getInputSummation()
								.getActivationFunction() instanceof ActivationSigmoid)) {
					deriv += FreeformPropagationTraining.FLAT_SPOT_CONST;
				}

				final double layerDelta = sum * deriv;
				fromNeuron.setTempTraining(0, layerDelta);
			}

			// recurse to the next level
			for (final FreeformConnection connection : toNeuron
					.getInputSummation().list()) {
				final FreeformNeuron fromNeuron = connection.getSource();
				calculateNeuronGradient(fromNeuron);
			}

		}

	}

	private void calculateOutputDelta(final FreeformNeuron neuron,
			final double diff) {
		final double neuronOutput = neuron.getActivation();
		final double neuronSum = neuron.getInputSummation().getSum();
		double deriv = neuron.getInputSummation().getActivationFunction()
				.derivativeFunction(neuronSum, neuronOutput);
		if (this.fixFlatSopt
				&& (neuron.getInputSummation().getActivationFunction() instanceof ActivationSigmoid)) {
			deriv += FreeformPropagationTraining.FLAT_SPOT_CONST;
		}
		final double layerDelta = deriv * diff;
		neuron.setTempTraining(0, layerDelta);
	}

	@Override
	public boolean canContinue() {
		return false;
	}

	@Override
	public void finishTraining() {
		this.network.tempTrainingClear();
	}

	@Override
	public double getError() {
		return this.error;
	}

	@Override
	public TrainingImplementationType getImplementationType() {
		return TrainingImplementationType.Iterative;
	}

	@Override
	public int getIteration() {
		return this.iterationCount;
	}

	@Override
	public MLMethod getMethod() {
		return this.network;
	}

	@Override
	public MLDataSet getTraining() {
		return this.training;
	}

	public boolean isFixFlatSopt() {
		return this.fixFlatSopt;
	}

	@Override
	public void iteration() {
		preIteration();
		this.iterationCount++;
		this.network.clearContext();
		
		if (this.batchSize == 0) {
			processPureBatch();
		} else {
			processBatches();
		}
		
		postIteration();
	}

	@Override
	public void iteration(final int count) {
		for (int i = 0; i < count; i++) {
			this.iteration();
		}

	}
	
	protected void processPureBatch() {
		final ErrorCalculation errorCalc = new ErrorCalculation();
		this.visited.clear();

		for (final MLDataPair pair : this.training) {
			final MLData input = pair.getInput();
			final MLData ideal = pair.getIdeal();
			final MLData actual = this.network.compute(input);
			final double sig = pair.getSignificance();

			errorCalc.updateError(actual.getData(), ideal.getData(), sig);

			for (int i = 0; i < this.network.getOutputCount(); i++) {
				final double diff = (ideal.getData(i) - actual.getData(i))
						* sig;
				final FreeformNeuron neuron = this.network.getOutputLayer()
						.getNeurons().get(i);
				calculateOutputDelta(neuron, diff);
				calculateNeuronGradient(neuron);
			}
		}

		// Set the overall error.
		setError(errorCalc.calculate());
		
		// Learn for all data.
		learn();		
	}
	
	protected void processBatches() {
		int lastLearn = 0;
		final ErrorCalculation errorCalc = new ErrorCalculation();
		this.visited.clear();

		for (final MLDataPair pair : this.training) {
			final MLData input = pair.getInput();
			final MLData ideal = pair.getIdeal();
			final MLData actual = this.network.compute(input);
			final double sig = pair.getSignificance();

			errorCalc.updateError(actual.getData(), ideal.getData(), sig);

			for (int i = 0; i < this.network.getOutputCount(); i++) {
				final double diff = (ideal.getData(i) - actual.getData(i))
						* sig;
				final FreeformNeuron neuron = this.network.getOutputLayer()
						.getNeurons().get(i);
				calculateOutputDelta(neuron, diff);
				calculateNeuronGradient(neuron);
			}
			
			// Are we at the end of a batch.
			lastLearn++;
			if( lastLearn>=this.batchSize ) {
				lastLearn = 0;
				learn();	
			}
		}
		
		// Handle any remaining data.
		if( lastLearn>0 ) {
			learn();
		}

		// Set the overall error.
		setError(errorCalc.calculate());
		
	}
	
	protected void learn() {
		this.network.performConnectionTask(new ConnectionTask() {
			@Override
			public void task(final FreeformConnection connection) {
				learnConnection(connection);
				connection.setTempTraining(0, 0);
			}
		});
	}

	protected abstract void learnConnection(FreeformConnection connection);

	@Override
	public void setError(final double theError) {
		this.error = theError;

	}

	public void setFixFlatSopt(final boolean fixFlatSopt) {
		this.fixFlatSopt = fixFlatSopt;
	}

	@Override
	public void setIteration(final int iteration) {
		this.iterationCount = iteration;
	}

	public int getBatchSize() {
		return batchSize;
	}

	public void setBatchSize(int batchSize) {
		this.batchSize = batchSize;
	}
	
}
