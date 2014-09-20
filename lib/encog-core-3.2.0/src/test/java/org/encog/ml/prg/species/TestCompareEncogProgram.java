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
package org.encog.ml.prg.species;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.encog.Encog;
import org.encog.ml.prg.EncogProgram;

public class TestCompareEncogProgram extends TestCase {
	
	public double eval(String prg1, String prg2) {
		EncogProgram expression1 = new EncogProgram(prg1);
		EncogProgram expression2 = new EncogProgram(prg2);
		CompareEncogProgram comp = new CompareEncogProgram();
		return comp.compare(expression1, expression2);
	}
	
	
	public void testSingle() {
		Assert.assertEquals(2.0, eval("1+x","x+1"), Encog.DEFAULT_DOUBLE_EQUAL);
	}
}
