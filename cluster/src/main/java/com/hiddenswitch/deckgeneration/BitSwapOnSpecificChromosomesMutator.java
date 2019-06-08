package com.hiddenswitch.deckgeneration;

import io.jenetics.Chromosome;
import io.jenetics.Gene;
import io.jenetics.Genotype;
import io.jenetics.MutatorResult;
import io.jenetics.internal.math.probability;
import io.jenetics.util.ISeq;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BitSwapOnSpecificChromosomesMutator<
		G extends Gene<Boolean, G>,
		C extends Comparable<? super C>
		>
		extends BitSwapMutator<G, C> {
	List<Integer> chromosomesToActOn;

	public BitSwapOnSpecificChromosomesMutator(double probability, List<Integer> chromosomesToActOn) {
		super(probability);
		this.chromosomesToActOn = chromosomesToActOn;
	}

	public BitSwapOnSpecificChromosomesMutator(List<Integer> chromosomesToActOn) {
		this.chromosomesToActOn = chromosomesToActOn;
	}

	@Override
	protected MutatorResult<Genotype<G>> mutate(
			final Genotype<G> genotype,
			final double p,
			final Random random
	) {
		final int P = probability.toInt(p);
		List<MutatorResult<Chromosome<G>>> mutatorResults = new ArrayList<>();
		for (int i = 0; i < genotype.length(); i++) {
			if (chromosomesToActOn.contains(i) && random.nextInt() < P) {
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
