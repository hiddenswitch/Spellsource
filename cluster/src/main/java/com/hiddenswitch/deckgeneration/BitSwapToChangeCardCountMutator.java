package com.hiddenswitch.deckgeneration;

import io.jenetics.Chromosome;
import io.jenetics.Gene;
import io.jenetics.MutatorResult;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.logic.XORShiftRandom;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

public class BitSwapToChangeCardCountMutator<
		G extends Gene<Boolean, G>,
		C extends Comparable<? super C>
		> extends BitSwapMutator<G, C> {
	private List<Integer> duplicateCardList;
	private ThreadLocal<Integer> selectedOne = ThreadLocal.withInitial(() -> -1);
	private ThreadLocal<Integer> selectedZero = ThreadLocal.withInitial(() -> -1);
	private ThreadLocal<Integer> index = ThreadLocal.withInitial(() -> -1);

	public BitSwapToChangeCardCountMutator(double probability, List<Card> indexInBitmap) {
		super(probability);
		duplicateCardList = createDuplicateCardList(indexInBitmap);
	}

	public BitSwapToChangeCardCountMutator(List<Card> indexInBitmap) {
		super(0.1);
		duplicateCardList = createDuplicateCardList(indexInBitmap);
	}

	private List<Integer> createDuplicateCardList(List<Card> indexInBitmap) {
		List<Integer> duplicateCardList = new ArrayList<>(Collections.nCopies(indexInBitmap.size(), -1));
		List<String> cardIdList = indexInBitmap.stream().map(card -> card.getCardId()).collect(toList());
		for (int i = 0; i < indexInBitmap.size(); i++) {
			if (duplicateCardList.get(i) == -1) {
				String target = cardIdList.get(i);
				for (int j = i + 1; j < indexInBitmap.size(); j++) {
					if (cardIdList.get(j).equals(target)) {
						duplicateCardList.set(i, j);
						duplicateCardList.set(j, i);
						break;
					}
				}
			}
		}
		return duplicateCardList;
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
		return getChromosomeMutatorResult(chromosome, p, random);
	}

	@NotNull
	public MutatorResult<Chromosome<G>> getChromosomeMutatorResult(Chromosome<G> chromosome, double p, Random random) {
		if (p < random.nextDouble()) {
			return MutatorResult.of(chromosome);
		}

		if (chromosome.length() != duplicateCardList.size()) {
			throw new IllegalArgumentException("chromosome.length() != indexInBitmap.size()");
		}

		// Find all of the indices of bits that are true and that
		// represent a card that such that a deck can have multiple copies of that card
		List<Integer> indicesOfPotentialBits = IntStream.range(0, chromosome.length())
				.filter(i -> chromosome.getGene(i).getAllele() && duplicateCardList.get(i) != -1)
				.boxed()
				.collect(toList());

		// Ensure that the list contains each unique card once
		indicesOfPotentialBits = removeDuplicateCardsFromList(indicesOfPotentialBits);

		if (indicesOfPotentialBits.isEmpty()) {
			return MutatorResult.of(chromosome);
		}

		// Randomly pick a card, then see if both copies are in the deck, or just that one
		int index = indicesOfPotentialBits.get(random.nextInt(indicesOfPotentialBits.size()));
		int otherIndex = duplicateCardList.get(index);
		boolean otherIsInDeck = chromosome.getGene(otherIndex).getAllele();
		// If both copies are in the deck, remove this card
		if (otherIsInDeck) {
			selectedOne.set(index);
			// Otherwise, add the copy not in the deck to the deck
		} else {
			selectedZero.set(otherIndex);
		}
		MutatorResult<Chromosome<G>> result = super.getBitSwapMutatorResult(chromosome, 1, random);
		selectedOne.set(-1);
		selectedZero.set(-1);
		return result;
	}

	// If the selected one has been chosen, use only that.
	// Otherwise, make sure we don't turn a zero into a one that doesn't change the deck
	@Override
	protected boolean isOne(Chromosome<G> chromosome, int i) {
		if (selectedOne.get() != -1) {
			return i == selectedOne.get();
		}
		return super.isOne(chromosome, i) && (duplicateCardList.get(i) != selectedZero.get());
	}

	// If the selected zero has already been chosen, use that.
	// Otherwise, pick any other random zero to add in
	@Override
	protected boolean isZero(Chromosome<G> chromosome, int i) {
		if (selectedZero.get() != -1) {
			return i == selectedZero.get();
		}
		return super.isZero(chromosome, i);
	}

	// If the one has already been chosen, there is only one possible value
	// Otherwise, we can pick any one card that isn't the same as what we're putting in
	@Override
	protected int getNumberOfOnes(Chromosome<G> chromosome) {
		if (selectedOne.get() == -1) {
			return super.getNumberOfOnes(chromosome) - 1;
		}
		return 1;
	}

	// If the zero has already been chosen, we can only choose that one.
	// Otherwise, we calculate as normal
	@Override
	protected int getNumberOfZeroes(Chromosome<G> chromosome, int numberOfOnes) {
		if (selectedZero.get() == -1) {
			return super.getNumberOfZeroes(chromosome, super.getNumberOfOnes(chromosome));
		}
		return 1;
	}

	// Remove duplicate cards from the list in order to let any unique card
	// be chosen with uniform probability
	protected List<Integer> removeDuplicateCardsFromList(List<Integer> indicesOfPotentialBits) {
		Random random = new XORShiftRandom(101010L);
		List<Integer> result = new ArrayList<>(indicesOfPotentialBits);
		int i = 0;
		while (i < result.size()) {
			int duplicateIndex = result.indexOf(duplicateCardList.get(result.get(i)));
			if (duplicateIndex != -1) {
				int x = random.nextInt(2);
				if (x == 0) {
					result.remove(i);
					i--;
				} else {
					result.remove(duplicateIndex);
				}
			}
			i++;
		}
		return result;
	}
}
