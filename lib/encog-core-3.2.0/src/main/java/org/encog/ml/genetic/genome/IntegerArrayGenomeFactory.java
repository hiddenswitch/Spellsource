/*
 * Encog(tm) Core v3.2 - Java Version
 * http://www.heatonresearch.com/encog/
 * https://github.com/encog/encog-java-core
 
 * Copyright 2008-2013 Heaton Research, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *   
 * For more information on Heaton Research copyrights, licenses 
 * and trademarks visit:
 * http://www.heatonresearch.com/copyright
 */
package org.encog.ml.genetic.genome;

import org.encog.ml.ea.genome.Genome;
import org.encog.ml.ea.genome.GenomeFactory;

/**
 * A factory to create integer genomes of a specific size.
 */
public class IntegerArrayGenomeFactory implements GenomeFactory {
	
	/**
	 * The size of genome to create.
	 */
	private int size;
	
	/**
	 * Create the integer genome of a fixed size.
	 * @param theSize The size to use.
	 */
	public IntegerArrayGenomeFactory(int theSize) {
		this.size = theSize;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Genome factor() {
		return new IntegerArrayGenome(this.size);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Genome factor(Genome other) {
		return new IntegerArrayGenome( ((IntegerArrayGenome)other));
	}
}
