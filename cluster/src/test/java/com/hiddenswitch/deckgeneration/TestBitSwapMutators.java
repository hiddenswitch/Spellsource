package com.hiddenswitch.deckgeneration;

import io.jenetics.BitChromosome;
import io.jenetics.BitGene;
import io.jenetics.Chromosome;
import io.jenetics.MutatorResult;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.CardSet;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.logic.XORShiftRandom;
import org.testng.annotations.Test;

import java.util.BitSet;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.testng.Assert.assertTrue;

public class TestBitSwapMutators {

	/**
	 * Tests that the smart swap method does indeed
	 * swap the 1/0 bits representing two cards with
	 * the same mana cost
	 */
	@Test
	public void bitSwapByManaCostMutationWithSpecificDecksTest() {

		// Create a card list with only 4 cards:
		// 2 cost 1 mana and the other 2 cost 2 mana
		CardCatalogue.loadCardsFromPackage();
		Stream<Card> ones = CardCatalogue.getAll()
				.stream()
				.filter(card -> card.isCollectible()
						&& (card.getHeroClass() == HeroClass.BLUE || card.getHeroClass() == HeroClass.ANY)
						&& card.getCardSet() == CardSet.BASIC && card.getBaseManaCost() == 1).limit(2);
		Stream<Card> twos = CardCatalogue.getAll()
				.stream()
				.filter(card -> card.isCollectible()
						&& (card.getHeroClass() == HeroClass.BLUE || card.getHeroClass() == HeroClass.ANY)
						&& card.getCardSet() == CardSet.BASIC && card.getBaseManaCost() == 2).limit(2);
		List<Card> indexInBitmap = Stream.concat(ones, twos).collect(toList()); // two cards of cost 1, two cards of cost 2
		int size = indexInBitmap.size();

		BitSet bits = new BitSet(size);
		bits.flip(0);
		bits.flip(2);
		// bits = 0101

		Random random = new XORShiftRandom(101010L);
		Chromosome<BitGene> testChromosome = BitChromosome.of(bits, size);
		BitSwapByManaCostMutator mutator = new BitSwapByManaCostMutator<>(1, indexInBitmap);
		MutatorResult<Chromosome<BitGene>> value = mutator.getBitSwapMutatorResult(testChromosome, 1, random);

		// Tests that a card is swapped in with the same mana cost
		// Does not matter whether the 1 mana card
		// or 2 mana card is swapped
		int count1 = 0;
		int count2 = 0;
		int differences = 0;
		int firstCard = -1;
		for (int i = 0; i < testChromosome.length(); i++) {
			if (testChromosome.getGene(i) != value.getResult().getGene(i)) {
				differences++;
				if (firstCard == -1) {
					firstCard = i;
				} else {
					assertTrue(indexInBitmap.get(firstCard).getBaseManaCost() == indexInBitmap.get(i).getBaseManaCost());
				}
			}
			if (testChromosome.getGene(i).booleanValue()) {
				count1++;
			}
			if (value.getResult().getGene(i).booleanValue()) {
				count2++;
			}
		}
		assertTrue(differences == 2);
		assertTrue(count1 == count2);


		// Check that we find a card such that there exists a
		// card with the same mana cost not in the deck and swap
		bits.flip(1);
		// bits = 0111

		testChromosome = BitChromosome.of(bits, size);
		value = mutator.getBitSwapMutatorResult(testChromosome, 1, random);

		assertTrue(value.getResult().getGene(0) == testChromosome.getGene(0));
		assertTrue(value.getResult().getGene(1) == testChromosome.getGene(1));
		assertTrue(value.getResult().getGene(2) != testChromosome.getGene(2));
		assertTrue(value.getResult().getGene(3) != testChromosome.getGene(3));

		// If no swappable cards exist, do not mutate

		bits.flip(3);
		// bits = 1111

		testChromosome = BitChromosome.of(bits, size);
		value = mutator.getBitSwapMutatorResult(testChromosome, 1, random);
		assertTrue(value.getResult().getGene(0) == testChromosome.getGene(0));
		assertTrue(value.getResult().getGene(1) == testChromosome.getGene(1));
		assertTrue(value.getResult().getGene(2) == testChromosome.getGene(2));
		assertTrue(value.getResult().getGene(3) == testChromosome.getGene(3));

		bits.flip(0);
		bits.flip(1);
		bits.flip(2);
		bits.flip(3);
		// bits = 0000

		testChromosome = BitChromosome.of(bits, size);
		value = mutator.getBitSwapMutatorResult(testChromosome, 1, random);
		assertTrue(value.getResult().getGene(0) == testChromosome.getGene(0));
		assertTrue(value.getResult().getGene(1) == testChromosome.getGene(1));
		assertTrue(value.getResult().getGene(2) == testChromosome.getGene(2));
		assertTrue(value.getResult().getGene(3) == testChromosome.getGene(3));
	}

