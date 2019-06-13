package com.hiddenswitch.deckgeneration;

import io.jenetics.AltererResult;
import io.jenetics.Gene;
import io.jenetics.Mutator;
import io.jenetics.Phenotype;
import io.jenetics.util.RandomRegistry;
import io.jenetics.util.Seq;

import java.util.List;
import java.util.Random;

public class UmbrellaMutator<
		G extends Gene<?, G>,
		C extends Comparable<? super C>
		> extends Mutator<G, C> {
	List<Mutator> mutators;

	/**
	 * @param mutators A list of mutators, one of which (each with equal probability)
	 *                 will be called to mutate the given population
	 */
	public UmbrellaMutator(List<Mutator> mutators) {
		this.mutators = mutators;
	}

	/**
	 * Randomly call one of the mutators to mutate the given population
	 *
	 * @param population The population to be mutated
	 * @param generation The current generation of the population
	 * @return
	 */
	@Override
	public AltererResult<G, C> alter(Seq<Phenotype<G, C>> population, long generation) {
		Random random = RandomRegistry.getRandom();
		int index = random.nextInt(mutators.size());
		return mutators.get(index).alter(population, generation);
	}
}
