package com.hiddenswitch.deckgeneration;

import io.jenetics.*;
import io.jenetics.util.MSeq;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

/**
 * Swaps the order of only true bits.
 *
 * @param <C>
 */
public class BitSwapMutator<
		C extends Comparable<? super C>
		>
		extends SwapMutator<BitGene, C> {
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
	protected MutatorResult<Chromosome<BitGene>> mutate(
			final Chromosome<BitGene> chromosome,
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
	public static MutatorResult<Chromosome<BitGene>> getBitSwapMutatorResult(Chromosome<BitGene> chromosome, double p, Random random) {
		final MutatorResult<Chromosome<BitGene>> result;
		// Calculates the number of cards in the deck
		int numberOfOnes;
		if (chromosome instanceof BitChromosome) {
			numberOfOnes = ((BitChromosome) chromosome).bitCount();
		} else {
			numberOfOnes = (int) chromosome.stream().filter(BitGene::getBit).count();
		}
		int length = chromosome.length();
		if (length > 1
				&& numberOfOnes != length
				&& numberOfOnes != 0
				&& random.nextDouble() <= p) {
			MSeq<BitGene> genes = chromosome.toSeq().copy();

			int trueN = random.nextInt(numberOfOnes);
			int falseN = random.nextInt(length - numberOfOnes);

			int trueIndex = -1;
			int falseIndex = -1;
			int j = 0;
			int k = 0;
			for (int i = 0; i < length; i++) {
				boolean bit = chromosome.getGene(i).booleanValue();
				if (bit) {
					if (j == trueN) {
						trueIndex = i;
					}
					j++;
				} else {
					if (k == falseN) {
						falseIndex = i;
					}
					k++;
				}
				if (trueIndex != -1 && falseIndex != -1) {
					break;
				}
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
}
