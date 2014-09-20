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

import org.encog.ml.data.MLDataSet;
import org.encog.neural.freeform.FreeformConnection;
import org.encog.neural.freeform.FreeformNetwork;
import org.encog.neural.networks.training.propagation.TrainingContinuation;

public class FreeformBackPropagation extends FreeformPropagationTraining
		implements Serializable {

	/**
	 * The serial ID.
	 */
	private static final long serialVersionUID = 1L;

	private final double learningRate;
	private final double momentum;

	public FreeformBackPropagation(final FreeformNetwork theNetwork,
			final MLDataSet theTraining, final double theLearningRate,
			final double theMomentum) {
		super(theNetwork, theTraining);
		theNetwork.tempTrainingAllocate(1, 2);
		this.learningRate = theLearningRate;
		this.momentum = theMomentum;
	}

	@Override
	protected void learnConnection(final FreeformConnection connection) {
		final double gradient = connection.getTempTraining(0);
		final double delta = (gradient * this.learningRate)
				+ (connection.getTempTraining(1) * this.momentum);
		connection.setTempTraining(1, delta);
		connection.addWeight(delta);
	}

	@Override
	public TrainingContinuation pause() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void resume(final TrainingContinuation state) {
		// TODO Auto-generated method stub

	}

}
