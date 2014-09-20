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
package org.encog.ml.ea.opp;

import java.util.Random;

import org.encog.ml.ea.genome.Genome;
import org.encog.ml.ea.train.EvolutionaryAlgorithm;

/**
 * An evolutionary operator is used to create new offspring genomes based on
 * parent genomes. There are a variety of means by which this can be done. The
 * number of parents required, as well as the number of offspring produced are
 * dependent on the operator. This interface defines key characteristics that
 * all operators must share.
 * 
 * Most operators do not modify the parents. However, some mutation operators do
 * require that the children and parent array be the same. If the children and
 * parent arrays are the same, then the parent will be mutated.
 */
public interface EvolutionaryOperator {
	/**
	 * Called to setup the evolutionary operator.
	 * 
	 * @param theOwner
	 *            The evolutionary algorithm used with this operator.
	 */
	void init(EvolutionaryAlgorithm theOwner);

	/**
	 * @return The number of offspring produced by this type of crossover.
	 */
	int offspringProduced();

	/**
	 * @return The number of parents needed.
	 */
	int parentsNeeded();

	/**
	 * Perform the evolutionary operation.
	 * 
	 * @param rnd
	 *            A random number generator.
	 * @param parents
	 *            The parents.
	 * @param parentIndex
	 *            The index into the parents array.
	 * @param offspring
	 *            The offspring.
	 * @param offspringIndex
	 *            An index into the offspring array.
	 */
	void performOperation(Random rnd, Genome[] parents, int parentIndex,
			Genome[] offspring, int offspringIndex);
}
