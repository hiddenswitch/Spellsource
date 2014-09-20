package org.encog.neural.networks.training;

import java.io.File;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.encog.Encog;
import org.encog.ml.CalculateScore;
import org.encog.ml.data.MLDataSet;
import org.encog.ml.data.basic.BasicMLData;
import org.encog.ml.data.basic.BasicMLDataPair;
import org.encog.ml.data.basic.BasicMLDataSet;
import org.encog.ml.data.buffer.BufferedMLDataSet;
import org.encog.ml.ea.train.EvolutionaryAlgorithm;
import org.encog.neural.neat.NEATNetwork;
import org.encog.neural.neat.NEATPopulation;
import org.encog.neural.neat.NEATUtil;
import org.encog.neural.networks.XOR;
import org.encog.util.TempDir;
import org.encog.util.simple.EncogUtility;

public class TestNEAT extends TestCase {
	public final TempDir TEMP_DIR = new TempDir();
	public final File EGB_FILENAME = TEMP_DIR.createFile("encogtest.egb");

	public void testNEATBuffered() {
		BufferedMLDataSet buffer = new BufferedMLDataSet(EGB_FILENAME);
		buffer.beginLoad(2, 1);
		for(int i=0;i<XOR.XOR_INPUT.length;i++) {
			buffer.add(new BasicMLDataPair(
					new BasicMLData(XOR.XOR_INPUT[i]),
					new BasicMLData(XOR.XOR_IDEAL[i])));
		}
		buffer.endLoad();
		
		NEATPopulation pop = new NEATPopulation(2,1,1000);
		pop.setInitialConnectionDensity(1.0);// not required, but speeds training
		pop.reset();

		CalculateScore score = new TrainingSetScore(buffer);
		// train the neural network
		
		final EvolutionaryAlgorithm train = NEATUtil.constructNEATTrainer(pop,score);
		
		do {
			train.iteration();
		} while(train.getError() > 0.01 && train.getIteration()<10000);
		Encog.getInstance().shutdown();
		NEATNetwork network = (NEATNetwork)train.getCODEC().decode(train.getBestGenome());
		
		Assert.assertTrue(train.getError()<0.01);
		Assert.assertTrue(network.calculateError(buffer)<0.01);
	}
	
	public void testNEAT() {
		MLDataSet trainingSet = new BasicMLDataSet(XOR.XOR_INPUT, XOR.XOR_IDEAL);
		NEATPopulation pop = new NEATPopulation(2,1,1000);
		pop.setInitialConnectionDensity(1.0);// not required, but speeds training
		pop.reset();

		CalculateScore score = new TrainingSetScore(trainingSet);
		// train the neural network
		
		final EvolutionaryAlgorithm train = NEATUtil.constructNEATTrainer(pop,score);
		
		do {
			train.iteration();
		} while(train.getError() > 0.01);

		// test the neural network
		Encog.getInstance().shutdown();
		Assert.assertTrue(train.getError()<0.01);
		NEATNetwork network = (NEATNetwork)train.getCODEC().decode(train.getBestGenome());
		Assert.assertTrue(network.calculateError(trainingSet)<0.01);
	}
}
