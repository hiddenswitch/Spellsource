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
package org.encog.ml;


/**
 * Used by simulated annealing and genetic algorithms to calculate the score
 * for a machine learning method.  This allows networks to be ranked.  We may be seeking
 * a high or a low score, depending on the value the shouldMinimize
 * method returns.
 */
public interface CalculateScore {
	
	/**
	 * Calculate this network's score.
	 * @param method The ML method.
	 * @return The score.
	 */
	double calculateScore(MLMethod method);
	
	/**
	 * @return True if the goal is to minimize the score.
	 */
	boolean shouldMinimize();

	/**
	 * @return True, if this score function cannot be done in parallel.
	 */
	boolean requireSingleThreaded();
}
