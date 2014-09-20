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
package org.encog.neural.networks.training;

import junit.framework.TestCase;

import org.encog.ml.MLRegression;
import org.encog.ml.data.MLDataSet;
import org.encog.ml.data.folded.FoldedDataSet;
import org.encog.ml.train.MLTrain;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.NetworkUtil;
import org.encog.neural.networks.XOR;
import org.encog.neural.networks.training.cross.CrossValidationKFold;
import org.encog.neural.networks.training.propagation.resilient.ResilientPropagation;
import org.encog.util.simple.EncogUtility;
import org.junit.Test;

public class TestFolded extends TestCase {
	@Test
	public void testRPROP() throws Throwable
	{
		MLDataSet trainingData = XOR.createNoisyXORDataSet(10);
		
		BasicNetwork network = NetworkUtil.createXORNetworkUntrained();
		
		final FoldedDataSet folded = new FoldedDataSet(trainingData); 
		final MLTrain train = new ResilientPropagation(network, folded);
		final CrossValidationKFold trainFolded = new CrossValidationKFold(train,4);
		
		EncogUtility.trainToError(trainFolded, 0.2);
		
		XOR.verifyXOR((MLRegression)trainFolded.getMethod(), 0.2);
		
	}
}
