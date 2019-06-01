package com.hiddenswitch.deckgeneration;

import io.jenetics.BitChromosome;
import io.jenetics.BitGene;
import io.jenetics.Genotype;
import io.jenetics.util.Factory;
import io.jenetics.util.RandomRegistry;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Random;

/**
 * Custom Factory class that creates a valid deck as a Genotype
 */
public class DeckGeneFactory implements Factory<Genotype<BitGene>> {
	protected int deckSize = 30;
	protected int totalCardsInCollection;
	protected List<Integer> invalidCards = new ArrayList<>();

	public DeckGeneFactory(int deckSize, int totalCardsInCollection) {
		this.deckSize = deckSize;
		this.totalCardsInCollection = totalCardsInCollection;
	}

	public DeckGeneFactory(int totalCardsInCollection) {
		this.totalCardsInCollection = totalCardsInCollection;
	}

	public DeckGeneFactory(int totalCardsInCollection, List<Integer> invalidCards) {
		this.invalidCards = invalidCards;
		this.totalCardsInCollection = totalCardsInCollection;
	}

	public DeckGeneFactory(int deckSize, int totalCardsInCollection, List<Integer> invalidCards) {
		this.deckSize = deckSize;
		this.totalCardsInCollection = totalCardsInCollection;
		this.invalidCards = invalidCards;
	}

	@Override
	public Genotype<BitGene> newInstance() {
		BitSet bits = new BitSet(totalCardsInCollection);
		Random random = RandomRegistry.getRandom();
		for (int i = 0; i < deckSize; i++) {
			int bitIndex = random.nextInt(totalCardsInCollection);
			while (bits.get(bitIndex) || invalidCards.contains(bitIndex)) {
				bitIndex = random.nextInt(totalCardsInCollection);
			}
			bits.flip(bitIndex);
		}
		return Genotype.of(BitChromosome.of(bits, totalCardsInCollection));
	}
}
