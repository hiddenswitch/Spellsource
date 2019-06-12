package com.hiddenswitch.deckgeneration;

import io.jenetics.BitChromosome;
import io.jenetics.BitGene;
import io.jenetics.Chromosome;
import io.jenetics.Genotype;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public class DeckAndDecisionGeneFactory extends DeckGeneFactory {
	List<DecisionType> decisionTypeList = new ArrayList<>();
	List<DecisionType> otherDecisionTypeList = new ArrayList<>();

	public DeckAndDecisionGeneFactory(int deckSize, int totalCardsInCollection) {
		super(deckSize, totalCardsInCollection);
	}

	public DeckAndDecisionGeneFactory(int totalCardsInCollection) {
		super(totalCardsInCollection);
	}

	public DeckAndDecisionGeneFactory(int totalCardsInCollection, List<Integer> invalidCards) {
		super(totalCardsInCollection, invalidCards);
	}

	public DeckAndDecisionGeneFactory(int deckSize, int totalCardsInCollection, List<Integer> invalidCards) {
		super(deckSize, totalCardsInCollection, invalidCards);
	}

	public DeckAndDecisionGeneFactory(int deckSize, int totalCardsInCollection, List<DecisionType> decisionTypeList, List<DecisionType> otherDecisionTypeList) {
		super(deckSize, totalCardsInCollection);
		this.decisionTypeList = decisionTypeList;
		this.otherDecisionTypeList = otherDecisionTypeList;
	}

	public DeckAndDecisionGeneFactory(int totalCardsInCollection, List<DecisionType> decisionTypeList, List<DecisionType> otherDecisionTypeList) {
		super(totalCardsInCollection);
		this.decisionTypeList = decisionTypeList;
		this.otherDecisionTypeList = otherDecisionTypeList;
	}

	public DeckAndDecisionGeneFactory(int totalCardsInCollection, List<Integer> invalidCards, List<DecisionType> decisionTypeList, List<DecisionType> otherDecisionTypeList) {
		super(totalCardsInCollection, invalidCards);
		this.decisionTypeList = decisionTypeList;
		this.otherDecisionTypeList = otherDecisionTypeList;
	}

	public DeckAndDecisionGeneFactory(int deckSize, int totalCardsInCollection, List<Integer> invalidCards, List<DecisionType> decisionTypeList, List<DecisionType> otherDecisionTypeList) {
		super(deckSize, totalCardsInCollection, invalidCards);
		this.decisionTypeList = decisionTypeList;
		this.otherDecisionTypeList = otherDecisionTypeList;
	}

	@Override
	public Genotype<BitGene> newInstance() {
		Chromosome<BitGene> deck = super.newInstance().getChromosome();
		List<Chromosome<BitGene>> chromosomeList = new ArrayList<>();
		chromosomeList.add(deck);
		for (int i = 0; i < decisionTypeList.size(); i++) {
			BitSet bits = new BitSet(totalCardsInCollection);
			chromosomeList.add(BitChromosome.of(bits, totalCardsInCollection));
		}
		for (int i = 0; i < otherDecisionTypeList.size(); i++) {
			BitSet bits = new BitSet(1);
			chromosomeList.add(BitChromosome.of(bits, 1));
		}
		return Genotype.of(chromosomeList);
	}

}
