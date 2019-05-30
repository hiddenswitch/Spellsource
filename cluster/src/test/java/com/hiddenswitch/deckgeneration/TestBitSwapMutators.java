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

import static java.util.stream.Collectors.toList;
import static org.testng.Assert.assertTrue;

public class TestBitSwapMutators {

	/**
	 * Tests that the smart swap method does indeed
	 * swap the 1/0 bits representing two cards with
	 * the same mana cost
	 */
	@Test
	public void smartSwapMutationTest() {
		CardCatalogue.loadCardsFromPackage();
		List<Card> indexInBitmap = CardCatalogue.getAll()
				.stream()
				.filter(card -> card.isCollectible()
						&& (card.getHeroClass() == HeroClass.BLUE || card.getHeroClass() == HeroClass.ANY)
						&& card.getCardSet() == CardSet.BASIC)
				.collect(toList());
		int size = indexInBitmap.size();
		Random random = new XORShiftRandom(101010L);
		BitSet bits = new BitSet(16);
		for (int i = 0; i < 14; i++) {
			int toFlip = random.nextInt(size - 1);
			while (bits.get(toFlip)) {
				toFlip = random.nextInt(size - 1);
			}
			bits.flip(toFlip);
		}
		bits.flip(size - 1);
		Chromosome<BitGene> testChromosome = BitChromosome.of(bits);
		MutatorResult<Chromosome<BitGene>> value = SwapByManaCostMutator.getSimpleSmartSwapMutatorResult(testChromosome, 1, random, indexInBitmap);

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
	public void simpleCardSwapMutationTest() {
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
		MutatorResult<Chromosome<BitGene>> value = BitSwapMutator.getBitSwapMutatorResult(testChromosome, 1, random);

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
	public void simpleCardSwapReturnsTest() {
		Random random = new XORShiftRandom(101010L);
		BitSet bits = new BitSet(2);
		Chromosome<BitGene> testChromosome = BitChromosome.of(bits, 2);
		MutatorResult<Chromosome<BitGene>> value = BitSwapMutator.getBitSwapMutatorResult(testChromosome, 1, random);
		assertTrue(testChromosome.equals(value.getResult()));

		bits.flip(0);
		bits.flip(1);
		MutatorResult<Chromosome<BitGene>> value2 = BitSwapMutator.getBitSwapMutatorResult(testChromosome, 1, random);
		assertTrue(testChromosome.equals(value2.getResult()));
	}
}
