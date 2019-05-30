package com.hiddenswitch.deckgeneration;

import io.jenetics.*;
import io.jenetics.util.MSeq;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

/**
 * The mutator that swaps a card in a deck with
 * one not in the deck
 *
 * @param <G>
 * @param <C>
 */
public class SimpleCardSwapMutator<
        G extends Gene<?, G>,
        C extends Comparable<? super C>
        >
        extends SwapMutator<G, C> {
    /**
     * Constructs an alterer that swaps a 1 and 0 in the BitChromosome
     * (in our case, swapping out a card in the deck for one
     * not in the deck)
     *
     * @param probability the crossover probability.
     * @throws IllegalArgumentException if the {@code probability} is not in the
     *                                  valid range of {@code [0, 1]}.
     */
    public SimpleCardSwapMutator(final double probability) {
        super(probability);
    }

    /**
     * Default constructor, with default mutation probability
     * ({@link AbstractAlterer#DEFAULT_ALTER_PROBABILITY}).
     */
    public SimpleCardSwapMutator() {
        this(DEFAULT_ALTER_PROBABILITY);
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
        return getSimpleCardSwapMutatorResult(chromosome, p, random);
    }

    /**
     * Swaps a 1 and 0 in the given array, with the mutation probability of this
     * mutation.
     *
     * @param chromosome The Chromosome that represents our deck
     * @param p          The probability of making a swap
     * @param random     The Random object used for selecting our indices
     * @param <G>        The type of our gene (BitGene is recommended)
     * @return The mutated Chromosome
     */
    @NotNull
    public static <G extends Gene<?, G>> MutatorResult<Chromosome<G>> getSimpleCardSwapMutatorResult(Chromosome<G> chromosome, double p, Random random) {
        final MutatorResult<Chromosome<G>> result;
        // Calculates the number of cards in the deck
        int numberOfOnes = ((BitChromosome) chromosome).bitCount();
        if (chromosome.length() > 1
                && numberOfOnes != chromosome.length()
                && numberOfOnes != 0
                && random.nextDouble() <= p) {
            final MSeq<G> genes = chromosome.toSeq().copy();
            int firstCard = random.nextInt(chromosome.length());
            while (true) {
                // If i is in the deck then randomly select cards until we find one
                // not in the deck, or vice versa
                int secondCard = random.nextInt(genes.length());
                if (!genes.get(secondCard).equals(genes.get(firstCard))) {
                    genes.swap(firstCard, secondCard);
                    break;
                }
            }
            result = MutatorResult.of(
                    chromosome.newInstance(genes.toISeq())
            );
        } else {
            result = MutatorResult.of(chromosome);
        }
        return result;
    }
}
