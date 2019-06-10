package com.hiddenswitch.deckgeneration;

/*
 * Adapted from the MultiPointCrossover class in the
 * Jenetics library
 */

import io.jenetics.Gene;
import io.jenetics.internal.math.comb;
import io.jenetics.internal.util.Equality;
import io.jenetics.internal.util.Hash;
import io.jenetics.util.MSeq;
import io.jenetics.util.RandomRegistry;

import java.util.List;
import java.util.Random;

import static java.lang.Math.min;
import static java.lang.String.format;

public class MultiPointCrossoverOnSpecificChromosomes<
		G extends Gene<?, G>,
		C extends Comparable<? super C>
		>
		extends CrossoverOnSpecificChromosomes<G, C> {

	private final int _n;

	/**
	 * Create a new crossover instance.
	 *
	 * @param probability            the recombination probability.
	 * @param n                      the number of crossover points.
	 * @param chromosomesToCrossover the list of valid chromosomes indices to crossover
	 * @throws IllegalArgumentException if the {@code probability} is not in the
	 *                                  valid range of {@code [0, 1]} or {@code n &lt; 1}.
	 */
	public MultiPointCrossoverOnSpecificChromosomes(final double probability, final int n, List<Integer> chromosomesToCrossover) {
		super(probability, chromosomesToCrossover);
		if (n < 1) {
			throw new IllegalArgumentException(format(
					"n must be at least 1 but was %d.", n
			));
		}
		_n = n;
	}

	/**
	 * Create a new crossover instance with two crossover points.
	 *
	 * @param probability            the recombination probability.
	 * @param chromosomesToCrossover the list of valid chromosomes indices to crossover
	 * @throws IllegalArgumentException if the {@code probability} is not in the
	 *                                  valid range of {@code [0, 1]}.
	 */
	public MultiPointCrossoverOnSpecificChromosomes(final double probability, List<Integer> chromosomesToCrossover) {
		this(probability, 2, chromosomesToCrossover);
	}

	/**
	 * Create a new crossover instance with default crossover probability of
	 * 0.05.
	 *
	 * @param n                      the number of crossover points.
	 * @param chromosomesToCrossover the list of valid chromosomes indices to crossover
	 * @throws IllegalArgumentException if {@code n &lt; 1}.
	 */
	public MultiPointCrossoverOnSpecificChromosomes(final int n, List<Integer> chromosomesToCrossover) {
		this(0.05, n, chromosomesToCrossover);
	}

	/**
	 * Create a new crossover instance with two crossover points and crossover
	 * probability 0.05.
	 *
	 * @param chromosomesToCrossover the list of valid chromosomes indices to crossover
	 */
	public MultiPointCrossoverOnSpecificChromosomes(List<Integer> chromosomesToCrossover) {
		this(0.05, 2, chromosomesToCrossover);
	}

	/**
	 * Return the number of crossover points.
	 *
	 * @return the number of crossover points.
	 */
	public int getN() {
		return _n;
	}

	@Override
	protected int crossover(final MSeq<G> that, final MSeq<G> other) {
		assert that.length() == other.length();

		final int n = min(that.length(), other.length());
		final int k = min(n, _n);

		final Random random = RandomRegistry.getRandom();
		final int[] points = k > 0 ? comb.subset(n, k, random) : new int[0];

		crossover(that, other, points);
		return 2;
	}

	// Package private for testing purpose.
	static <T> void crossover(
			final MSeq<T> that,
			final MSeq<T> other,
			final int[] indexes
	) {

		for (int i = 0; i < indexes.length - 1; i += 2) {
			final int start = indexes[i];
			final int end = indexes[i + 1];
			that.swap(start, end, other, start);
		}
		if (indexes.length % 2 == 1) {
			final int index = indexes[indexes.length - 1];
			that.swap(index, min(that.length(), other.length()), other, index);
		}
	}

	@Override
	public int hashCode() {
		return Hash.of(getClass())
				.and(super.hashCode())
				.and(_n).value();
	}

	@Override
	public boolean equals(final Object obj) {
		return Equality.of(this, obj).test(mpc ->
				_n == mpc._n &&
						super.equals(obj)
		);
	}

	@Override
	public String toString() {
		return format(
				"%s[p=%f, n=%d]",
				getClass().getSimpleName(), _probability, _n
		);
	}
}
