package com.hiddenswitch.deckgeneration;

import io.jenetics.Chromosome;
import io.jenetics.Gene;
import io.jenetics.MutatorResult;
import net.demilich.metastone.game.cards.Card;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

/**
 * A mutator that swaps a card in a deck for one
 * not in the deck with the same mana cost
 *
 * @param <G>
 * @param <C>
 */
public class BitSwapByManaCostMutator<
		G extends Gene<Boolean, G>,
		C extends Comparable<? super C>
		>
		extends BitSwapMutator<G, C> {

	private List<Card> indexInBitmap;
	private ThreadLocal<Integer> manaCost = ThreadLocal.withInitial(() -> -1);
	private ThreadLocal<Integer> selectedOne = ThreadLocal.withInitial(() -> -1);

	/**
	 * Constructs an alterer that swaps a card in a deck
	 * for one not in the deck, but of the same mana cost
	 *
	 * @param probability the crossover probability.
	 * @throws IllegalArgumentException if the {@code probability} is not in the
	 *                                  valid range of {@code [0, 1]}.
	 */
	public BitSwapByManaCostMutator(final double probability, List<Card> indexInBitmap) {
		super(probability);
		this.indexInBitmap = indexInBitmap;
	}

	@NotNull
	@Override
	public MutatorResult<Chromosome<G>> getBitSwapMutatorResult(Chromosome<G> chromosome, double p, Random random) {
		if (p < random.nextDouble()) {
			return MutatorResult.of(chromosome);
		}

		try {
			if (chromosome.length() != indexInBitmap.size()) {
				throw new IllegalArgumentException("chromosome.length() != indexInBitmap.size()");
			}

			List<Integer> indicesOfTrueBits = IntStream.range(0, chromosome.length())
					.filter(i -> chromosome.getGene(i).getAllele())
					.boxed()
					.collect(toList());

			if (indicesOfTrueBits.isEmpty()) {
				return MutatorResult.of(chromosome);
			}

			int numberOfZeroes;
			int gene;
			do {
				int index = random.nextInt(indicesOfTrueBits.size());
				gene = indicesOfTrueBits.remove(index);
				manaCost.set(indexInBitmap.get(gene).getBaseManaCost());
				numberOfZeroes = getNumberOfZeroes(chromosome, indicesOfTrueBits.size());
			} while (numberOfZeroes == 0 && !indicesOfTrueBits.isEmpty());

			if (numberOfZeroes == 0) {
				return MutatorResult.of(chromosome);
			}

			selectedOne.set(gene);
			return super.getBitSwapMutatorResult(chromosome, 1, random);
		} finally {
			manaCost.set(-1);
		}
	}

	@Override
	protected int getNumberOfZeroes(Chromosome<G> chromosome, int numberOfOnes) {
		return (int) IntStream.range(0, chromosome.length()).filter(i -> isZero(chromosome, i)).count();
	}

	@Override
	protected int getNumberOfOnes(Chromosome<G> chromosome) {
		return 1;
	}

	@Override
	protected boolean isZero(Chromosome<G> chromosome, int i) {
		return super.isZero(chromosome, i) && indexInBitmap.get(i).getBaseManaCost() == manaCost.get();
	}

	@Override
	protected boolean isOne(Chromosome<G> chromosome, int i) {
		return i == selectedOne.get();
	}
}