	@Test
	public void bitSwapByManaCostMutationWithGeneralRandomDecksTest() {
		Random random = new XORShiftRandom(101010L);
		CardCatalogue.loadCardsFromPackage();
		List<Card> indexInBitmap = CardCatalogue.getAll()
				.stream()
				.filter(card -> card.isCollectible()
						&& (card.getHeroClass() == HeroClass.BLUE || card.getHeroClass() == HeroClass.ANY)
						&& card.getCardSet() == CardSet.BASIC && card.getBaseManaCost() == 1).collect(Collectors.toList());
		int size = indexInBitmap.size();

		BitSet bits = new BitSet(size);
		for (int i = 0; i < size / 2; i++) {
			int toFlip = random.nextInt(size);
			while (bits.get(toFlip)) {
				toFlip = random.nextInt(size);
			}
			bits.flip(toFlip);
		}

		Chromosome<BitGene> testChromosome = BitChromosome.of(bits, size);
		BitSwapByManaCostMutator mutator = new BitSwapByManaCostMutator<>(1, indexInBitmap);
		MutatorResult<Chromosome<BitGene>> value = mutator.getBitSwapMutatorResult(testChromosome, 1, random);

		// Tests that a card is swapped in with the same mana cost
		int count1 = 0;
		int count2 = 0;
		int differences = 0;
		int firstCard = -1;
		for (int i = 0; i < testChromosome.length(); i++) {
			if (testChromosome.getGene(i) != value.getResult().getGene(i)) {
				differences++;
				if (firstCard == -1) {
					firstCard = i;
				} else {
					assertTrue(indexInBitmap.get(firstCard).getBaseManaCost() == indexInBitmap.get(i).getBaseManaCost());
				}
			}
			if (testChromosome.getGene(i).booleanValue()) {
				count1++;
			}
			if (value.getResult().getGene(i).booleanValue()) {
				count2++;
			}
		}
		assertTrue(differences == 2);
		assertTrue(count1 == count2);
	}


	/**
	 * Tests that the basic card swapper returns a chromosome that
	 * contains the same number of cards and has swapped a 1 and 0
	 */
	@Test
	public void bitSwapMutationTest() {
		int size = 10;
		Random random = new XORShiftRandom(101010L);
		BitSet bits = new BitSet(16);
		for (int i = 0; i < 4; i++) {
			int toFlip = random.nextInt(size - 1);
			while (bits.get(toFlip)) {
				toFlip = random.nextInt(size - 1);
			}
			bits.flip(toFlip);
		}
		bits.flip(size - 1);
		Chromosome<BitGene> testChromosome = BitChromosome.of(bits);
		BitSwapMutator mutator = new BitSwapMutator(1);
		MutatorResult<Chromosome<BitGene>> value = mutator.getBitSwapMutatorResult(testChromosome, 1, random);

		int count1 = 0;
		int count2 = 0;
		int differences = 0;
		for (int i = 0; i < testChromosome.length(); i++) {
			if (testChromosome.getGene(i) != value.getResult().getGene(i)) {
				differences++;
			}
			if (testChromosome.getGene(i).booleanValue()) {
				count1++;
			}
			if (value.getResult().getGene(i).booleanValue()) {
				count2++;
			}
		}
		// A swapped chromosome differs from the original in only 2 places:
		// The swapped 1 and 0 locations
		assertTrue(differences == 2);
		assertTrue(count1 == count2);
	}

	/**
	 * Tests that the basic card swapper handles the edge cases where either every
	 * card is in the deck or no cards are in the deck
	 */
	@Test
	public void bitSwapReturnsTest() {
		Random random = new XORShiftRandom(101010L);
		BitSet bits = new BitSet(2);
		Chromosome<BitGene> testChromosome = BitChromosome.of(bits, 2);
		BitSwapMutator mutator = new BitSwapMutator(1);
		MutatorResult<Chromosome<BitGene>> value = mutator.getBitSwapMutatorResult(testChromosome, 1, random);
		assertTrue(testChromosome.equals(value.getResult()));

		bits.flip(0);
		bits.flip(1);
		MutatorResult<Chromosome<BitGene>> value2 = mutator.getBitSwapMutatorResult(testChromosome, 1, random);
		assertTrue(testChromosome.equals(value2.getResult()));
	}
}
