package com.hiddenswitch.deckgeneration;

import io.jenetics.BitChromosome;
import io.jenetics.BitGene;
import io.jenetics.Chromosome;
import io.jenetics.MutatorResult;
import io.jenetics.util.MSeq;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.CardSet;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.logic.XORShiftRandom;
import org.testng.annotations.Test;

import java.util.ArrayList;
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

	@Test
	public void bitSwapBetweenTwoSequencesMutatorWithSingletonChromosomesTest() {
		MSeq<BitGene> seq1 = MSeq.ofLength(2);
		seq1.set(0, BitGene.TRUE);
		seq1.set(1, BitGene.FALSE);
		// seq1 = 01

		MSeq<BitGene> seq2 = MSeq.ofLength(2);
		seq2.set(0, BitGene.FALSE);
		seq2.set(1, BitGene.TRUE);
		// seq = 10

		BitSwapBetweenTwoSequencesMutator mutator = new BitSwapBetweenTwoSequencesMutator(1);
		assertTrue(mutator.crossover(seq1, seq2) == 2);
		// We expect crossover to return 2, since we made
		// successful mutations

		// Now, we expect seq1 = 10 and seq2 = 01
		assertTrue(!seq1.get(0).booleanValue());
		assertTrue(seq1.get(1).booleanValue());
		assertTrue(seq2.get(0).booleanValue());
		assertTrue(!seq2.get(1).booleanValue());
	}

	@Test
	public void bitSwapBetweenTwoSequencesMutatorWhenImpossibleTest() {
		MSeq<BitGene> seq1 = MSeq.ofLength(2);
		seq1.set(0, BitGene.TRUE);
		seq1.set(1, BitGene.TRUE);
		// seq1 = 11

		MSeq<BitGene> seq2 = MSeq.ofLength(2);
		seq2.set(0, BitGene.FALSE);
		seq2.set(1, BitGene.TRUE);
		// seq2 = 10

		BitSwapBetweenTwoSequencesMutator mutator = new BitSwapBetweenTwoSequencesMutator(1);
		assertTrue(mutator.crossover(seq1, seq2) == 0);
		// We expect crossover to return 0, since no changes
		// should be made

		// Now, we still expect seq1 = 11 and seq2 = 10
		assertTrue(seq1.get(0).booleanValue());
		assertTrue(seq1.get(1).booleanValue());
		assertTrue(!seq2.get(0).booleanValue());
		assertTrue(seq2.get(1).booleanValue());

		seq1.set(0, BitGene.FALSE);
		seq1.set(1, BitGene.FALSE);
		// seq1 = 00

		assertTrue(mutator.crossover(seq1, seq2) == 0);
		// We expect crossover to return 0 again

		// Again, we still expect seq1 = 00 and seq2 = 10
		assertTrue(!seq1.get(0).booleanValue());
		assertTrue(!seq1.get(1).booleanValue());
		assertTrue(!seq2.get(0).booleanValue());
		assertTrue(seq2.get(1).booleanValue());
	}

	@Test
	public void bitSwapBetweenTwoSequencesMutatorWithGeneralSequence() {
		// Randomly generate two sequences of length TEST_LENGTH
		final int TEST_LENGTH = 100;
		Random random = new XORShiftRandom(101010L);

		MSeq<BitGene> seq1 = MSeq.ofLength(TEST_LENGTH);
		MSeq<BitGene> seq2 = MSeq.ofLength(TEST_LENGTH);

		for (int i = 0; i < TEST_LENGTH; i++) {
			if (random.nextDouble() < 0.5) {
				seq1.set(i, BitGene.TRUE);
			} else {
				seq1.set(i, BitGene.FALSE);
			}
			if (random.nextDouble() < 0.5) {
				seq2.set(i, BitGene.TRUE);
			} else {
				seq2.set(i, BitGene.FALSE);
			}
		}

		// Store copies of the originals
		MSeq<BitGene> originalSeq1 = MSeq.ofLength(TEST_LENGTH);
		originalSeq1.setAll(seq1);

		MSeq<BitGene> originalSeq2 = MSeq.ofLength(TEST_LENGTH);
		originalSeq2.setAll(seq2);

		BitSwapBetweenTwoSequencesMutator mutator = new BitSwapBetweenTwoSequencesMutator(1);
		int res = mutator.crossover(seq1, seq2);
		assertTrue(res == 2 || res == 0);

		// A difference only occurs if a swap between the sequences occurred in two places
		// In both places, the new sequences must differ from their respective old copies
		int differenceCount = 0;
		for (int i = 0; i < TEST_LENGTH; i++) {
			boolean isDifference = (originalSeq1.get(i) != seq1.get(i) && originalSeq2.get(i) != seq2.get(i) && seq1.get(i) != seq2.get(i));
			boolean isSame = (originalSeq1.get(i) == seq1.get(i) && originalSeq2.get(i) == seq2.get(i));
			assertTrue(isSame || isDifference);
			if (isDifference) {
				differenceCount++;
			}
			// There should never be more than 2 differences
			if (differenceCount > 2) {
				assertTrue(false);
			}
		}

		// The mutator should return the number of alterations made to each
		// and should be reflected by our difference count
		assertTrue(differenceCount == res);
	}

	/**
	 * Tests that the smart swap method does indeed
	 * swap the 1/0 bits representing two cards with
	 * the same mana cost
	 */
	@Test
	public void bitSwapByToAddDuplicateCardsWithSpecificDecksTest() {

		// Create a card list with only 4 cards:
		CardCatalogue.loadCardsFromPackage();
		List<Card> indexInBitmap = new ArrayList<>();
		indexInBitmap.add(CardCatalogue.getCardById("spell_fireball_0"));
		indexInBitmap.add(CardCatalogue.getCardById("spell_fireball_0"));
		indexInBitmap.add(CardCatalogue.getCardById("spell_fireball_1"));
		indexInBitmap.add(CardCatalogue.getCardById("spell_fireball_1"));

		int size = indexInBitmap.size();

		BitSet bits = new BitSet(size);
		bits.flip(0);
		bits.flip(2);
		// bits = 0101

		Random random = new XORShiftRandom(101010L);
		Chromosome<BitGene> testChromosome = BitChromosome.of(bits, size);
		BitSwapToChangeCardCountMutator mutator = new BitSwapToChangeCardCountMutator(1, indexInBitmap);
		MutatorResult<Chromosome<BitGene>> value = mutator.getChromosomeMutatorResult(testChromosome, 1, random);

		// Tests that a card is swapped in to create a 2-set
		// Does not matter whether fireball_0 or fireball_1 is swapped out
		// i.e. We expect either 1100 or 0011
		assertTrue(value.getResult().getGene(0).getAllele() == value.getResult().getGene(1).getAllele());
		assertTrue(value.getResult().getGene(2).getAllele() == value.getResult().getGene(3).getAllele());
		assertTrue(value.getResult().getGene(0).getAllele() != value.getResult().getGene(2).getAllele());

		bits.flip(3);
		// bits = 1101
		// We will now do the same thing, but we want to ensure that
		// our result is 0111 or 1011

		testChromosome = BitChromosome.of(bits, size);
		value = mutator.getChromosomeMutatorResult(testChromosome, 1, random);
		assertTrue(value.getResult().getGene(0).getAllele() && value.getResult().getGene(1).getAllele());
		assertTrue(value.getResult().getGene(2).getAllele() != value.getResult().getGene(3).getAllele());

		bits.flip(0);
		// bits = 1100
		// Now we want to test removing a duplicate
		// Our result should be 1001, 1010, 0101, or 0110

		testChromosome = BitChromosome.of(bits, size);
		value = mutator.getChromosomeMutatorResult(testChromosome, 1, random);
		assertTrue(value.getResult().getGene(0).getAllele() != value.getResult().getGene(1).getAllele());
		assertTrue(value.getResult().getGene(2).getAllele() != value.getResult().getGene(3).getAllele());
	}

	@Test
	public void bitSwapToAddDuplicateCardMutatorWithUniqueCards() {
		Random random = new XORShiftRandom(101010L);

		CardCatalogue.loadCardsFromPackage();
		List<Card> indexInBitmap = new ArrayList<>();
		indexInBitmap.add(CardCatalogue.getCardById("spell_fireball_0"));
		indexInBitmap.add(CardCatalogue.getCardById("spell_fireball_1"));
		indexInBitmap.add(CardCatalogue.getCardById("spell_fireball_2"));
		indexInBitmap.add(CardCatalogue.getCardById("spell_fireball_3"));

		int size = indexInBitmap.size();
		BitSet bits = new BitSet(size);
		bits.flip(0);
		bits.flip(3);

		Chromosome<BitGene> testChromosome = BitChromosome.of(bits, size);
		BitSwapToChangeCardCountMutator mutator = new BitSwapToChangeCardCountMutator(1, indexInBitmap);
		MutatorResult<Chromosome<BitGene>> value = mutator.getChromosomeMutatorResult(testChromosome, 1, random);

		assertTrue(value.getResult().getGene(0).booleanValue() && value.getResult().getGene(3).booleanValue());
		assertTrue(!value.getResult().getGene(1).booleanValue() && !value.getResult().getGene(2).booleanValue());
	}

	@Test
	public void bitSwapToAddDuplicateCardMutatorWithGeneralRandomDecksTest() {
		Random random = new XORShiftRandom(101010L);
		CardCatalogue.loadCardsFromPackage();
		List<Card> indexInBitmap = CardCatalogue.getAll()
				.stream()
				.filter(card -> card.isCollectible()
						&& (card.getHeroClass() == HeroClass.BLUE || card.getHeroClass() == HeroClass.ANY)
						&& card.getCardSet() == CardSet.BASIC && card.getBaseManaCost() == 1).limit(10).collect(Collectors.toList());
		indexInBitmap.addAll(CardCatalogue.getAll()
				.stream()
				.filter(card -> card.isCollectible()
						&& (card.getHeroClass() == HeroClass.BLUE || card.getHeroClass() == HeroClass.ANY)
						&& card.getCardSet() == CardSet.BASIC && card.getBaseManaCost() == 1).limit(10).collect(Collectors.toList()));
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
		BitSwapToChangeCardCountMutator mutator = new BitSwapToChangeCardCountMutator(1, indexInBitmap);
		MutatorResult<Chromosome<BitGene>> value = mutator.getChromosomeMutatorResult(testChromosome, 1, random);

		// Tests that a card is swapped in with the same mana cost
		int count1 = 0;
		int count2 = 0;
		int differences = 0;
		int mutationForDuplicates = 0;

		for (int i = 0; i < testChromosome.length(); i++) {
			if (testChromosome.getGene(i) != value.getResult().getGene(i)) {
				differences++;
				for (int j = 0; j < indexInBitmap.size(); j++) {
					if (indexInBitmap.get(i).getCardId() == indexInBitmap.get(j).getCardId() && i != j && value.getResult().getGene(j).getAllele()) {
						mutationForDuplicates += 1;
					}
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
		assertTrue(mutationForDuplicates >= 1);
	}


}
