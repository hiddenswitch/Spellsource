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
package org.encog.neural.activation;

import junit.framework.TestCase;

import org.encog.engine.network.activation.ActivationGaussian;
import org.junit.Assert;
import org.junit.Test;

public class TestActivationGaussian extends TestCase {
	
	@Test
	public void testGaussian() throws Throwable
	{
		ActivationGaussian activation = new ActivationGaussian();
		Assert.assertFalse(!activation.hasDerivative());
		
		ActivationGaussian clone = (ActivationGaussian)activation.clone();
		Assert.assertNotNull(clone);
		
		double[] input = { 0.0  };
		
		activation.activationFunction(input,0,input.length);
		
		Assert.assertEquals(1.0,input[0],0.1);

	}
}
