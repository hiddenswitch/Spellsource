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
package org.encog.ensemble.adaboost;

import java.util.ArrayList;

import org.encog.ensemble.Ensemble;
import org.encog.ensemble.EnsembleAggregator;
import org.encog.ensemble.EnsembleML;
import org.encog.ensemble.EnsembleMLMethodFactory;
import org.encog.ensemble.EnsembleTrainFactory;
import org.encog.ensemble.EnsembleTypes;
import org.encog.ensemble.EnsembleTypes.ProblemType;
import org.encog.ensemble.GenericEnsembleML;
import org.encog.ensemble.data.EnsembleDataSet;
import org.encog.ensemble.data.factories.ResamplingDataSetFactory;
import org.encog.mathutil.VectorAlgebra;
import org.encog.ml.data.MLDataPair;
import org.encog.ml.data.MLDataSet;
import org.encog.ml.train.MLTrain;

public class AdaBoost extends Ensemble {

	private int T;
	private VectorAlgebra va;
	private ArrayList<Double> weights;

	public AdaBoost(int iterationsT, int dataSetSize, EnsembleMLMethodFactory mlFactory, EnsembleTrainFactory trainFactory, EnsembleAggregator aggregator) {
		this.dataSetFactory = new ResamplingDataSetFactory(dataSetSize);
		this.T = iterationsT;
		this.mlFactory = mlFactory;
		this.va = new VectorAlgebra();
		this.weights = new ArrayList<Double>();
		this.members = new ArrayList<EnsembleML>();
		this.trainFactory = trainFactory;
		this.aggregator = aggregator;
	}

	@Override
	public void train(double targetAccuracy, double selectionError, EnsembleDataSet testset, boolean verbose) {
		ArrayList<Double> D = new ArrayList<Double>();
		int dss = dataSetFactory.getInputData().size();
		for (int k = 0; k < dss; k++)
			D.add(1.0 / (float) dss);
		for (int i = 0; i < T; i++) {
			dataSetFactory.setSignificance(D);
			MLDataSet thisSet = dataSetFactory.getNewDataSet();
			GenericEnsembleML newML = new GenericEnsembleML(mlFactory.createML(dataSetFactory.getInputData().getInputSize(), dataSetFactory.getInputData().getIdealSize()),mlFactory.getLabel());
			do {
				mlFactory.reInit(newML.getMl());
				MLTrain train = trainFactory.getTraining(newML.getMl(), thisSet);
				newML.setTraining(train);
				newML.train(targetAccuracy,verbose);
			} while (newML.getError(testset) > selectionError);
			double newWeight = getWeightedError(newML,thisSet);
			members.add(newML);
			weights.add(newWeight);
			D = updateD(newML,thisSet,D);
		}
	}

	private double epsilon(GenericEnsembleML ml, MLDataSet dataSet) {
		int bad = 0;
		for (MLDataPair data: dataSet) {
			if (ml.classify(data.getInput()) != ml.winner(data.getIdeal()))
				bad++;
		}
		return (float) bad / (float) dataSet.size();
	}

	private ArrayList<Double> updateD(GenericEnsembleML ml, MLDataSet dataSet, ArrayList<Double> D_t) {
		ArrayList<Double> D_tplus1 = new ArrayList<Double>();
		double epsilon = epsilon(ml, dataSet);
		double alpha_t = Math.log(1 - epsilon / epsilon);
		for (int i = 0; i < dataSet.size(); i++) {
			double D_tplus1_i = D_t.get(i) * Math.exp(-alpha_t * va.dotProduct(dataSet.get(i).getIdeal().getData(), ml.compute(dataSet.get(i).getInput()).getData()));
			D_tplus1.add(D_tplus1_i);
		}
		return D_tplus1;
	}

	@Override
	public void initMembers() {
		//This cannot do anything, as member generation is strictly linked to training!
	}

	private double getWeightedError(GenericEnsembleML newML, MLDataSet dataSet) {
		double sum = 0;
		for (int i = 0; i < dataSet.size(); i++) {
			MLDataPair currentData = dataSet.get(i);
			if (newML.classify(currentData.getInput()) != newML.winner(currentData.getIdeal()))
				sum += currentData.getSignificance();
		}
		return sum;
	}

	@Override
	public void addMember(EnsembleML newMember) throws NotPossibleInThisMethod {
		throw new NotPossibleInThisMethod();
	}

	@Override
	public ProblemType getProblemType() {
		return EnsembleTypes.ProblemType.CLASSIFICATION;
	}

}
