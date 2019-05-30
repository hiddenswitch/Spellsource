package com.hiddenswitch.deckgeneration;

import io.jenetics.*;
import io.jenetics.util.MSeq;
import net.demilich.metastone.game.cards.Card;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Random;

/**
 * A mutator that swaps a card in a deck for one
 * not in the deck with the same mana cost
 *
 * @param <G>
 * @param <C>
 */
public class SwapByManaCostMutator<
		G extends Gene<?, G>,
		C extends Comparable<? super C>
		>
		extends SwapMutator<G, C> {

	List<Card> indexInBitmap;

	/**
	 * Constructs an alterer that swaps a card in a deck
	 * for one not in the deck, but of the same mana cost
	 *
	 * @param probability the crossover probability.
	 * @throws IllegalArgumentException if the {@code probability} is not in the
	 *                                  valid range of {@code [0, 1]}.
	 */
	public SwapByManaCostMutator(final double probability, List<Card> indexInBitmap) {
		super(probability);
		this.indexInBitmap = indexInBitmap;
	}

	/**
	 * Calls the swapping method
	 */
	@Override
	protected MutatorResult<Chromosome<G>> mutate(
			final Chromosome<G> chromosome,
			final double p,
			final Random random
	) {
		return getSimpleSmartSwapMutatorResult(chromosome, p, random, indexInBitmap);
	}

	/**
	 * Method that swaps the position of a 1 and 0 in a chromosome
	 * as long as those two have the same mana cost
	 *
	 * @param chromosome    The Chromosome that represents our deck
	 * @param p             The probability of making a swap
	 * @param random        The Random object used for selecting our indices
	 * @param indexInBitmap The list of cards as they correspond to the bit encoding
	 * @param <G>           The type of our gene (BitGene is recommended)
	 * @return The mutated Chromosome
	 */
	@NotNull
	public static <G extends Gene<?, G>> MutatorResult<Chromosome<G>> getSimpleSmartSwapMutatorResult(Chromosome<G> chromosome, double p, Random random, List<Card> indexInBitmap) {
		final MutatorResult<Chromosome<G>> result;
		int numberOfOnes = ((BitChromosome) chromosome).bitCount();
		if (chromosome.length() > 1
				&& numberOfOnes != chromosome.length()
				&& numberOfOnes != 0
				&& random.nextDouble() <= p) {
			final MSeq<G> genes = chromosome.toSeq().copy();
			int firstCard = random.nextInt(chromosome.length());
			while (true) {
				// If i is in the deck then randomly select cards until we find one
				// not in the deck, or vice versa
				int secondCard = random.nextInt(genes.length());
				if (!genes.get(secondCard).equals(genes.get(firstCard)) &&
						(indexInBitmap.get(firstCard).getBaseManaCost() == indexInBitmap.get(secondCard).getBaseManaCost())) {
					genes.swap(firstCard, secondCard);
					break;
				}
			}
			result = MutatorResult.of(
					chromosome.newInstance(genes.toISeq())
			);
		} else {
			result = MutatorResult.of(chromosome);
		}
		return result;
	}
}
