package com.hiddenswitch.deckgeneration;

import io.jenetics.BitChromosome;
import io.jenetics.BitGene;
import io.jenetics.Genotype;
import io.jenetics.util.RandomRegistry;

import java.util.*;

public class DeckGeneFactoryForComboTest extends DeckGeneFactory {
	int count = 0;
	List<Integer> requiredCards = new ArrayList<>();

	public DeckGeneFactoryForComboTest(int deckSize, int totalCardsInCollection, List<Integer> requiredCards) {
		super(deckSize, totalCardsInCollection);
		this.requiredCards = requiredCards;
	}

	public DeckGeneFactoryForComboTest(int totalCardsInCollection, List<Integer> requiredCards) {
		super(totalCardsInCollection);
		this.requiredCards = requiredCards;
	}

	public DeckGeneFactoryForComboTest(int totalCardsInCollection, List<Integer> invalidCards, List<Integer> requiredCards) {
		super(totalCardsInCollection, invalidCards);
		this.requiredCards = requiredCards;
	}

	public DeckGeneFactoryForComboTest(int deckSize, int totalCardsInCollection, List<Integer> invalidCards, List<Integer> requiredCards) {
		super(deckSize, totalCardsInCollection, invalidCards);
		this.requiredCards = requiredCards;
	}

	@Override
	public Genotype<BitGene> newInstance() {
		BitSet bits = new BitSet(totalCardsInCollection);
		int i = 0;
		if (count < requiredCards.size()) {
			bits.flip(requiredCards.get(count));
			count++;
			i++;
		}
		Random random = RandomRegistry.getRandom();
		while (i < deckSize) {
			int bitIndex = random.nextInt(totalCardsInCollection);
			while (bits.get(bitIndex) || invalidCards.contains(bitIndex)) {
				bitIndex = random.nextInt(totalCardsInCollection);
			}
			bits.flip(bitIndex);
			i++;
		}
		return Genotype.of(BitChromosome.of(bits, totalCardsInCollection));
	}
}
