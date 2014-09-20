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
package org.encog.ml.genetic.mutate;

import java.util.Random;

import org.encog.ml.ea.genome.Genome;
import org.encog.ml.ea.opp.EvolutionaryOperator;
import org.encog.ml.ea.train.EvolutionaryAlgorithm;
import org.encog.ml.genetic.genome.ArrayGenome;

/**
 * A simple mutation where genes are shuffled. This mutation will not produce
 * repeated genes.
 */
public class MutateShuffle implements EvolutionaryOperator {

	/**
	 * The owner.
	 */
	private EvolutionaryAlgorithm owner;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init(final EvolutionaryAlgorithm theOwner) {
		this.owner = theOwner;
	}

	/**
	 * @return The number of offspring produced, which is 1 for this mutation.
	 */
	@Override
	public int offspringProduced() {
		return 1;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int parentsNeeded() {
		return 1;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void performOperation(final Random rnd, final Genome[] parents,
			final int parentIndex, final Genome[] offspring,
			final int offspringIndex) {
		final ArrayGenome parent = (ArrayGenome) parents[parentIndex];
		offspring[offspringIndex] = this.owner.getPopulation()
				.getGenomeFactory().factor();
		final ArrayGenome child = (ArrayGenome) offspring[offspringIndex];

		child.copy(parent);

		final int length = parent.size();
		int iswap1 = (int) (rnd.nextDouble() * length);
		int iswap2 = (int) (rnd.nextDouble() * length);

		// can't be equal
		if (iswap1 == iswap2) {
			// move to the next, but
			// don't go out of bounds
			if (iswap1 > 0) {
				iswap1--;
			} else {
				iswap1++;
			}

		}

		// make sure they are in the right order
		if (iswap1 > iswap2) {
			final int temp = iswap1;
			iswap1 = iswap2;
			iswap2 = temp;
		}

		child.swap(iswap1, iswap2);
	}

}
