package com.hiddenswitch.deckgeneration;

import io.jenetics.*;
import io.jenetics.util.MSeq;
import org.jetbrains.annotations.NotNull;

import java.util.Random;
import java.util.stream.IntStream;

/**
 * Swaps the order of only true bits.
 *
 * @param <C>
 */
public class BitSwapMutator<
		G extends Gene<Boolean, G>,
		C extends Comparable<? super C>
		>
		extends SwapMutator<G, C> {
	/**
	 * Constructs an alterer that swaps a 1 and 0 in the BitChromosome
	 * (in our case, swapping out a card in the deck for one
	 * not in the deck)
	 *
	 * @param probability the crossover probability.
	 * @throws IllegalArgumentException if the {@code probability} is not in the
	 *                                  valid range of {@code [0, 1]}.
	 */
	public BitSwapMutator(final double probability) {
		super(probability);
	}

	/**
	 * Default constructor, with default mutation probability
	 * ({@link AbstractAlterer#DEFAULT_ALTER_PROBABILITY}).
	 */
	public BitSwapMutator() {
		this(DEFAULT_ALTER_PROBABILITY);
	}

	/**
	 * Calls the basic swapping function
	 */
	@Override
	protected MutatorResult<Chromosome<G>> mutate(
			final Chromosome<G> chromosome,
			final double p,
			final Random random
	) {
		return getBitSwapMutatorResult(chromosome, p, random);
	}

	/**
	 * Swaps a 1 and 0 in the given array, with the mutation probability of this
	 * mutation.
	 *
	 * @param chromosome The Chromosome that represents our deck
	 * @param p          The probability of making a swap
	 * @param random     The Random object used for selecting our indices
	 * @return The mutated Chromosome
	 */
	@NotNull
	public MutatorResult<Chromosome<G>> getBitSwapMutatorResult(Chromosome<G> chromosome, double p, Random random) {
		if (p < random.nextDouble()) {
			return MutatorResult.of(chromosome);
		}

		final MutatorResult<Chromosome<G>> result;
		// Calculates the number of cards in the deck
		int numberOfOnes;
		numberOfOnes = getNumberOfOnes(chromosome);
		int numberOfZeroes = getNumberOfZeroes(chromosome, numberOfOnes);
		int length = chromosome.length();

		if (numberOfOnes == 0) {
			return MutatorResult.of(chromosome);
		}

		if (numberOfZeroes == 0) {
			return MutatorResult.of(chromosome);
		}

		if (length > 1 && numberOfOnes != length && random.nextDouble() <= p) {
			MSeq<G> genes = chromosome.toSeq().copy();

			int trueN = random.nextInt(numberOfOnes);
			int falseN = random.nextInt(numberOfZeroes);

			int trueIndex = -1;
			int falseIndex = -1;
			int j = 0;
			int k = 0;
			for (int i = 0; i < length; i++) {
				if (isOne(chromosome, i)) {
					if (j == trueN) {
						trueIndex = i;
					}
					j++;
				} else if (isZero(chromosome, i)) {
					if (k == falseN) {
						falseIndex = i;
					}
					k++;
				}
				if (trueIndex != -1 && falseIndex != -1) {
					break;
				}
			}

			if (trueIndex == -1 || falseIndex == -1) {
				throw new IllegalStateException();
			}

			genes.swap(trueIndex, falseIndex);

			result = MutatorResult.of(
					chromosome.newInstance(genes.toISeq())
			);
		} else {
			result = MutatorResult.of(chromosome);
		}
		return result;
	}


	protected boolean isOne(Chromosome<G> chromosome, int i) {
		return chromosome.getGene(i).getAllele();
	}


	protected boolean isZero(Chromosome<G> chromosome, int i) {
		boolean bit = chromosome.getGene(i).getAllele();
		return !bit;
	}

	protected int getNumberOfZeroes(Chromosome<G> chromosome, int numberOfOnes) {
		return chromosome.length() - numberOfOnes;
	}

	protected int getNumberOfOnes(Chromosome<G> chromosome) {
		int numberOfOnes;
		if (chromosome instanceof BitChromosome) {
			numberOfOnes = ((BitChromosome) chromosome).bitCount();
		} else {
			numberOfOnes = (int) IntStream.range(0, chromosome.length()).filter(i -> isOne(chromosome, i)).count();
		}
		return numberOfOnes;
	}
}
