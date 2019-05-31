package com.hiddenswitch.deckgeneration;

import io.jenetics.Crossover;
import io.jenetics.Gene;
import io.jenetics.util.MSeq;
import net.demilich.metastone.game.logic.XORShiftRandom;

import java.util.*;

public class BitSwapBetweenTwoSequencesMutator<
		G extends Gene<Boolean, G>,
		C extends Comparable<? super C>
		>
		extends Crossover<G, C> {

	/**
	 * Constructs an alterer that takes two decks and swaps one card with each other
	 *
	 * @param probability the crossover probability.
	 * @throws IllegalArgumentException if the {@code probability} is not in the
	 *                                  valid range of {@code [0, 1]}.
	 */
	public BitSwapBetweenTwoSequencesMutator(final double probability) {
		super(probability);
	}

	@Override
	protected int crossover(final MSeq<G> firstSequence, final MSeq<G> secondSequence) {
		Random random = new XORShiftRandom(101010L);

		// Find the locations of the ones in each chromosome
		List<Integer> firstSequenceOnes = new ArrayList<>();
		List<Integer> secondSequenceOnes = new ArrayList<>();
		for (int i = 0; i < firstSequence.size(); i++) {
			if (firstSequence.get(i).getAllele()) {
				firstSequenceOnes.add(i);
			}
		}
		for (int i = 0; i < secondSequence.size(); i++) {
			if (secondSequence.get(i).getAllele()) {
				secondSequenceOnes.add(i);
			}
		}

		// Find the "unique" ones of each chromosome (where the 1
		Set<Integer> intersection = new HashSet<>(firstSequenceOnes);
		intersection.retainAll(secondSequenceOnes);

		firstSequenceOnes.removeAll(intersection);
		secondSequenceOnes.removeAll(intersection);

		// If at least one of the chromosomes has no ones, we cannot meaningfully swap any bits
		// between the two
		if (firstSequenceOnes.isEmpty() || secondSequenceOnes.isEmpty()) {
			return 0;
		}

		int firstGeneToSwap = firstSequenceOnes.get(random.nextInt(firstSequenceOnes.size()));
		int secondGeneToSwap = secondSequenceOnes.get(random.nextInt(secondSequenceOnes.size()));

		firstSequence.swap(firstGeneToSwap, firstGeneToSwap + 1, secondSequence, firstGeneToSwap);
		firstSequence.swap(secondGeneToSwap, secondGeneToSwap + 1, secondSequence, secondGeneToSwap);
		return 2;
	}

}
