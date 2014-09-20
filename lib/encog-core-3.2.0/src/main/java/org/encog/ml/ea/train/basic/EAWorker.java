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
package org.encog.ml.ea.train.basic;

import java.util.Random;
import java.util.concurrent.Callable;

import org.encog.EncogError;
import org.encog.ml.ea.exception.EARuntimeError;
import org.encog.ml.ea.genome.Genome;
import org.encog.ml.ea.opp.EvolutionaryOperator;
import org.encog.ml.ea.species.Species;

/**
 * A worker thread for an Evolutionary Algorithm.
 */
public class EAWorker implements Callable<Object> {

	/**
	 * The species being processed.
	 */
	private final Species species;

	/**
	 * The parent genomes.
	 */
	private final Genome[] parents;

	/**
	 * The children genomes.
	 */
	private final Genome[] children;

	/**
	 * Random number generator.
	 */
	private final Random rnd;

	/**
	 * The parent object.
	 */
	private final BasicEA train;

	/**
	 * Construct the EA worker.
	 * 
	 * @param theTrain
	 *            The trainer.
	 * @param theSpecies
	 *            The species.
	 */
	public EAWorker(final BasicEA theTrain, final Species theSpecies) {
		this.train = theTrain;
		this.species = theSpecies;
		this.rnd = this.train.getRandomNumberFactory().factor();

		this.parents = new Genome[this.train.getOperators().maxParents()];
		this.children = new Genome[this.train.getOperators().maxOffspring()];
	}

	/**
	 * Choose a parent.
	 * 
	 * @return The chosen parent.
	 */
	private Genome chooseParent() {
		final int idx = this.train.getSelection().performSelection(this.rnd,
				this.species);
		return this.species.getMembers().get(idx);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object call() {
		boolean success = false;
		int tries = this.train.getMaxOperationErrors();
		do {
			try {
				// choose an evolutionary operation (i.e. crossover or a type of
				// mutation) to use
				final EvolutionaryOperator opp = this.train.getOperators()
						.pickMaxParents(this.rnd,
								this.species.getMembers().size());

				this.children[0] = null;

				// prepare for either sexual or asexual reproduction either way,
				// we
				// need at least
				// one parent, which is the first parent.
				//
				// Chose the first parent, there must be at least one genome in
				// this
				// species
				this.parents[0] = chooseParent();

				// if the number of individuals in this species is only
				// one then we can only clone and perhaps mutate, otherwise use
				// the crossover probability to determine if we are to use
				// sexual reproduction.
				if (opp.parentsNeeded() > 1) {

					int numAttempts = 5;

					this.parents[1] = chooseParent();
					while (this.parents[0] == this.parents[1]
							&& numAttempts-- > 0) {
						this.parents[1] = chooseParent();
					}

					// success, perform crossover
					if (this.parents[0] != this.parents[1]) {
						opp.performOperation(this.rnd, this.parents, 0,
								this.children, 0);
					}
				} else {
					// clone a child (asexual reproduction)
					opp.performOperation(this.rnd, this.parents, 0,
							this.children, 0);
					this.children[0].setPopulation(this.parents[0]
							.getPopulation());
				}

				// process the new child
				for (Genome child : this.children) {
					if (child != null) {
						child.setPopulation(this.parents[0].getPopulation());
						if (this.train.getRules().isValid(child)) {
							child.setBirthGeneration(this.train.getIteration());

							this.train.calculateScore(child);
							if (!this.train.addChild(child)) {
								return null;
							}
							success = true;
						}
					}
				}
			} catch (EARuntimeError e) {
				tries--;
				if (tries < 0) {
					throw new EncogError(
							"Could not perform a successful genetic operaton after "
									+ this.train.getMaxOperationErrors()
									+ " tries.");
				}
			} catch (final Throwable t) {
				if (!this.train.getShouldIgnoreExceptions()) {
					this.train.reportError(t);
				}
			}

		} while (!success);
		return null;
	}
}
