package com.hiddenswitch.deckgeneration;

import io.jenetics.BitChromosome;
import io.jenetics.BitGene;
import io.jenetics.Chromosome;
import io.jenetics.Genotype;
import net.demilich.metastone.game.logic.XORShiftRandom;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Random;

public class DeckAndDecisionGeneFactory extends DeckGeneFactory {
	int decisionTypeListSize = 0;
	int otherDecisionTypeListSize = 0;
	double p = 0;

	/**
	 * @param deckSize               The number of cards in each deck
	 * @param totalCardsInCollection The number of cards to choose from
	 *                               (also the length of the deck chromosome)
	 */
	public DeckAndDecisionGeneFactory(int deckSize, int totalCardsInCollection) {
		super(deckSize, totalCardsInCollection);
	}

	/**
	 * @param totalCardsInCollection The number of cards to choose from
	 *                               (also the length of the deck chromosome)
	 */
	public DeckAndDecisionGeneFactory(int totalCardsInCollection) {
		super(totalCardsInCollection);
	}

	/**
	 * @param totalCardsInCollection The number of cards to choose from
	 *                               (also the length of the deck chromosome)
	 * @param invalidCards           The list of cards that should not appear in the initially generated decks
	 */
	public DeckAndDecisionGeneFactory(int totalCardsInCollection, List<Integer> invalidCards) {
		super(totalCardsInCollection, invalidCards);
	}

	/**
	 * @param deckSize               The number of cards in each deck
	 * @param totalCardsInCollection The number of cards to choose from
	 *                               (also the length of the deck chromosome)
	 * @param invalidCards           The list of cards that should not appear in the initially generated decks
	 */
	public DeckAndDecisionGeneFactory(int deckSize, int totalCardsInCollection, List<Integer> invalidCards) {
		super(deckSize, totalCardsInCollection, invalidCards);
	}

	/**
	 * @param deckSize                  The number of cards in each deck
	 * @param totalCardsInCollection    The number of cards to choose from
	 *                                  (also the length of the deck chromosome)
	 * @param decisionTypeListSize      The number of decisionTypes that
	 *                                  the genetic algorithm will be acting on
	 * @param otherDecisionTypeListSize The number of boolean decisionTypes that
	 *                                  the genetic algorithm will be acting on
	 */
	public DeckAndDecisionGeneFactory(int deckSize, int totalCardsInCollection, int decisionTypeListSize, int otherDecisionTypeListSize) {
		super(deckSize, totalCardsInCollection);
		this.decisionTypeListSize = decisionTypeListSize;
		this.otherDecisionTypeListSize = otherDecisionTypeListSize;
	}

	/**
	 * @param totalCardsInCollection    The number of cards to choose from
	 *                                  (also the length of the deck chromosome)
	 * @param decisionTypeListSize      The number of decisionTypes that
	 *                                  the genetic algorithm will be acting on
	 * @param otherDecisionTypeListSize The number of boolean decisionTypes that
	 *                                  the genetic algorithm will be acting on
	 */
	public DeckAndDecisionGeneFactory(int totalCardsInCollection, int decisionTypeListSize, int otherDecisionTypeListSize) {
		super(totalCardsInCollection);
		this.decisionTypeListSize = decisionTypeListSize;
		this.otherDecisionTypeListSize = otherDecisionTypeListSize;
	}

	/**
	 * @param totalCardsInCollection    The number of cards to choose from
	 *                                  (also the length of the deck chromosome)
	 * @param invalidCards              The list of cards that should not appear in the initially generated decks
	 * @param decisionTypeListSize      The number of decisionTypes that
	 *                                  the genetic algorithm will be acting on
	 * @param otherDecisionTypeListSize The number of boolean decisionTypes that
	 *                                  the genetic algorithm will be acting on
	 */
	public DeckAndDecisionGeneFactory(int totalCardsInCollection, List<Integer> invalidCards, int decisionTypeListSize, int otherDecisionTypeListSize) {
		super(totalCardsInCollection, invalidCards);
		this.decisionTypeListSize = decisionTypeListSize;
		this.otherDecisionTypeListSize = otherDecisionTypeListSize;
	}

	/**
	 * @param deckSize                  The number of cards in each deck
	 * @param totalCardsInCollection    The number of cards to choose from
	 *                                  (also the length of the deck chromosome)
	 * @param invalidCards              The list of cards that should not appear in the initially generated decks
	 * @param decisionTypeListSize      The number of decisionTypes that
	 *                                  the genetic algorithm will be acting on
	 * @param otherDecisionTypeListSize The number of boolean decisionTypes that
	 *                                  the genetic algorithm will be acting on
	 */
	public DeckAndDecisionGeneFactory(int deckSize, int totalCardsInCollection, List<Integer> invalidCards, int decisionTypeListSize, int otherDecisionTypeListSize) {
		super(deckSize, totalCardsInCollection, invalidCards);
		this.decisionTypeListSize = decisionTypeListSize;
		this.otherDecisionTypeListSize = otherDecisionTypeListSize;
	}

	/**
	 * @param deckSize                  The number of cards in each deck
	 * @param totalCardsInCollection    The number of cards to choose from
	 *                                  (also the length of the deck chromosome)
	 * @param invalidCards              The list of cards that should not appear in the initially generated decks
	 * @param decisionTypeListSize      The number of decisionTypes that
	 *                                  the genetic algorithm will be acting on
	 * @param otherDecisionTypeListSize The number of boolean decisionTypes that
	 *                                  the genetic algorithm will be acting on
	 * @param p                         the probability that a particular gene in the
	 *                                  decisionType chromosomes will be active
	 *                                  if the corresponding card is in the deck
	 */
	public DeckAndDecisionGeneFactory(int deckSize, int totalCardsInCollection, List<Integer> invalidCards, int decisionTypeListSize, int otherDecisionTypeListSize, double p) {
		super(deckSize, totalCardsInCollection, invalidCards);
		this.decisionTypeListSize = decisionTypeListSize;
		this.otherDecisionTypeListSize = otherDecisionTypeListSize;
		this.p = p;
	}

	@Override
	public Genotype<BitGene> newInstance() {
		Random random = new XORShiftRandom(101010L);
		Chromosome<BitGene> deck = super.newInstance().getChromosome();
		List<Chromosome<BitGene>> chromosomeList = new ArrayList<>();
		chromosomeList.add(deck);
		for (int i = 0; i < decisionTypeListSize; i++) {
			BitSet bits = new BitSet(totalCardsInCollection);
			if (p != 0) {
				for (int j = 0; j < totalCardsInCollection; j++) {
					if (random.nextDouble() < p) {
						bits.flip(j);
					}
				}
			}
			chromosomeList.add(BitChromosome.of(bits, totalCardsInCollection));
		}
		for (int i = 0; i < otherDecisionTypeListSize; i++) {
			BitSet bits = new BitSet(1);
			if (random.nextDouble() < p) {
				bits.flip(0);
			}
			chromosomeList.add(BitChromosome.of(bits, 1));
		}
		return Genotype.of(chromosomeList);
	}

}
