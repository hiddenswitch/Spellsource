package com.hiddenswitch.deckgeneration;

/*
 * Adapted from the Crossover class in the Jenetics library
 */

import io.jenetics.*;
import io.jenetics.util.MSeq;
import io.jenetics.util.RandomRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Performs a genetic algorithm crossover of two phenotypes,
 * but only using one of the given chromosomes indices to do so
 *
 * @param <G> the gene type.
 * @author <a href="mailto:franz.wilhelmstoetter@gmail.com">Franz Wilhelmstötter</a>
 * @version 4.0
 * @since 1.0
 */
public abstract class CrossoverOnSpecificChromosomes<
		G extends Gene<?, G>,
		C extends Comparable<? super C>
		>
		extends Recombinator<G, C> {

	/**
	 * @param chromosomesToCrossover the list of chromosome indices that may be crossed
	 */
	List<Integer> chromosomesToCrossover;

	/**
	 * Constructs an alterer with a given recombination probability and the given
	 * list of chromosome indices
	 *
	 * @param probability the recombination probability
	 * @throws IllegalArgumentException if the {@code probability} is not in the
	 *                                  valid range of {@code [0, 1]}
	 */
	protected CrossoverOnSpecificChromosomes(final double probability, List<Integer> chromosomesToCrossover) {
		super(probability, 2);
		this.chromosomesToCrossover = chromosomesToCrossover;
	}

	@Override
	protected final int recombine(
			final MSeq<Phenotype<G, C>> population,
			final int[] individuals,
			final long generation
	) {
		assert individuals.length == 2 : "Required order of 2";
		final Random random = RandomRegistry.getRandom();

		final Phenotype<G, C> pt1 = population.get(individuals[0]);
		final Phenotype<G, C> pt2 = population.get(individuals[1]);
		final Genotype<G> gt1 = pt1.getGenotype();
		final Genotype<G> gt2 = pt2.getGenotype();

		List<MSeq<Chromosome<G>>> c = getCrossedChromosomes(gt1, gt2, random);

		if (c.isEmpty()) {
			return 0;
		}

		//Creating two new Phenotypes and exchanging it with the old.
		population.set(
				individuals[0],
				pt1.newInstance(Genotype.of(c.get(0)), generation)
		);
		population.set(
				individuals[1],
				pt2.newInstance(Genotype.of(c.get(1)), generation)
		);

		return getOrder();
	}

	/**
	 * @param gt1 the first genotype to be crossed
	 * @param gt2 the second genotype to be crossed
	 * @return the two crossed chromosomes on success, or an empty list if the selected index is out of range
	 */
	public List<MSeq<Chromosome<G>>> getCrossedChromosomes(Genotype<G> gt1, Genotype<G> gt2, Random random) {
		//Choosing the Chromosome index for crossover.
		final int chIndex = chromosomesToCrossover.get(random.nextInt(chromosomesToCrossover.size()));

		if (gt1.length() < chIndex || gt2.length() < chIndex) {
			return new ArrayList<>();
		}

		final MSeq<Chromosome<G>> c1 = gt1.toSeq().copy();
		final MSeq<Chromosome<G>> c2 = gt2.toSeq().copy();
		final MSeq<G> genes1 = c1.get(chIndex).toSeq().copy();
		final MSeq<G> genes2 = c2.get(chIndex).toSeq().copy();

		crossover(genes1, genes2);

		c1.set(chIndex, c1.get(chIndex).newInstance(genes1.toISeq()));
		c2.set(chIndex, c2.get(chIndex).newInstance(genes2.toISeq()));

		List<MSeq<Chromosome<G>>> toReturn = new ArrayList<>();
		toReturn.add(c1);
		toReturn.add(c2);

		return toReturn;
	}

	/**
	 * Template method which performs the crossover. The arguments given are
	 * mutable non null arrays of the same length.
	 *
	 * @param that  the genes of the first chromosome
	 * @param other the genes of the other chromosome
	 * @return the number of altered genes
	 */
	protected abstract int crossover(final MSeq<G> that, final MSeq<G> other);

}
