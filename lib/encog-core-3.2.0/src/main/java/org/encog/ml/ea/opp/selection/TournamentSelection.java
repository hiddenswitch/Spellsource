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
package org.encog.ml.ea.opp.selection;

import java.io.Serializable;
import java.util.Random;

import org.encog.mathutil.randomize.RangeRandomizer;
import org.encog.ml.ea.genome.Genome;
import org.encog.ml.ea.species.Species;
import org.encog.ml.ea.train.EvolutionaryAlgorithm;
import org.encog.ml.ea.train.basic.BasicEA;

/**
 * Tournament select can be used to select a fit (or unfit) genome from a
 * species. The selection is run a set number of rounds. Each round two random
 * participants are chosen. The more fit participant continues to the next
 * round.
 * 
 * http://en.wikipedia.org/wiki/Tournament_selection
 * 
 */
public class TournamentSelection implements SelectionOperator, Serializable {
	
	/**
	 * The serial id.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * The trainer being used.
	 */
	private EvolutionaryAlgorithm trainer;

	/**
	 * The number of rounds.
	 */
	private int rounds;

	/**
	 * Construct a tournament selection.
	 * @param theTrainer The trainer.
	 * @param theRounds The number of rounds to use.
	 */
	public TournamentSelection(final EvolutionaryAlgorithm theTrainer,
			final int theRounds) {
		this.trainer = theTrainer;
		this.rounds = theRounds;
	}

	/**
	 * @return The number of rounds.
	 */
	public int getRounds() {
		return this.rounds;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public EvolutionaryAlgorithm getTrainer() {
		return this.trainer;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int performAntiSelection(final Random rnd, final Species species) {
		int worstIndex = rnd.nextInt(species.getMembers().size());
		Genome worst = species.getMembers().get(worstIndex);
		BasicEA.calculateScoreAdjustment(worst,
				this.trainer.getScoreAdjusters());

		for (int i = 0; i < this.rounds; i++) {
			final int competitorIndex = rnd.nextInt(species.getMembers().size());
			final Genome competitor = species.getMembers().get(competitorIndex);

			// force an invalid genome to lose
			if (Double.isInfinite(competitor.getAdjustedScore())
					|| Double.isNaN(competitor.getAdjustedScore())) {
				return competitorIndex;
			}

			BasicEA.calculateScoreAdjustment(competitor,
					this.trainer.getScoreAdjusters());
			if (!this.trainer.getSelectionComparator().isBetterThan(competitor,
					worst)) {
				worst = competitor;
				worstIndex = competitorIndex;
			}
		}
		return worstIndex;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int performSelection(final Random rnd, final Species species) {
		int bestIndex = rnd.nextInt(species.getMembers().size());
		Genome best = species.getMembers().get(bestIndex);
		BasicEA.calculateScoreAdjustment(best, this.trainer.getScoreAdjusters());

		for (int i = 0; i < this.rounds; i++) {
			final int competitorIndex = rnd.nextInt(species.getMembers().size());
			final Genome competitor = species.getMembers().get(competitorIndex);

			// only evaluate valid genomes
			if (!Double.isInfinite(competitor.getAdjustedScore())
					&& !Double.isNaN(competitor.getAdjustedScore())) {
				BasicEA.calculateScoreAdjustment(competitor,
						this.trainer.getScoreAdjusters());
				if (this.trainer.getSelectionComparator().isBetterThan(
						competitor, best)) {
					best = competitor;
					bestIndex = competitorIndex;
				}
			}
		}
		return bestIndex;
	}

	/**
	 * Set the number of rounds.
	 * @param rounds The number of rounds.
	 */
	public void setRounds(final int rounds) {
		this.rounds = rounds;
	}

	/**
	 * Set the trainer.
	 * @param trainer The trainer.
	 */
	public void setTrainer(final EvolutionaryAlgorithm trainer) {
		this.trainer = trainer;
	}
}
