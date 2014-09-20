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
package org.encog.ml.prg.generator;

import java.util.List;
import java.util.Random;

import org.encog.ml.prg.EncogProgram;
import org.encog.ml.prg.EncogProgramContext;
import org.encog.ml.prg.ProgramNode;
import org.encog.ml.prg.expvalue.ValueType;

/**
 * The grow generator creates a random program by choosing a random node from
 * both the "function and terminal" sets until the maximum depth is reached.
 * Once the maximum depth is reached only nodes from terminal set are chosen.
 * 
 * This algorithm was implemented as described in the following publication:
 * 
 * Genetic programming: on the programming of computers by means of natural
 * selection MIT Press Cambridge, MA, USA (c)1992 ISBN:0-262-11170-5
 */
public class PrgGrowGenerator extends AbstractPrgGenerator {

	/**
	 * Construct the grow generator.
	 * @param theContext The program context.
	 * @param theMaxDepth The max depth.
	 */
	public PrgGrowGenerator(final EncogProgramContext theContext,
			final int theMaxDepth) {
		super(theContext, theMaxDepth);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ProgramNode createNode(final Random rnd, final EncogProgram program,
			final int depthRemaining, final List<ValueType> types) {
		return createRandomNode(rnd, program, depthRemaining, types, true, true);
	}

}
