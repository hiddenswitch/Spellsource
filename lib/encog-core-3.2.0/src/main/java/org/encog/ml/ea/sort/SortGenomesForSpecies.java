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
package org.encog.ml.ea.sort;

import java.util.Comparator;

import org.encog.ml.ea.genome.Genome;
import org.encog.ml.ea.train.EvolutionaryAlgorithm;

/**
 * Sort the gnomes for species.  Sort first by score, second by birth generation.
 * This favors younger genomes if scores are equal.
 */
public class SortGenomesForSpecies implements Comparator<Genome> {

	/**
	 * The trainer.
	 */
	private final EvolutionaryAlgorithm train;

	/**
	 * Construct the comparator.
	 * @param theTrain The trainer.
	 */
	public SortGenomesForSpecies(final EvolutionaryAlgorithm theTrain) {
		this.train = theTrain;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int compare(Genome g1, Genome g2) {
		final int result = this.train.getSelectionComparator().compare(g1, g2);

		if (result != 0) {
			return result;
		}

		return g2.getBirthGeneration() - g1.getBirthGeneration();
	}

}
