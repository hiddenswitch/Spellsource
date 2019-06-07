package com.hiddenswitch.deckgeneration;

import io.jenetics.*;
import net.demilich.metastone.game.logic.XORShiftRandom;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Random;

import static org.testng.Assert.assertTrue;

public class TestSpecificChromosomesMutators {
	@Test
	public void testActsOnSpecificChromosomesMutator() {
		int bitLength = 8;

		BitSet bits = new BitSet(bitLength);
		Chromosome<BitGene> chromosome = BitChromosome.of(bits, bitLength);
		Genotype<BitGene> genotype = Genotype.of(chromosome, chromosome);

		List<Integer> chromosomesToActOn = new ArrayList<>();
		chromosomesToActOn.add(1);

		ActsOnSpecificChromosomesMutator actsOnSpecificChromosomesMutator = new ActsOnSpecificChromosomesMutator(1, chromosomesToActOn);
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
}
