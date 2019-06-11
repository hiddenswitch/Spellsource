package com.hiddenswitch.deckgeneration;

import io.jenetics.*;
import io.jenetics.util.ISeq;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ActsOnSpecificChromosomesBasicMutator<
		G extends Gene<?, G>,
		C extends Comparable<? super C>
		> extends Mutator<G, C> {
	List<Integer> chromosomesToActOn;

	public ActsOnSpecificChromosomesBasicMutator(double probability, List<Integer> chromosomesToActOn) {
		super(probability);
		this.chromosomesToActOn = chromosomesToActOn;
	}

	public ActsOnSpecificChromosomesBasicMutator(List<Integer> chromosomesToActOn) {
		super(0.1);
		this.chromosomesToActOn = chromosomesToActOn;
	}

	@Override
	protected MutatorResult<Genotype<G>> mutate(
			final Genotype<G> genotype,
			final double p,
			final Random random
	) {
		List<MutatorResult<Chromosome<G>>> mutatorResults = new ArrayList<>();
		for (int i = 0; i < genotype.length(); i++) {
			if (chromosomesToActOn.contains(i)) {
				mutatorResults.add(mutate(genotype.getChromosome(i), p, random));
			} else {
				mutatorResults.add(MutatorResult.of(genotype.getChromosome(i)));
			}
		}
		final ISeq<MutatorResult<Chromosome<G>>> result = ISeq.of(mutatorResults);
		return MutatorResult.of(
				Genotype.of(result.map(MutatorResult::getResult)),
				result.stream().mapToInt(MutatorResult::getMutations).sum()
		);
	}
}
