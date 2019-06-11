package com.hiddenswitch.deckgeneration;

import io.jenetics.BitGene;
import io.jenetics.Genotype;
import io.jenetics.util.Factory;

import java.util.List;

public class SpecificGenotypesFactory implements Factory<Genotype<BitGene>> {
	List<Genotype<BitGene>> genotypes;
	int index = 0;

	public SpecificGenotypesFactory(List<Genotype<BitGene>> genotypes) {
		this.genotypes = genotypes;
	}

	@Override
	public Genotype<BitGene> newInstance() {
		Genotype<BitGene> toReturn = genotypes.get(index);
		index = (index + 1) % genotypes.size();
		return toReturn;
	}
}
