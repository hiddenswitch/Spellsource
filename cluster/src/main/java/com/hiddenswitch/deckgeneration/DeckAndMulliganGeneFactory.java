package com.hiddenswitch.deckgeneration;

import io.jenetics.BitChromosome;
import io.jenetics.BitGene;
import io.jenetics.Chromosome;
import io.jenetics.Genotype;

import java.util.BitSet;
import java.util.List;

public class DeckAndMulliganGeneFactory extends DeckGeneFactory {
	public DeckAndMulliganGeneFactory(int deckSize, int totalCardsInCollection) {
		super(deckSize, totalCardsInCollection);
	}

	public DeckAndMulliganGeneFactory(int totalCardsInCollection) {
		super(totalCardsInCollection);
	}

	public DeckAndMulliganGeneFactory(int totalCardsInCollection, List<Integer> invalidCards) {
		super(totalCardsInCollection, invalidCards);
	}

	public DeckAndMulliganGeneFactory(int deckSize, int totalCardsInCollection, List<Integer> invalidCards) {
		super(deckSize, totalCardsInCollection, invalidCards);
	}

	@Override
	public Genotype<BitGene> newInstance() {
		Chromosome<BitGene> deck = super.newInstance().getChromosome();
		BitSet bits = new BitSet(totalCardsInCollection);
		Chromosome<BitGene> keepCards = BitChromosome.of(bits, totalCardsInCollection);
		return Genotype.of(deck, keepCards);
	}
}
