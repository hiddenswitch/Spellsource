package com.hiddenswitch.deckgeneration;

import io.jenetics.*;
import io.jenetics.util.MSeq;
import net.demilich.metastone.game.logic.XORShiftRandom;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Random;

import static org.testng.Assert.assertTrue;

public class TestSpecificChromosomesMutators {
	// Tests that the actsOnSpecificChromosomesMutator only mutates certain
	// chromosomes of a genotype
	@Test
	public void testActsOnSpecificChromosomesMutator() {
		int bitLength = 8;

		BitSet bits = new BitSet(bitLength);
		Chromosome<BitGene> chromosome = BitChromosome.of(bits, bitLength);
		Genotype<BitGene> genotype = Genotype.of(chromosome, chromosome);

		List<Integer> chromosomesToActOn = new ArrayList<>();
		chromosomesToActOn.add(1);

		ActsOnSpecificChromosomesBasicMutator actsOnSpecificChromosomesMutator = new ActsOnSpecificChromosomesBasicMutator(1, chromosomesToActOn);
		Random random = new XORShiftRandom(101010L);
		MutatorResult<Genotype<BitGene>> result = actsOnSpecificChromosomesMutator.mutate(genotype, 1, random);
		Chromosome<BitGene> resultChromosome1 = result.getResult().getChromosome(0);
		Chromosome<BitGene> resultChromosome2 = result.getResult().getChromosome(1);
		for (int i = 0; i < bitLength; i++) {
			assertTrue(!resultChromosome1.getGene(i).getAllele());
		}
		boolean isMutation = false;
		for (int i = 0; i < bitLength; i++) {
			if (resultChromosome2.getGene(i).getAllele()) {
				isMutation = true;
				break;
			}
		}
		assertTrue(isMutation);
	}

	@Test
	public void testBitSwapOnSpecificChromosomesMutator() {
		int bitLength = 2;

		BitSet bits = new BitSet(bitLength);
		bits.flip(0);
		// bits = 01
		Chromosome<BitGene> chromosome = BitChromosome.of(bits, bitLength);
		Genotype<BitGene> genotype = Genotype.of(chromosome, chromosome);
		// Beginning genotype: [01, 01]

		List<Integer> chromosomesToActOn = new ArrayList<>();
		chromosomesToActOn.add(1);

		BitSwapOnSpecificChromosomesMutator bitSwapOnSpecificChromosomesMutator = new BitSwapOnSpecificChromosomesMutator(1, chromosomesToActOn);
		Random random = new XORShiftRandom(101010L);
		MutatorResult<Genotype<BitGene>> result = bitSwapOnSpecificChromosomesMutator.mutate(genotype, 1, random);
		Chromosome<BitGene> resultChromosome1 = result.getResult().getChromosome(0);
		Chromosome<BitGene> resultChromosome2 = result.getResult().getChromosome(1);

		// Expected genotype: [01, 10]
		assertTrue(resultChromosome1.getGene(0).getAllele());
		assertTrue(!resultChromosome1.getGene(1).getAllele());
		assertTrue(!resultChromosome2.getGene(0).getAllele());
		assertTrue(resultChromosome2.getGene(1).getAllele());
	}

	@Test
	public void testMultiPointCrossoverOnSpecificChromosomes() {
		int bitLength = 10;
		BitSet bits1 = new BitSet(bitLength);
		BitSet bits2 = new BitSet(bitLength);
		for (int i = 0; i < bitLength / 2; i++) {
			bits1.flip(i);
			bits2.flip(i + (bitLength / 2));
		}
		Chromosome<BitGene> c1 = BitChromosome.of(bits1, bitLength);
		Chromosome<BitGene> c2 = BitChromosome.of(bits2, bitLength);
		Genotype<BitGene> gt1 = Genotype.of(c1, c1);
		Genotype<BitGene> gt2 = Genotype.of(c2, c2);

		List<Integer> chromosomesToCross = new ArrayList<>();
		chromosomesToCross.add(1);

		MultiPointCrossoverOnSpecificChromosomes mutator = new MultiPointCrossoverOnSpecificChromosomes(1, 2, chromosomesToCross);
		Random random = new XORShiftRandom(101010L);
		List<MSeq<Chromosome<BitGene>>> c = mutator.getCrossedChromosomes(gt1, gt2, random);
		assertTrue(!c.isEmpty());

		MSeq<Chromosome<BitGene>> r1 = c.get(0);
		MSeq<Chromosome<BitGene>> r2 = c.get(1);

		for (int i = 0; i < bitLength / 2; i++) {
			assertTrue(r1.get(0).getGene(i).booleanValue());
			assertTrue(!r1.get(0).getGene(i + (bitLength / 2)).booleanValue());
			assertTrue(!r2.get(0).getGene(i).booleanValue());
			assertTrue(r2.get(0).getGene(i + (bitLength / 2)).booleanValue());
		}

		int differences = 0;

		for (int i = 0; i < bitLength; i++) {
			assertTrue(r1.get(1).getGene(i).booleanValue() ^ r2.get(1).getGene(i).booleanValue());
			if (r1.get(1).getGene(i).booleanValue() ^ r2.get(1).getGene(i).booleanValue()) {
				differences++;
			}
		}
		assertTrue(differences > 0);
	}
}
