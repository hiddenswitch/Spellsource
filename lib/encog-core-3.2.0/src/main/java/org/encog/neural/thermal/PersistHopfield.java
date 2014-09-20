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
package org.encog.neural.thermal;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import org.encog.persist.EncogFileSection;
import org.encog.persist.EncogPersistor;
import org.encog.persist.EncogReadHelper;
import org.encog.persist.EncogWriteHelper;
import org.encog.persist.PersistConst;

/**
 * Persist the Hopfield network.
 * 
 */
public class PersistHopfield implements EncogPersistor {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getFileVersion() {
		return 1;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getPersistClassString() {
		return HopfieldNetwork.class.getSimpleName();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object read(final InputStream is) {
		final HopfieldNetwork result = new HopfieldNetwork();
		final EncogReadHelper in = new EncogReadHelper(is);
		EncogFileSection section;

		while ((section = in.readNextSection()) != null) {
			if (section.getSectionName().equals("HOPFIELD")
					&& section.getSubSectionName().equals("PARAMS")) {
				final Map<String, String> params = section.parseParams();
				result.getProperties().putAll(params);
			}
			if (section.getSectionName().equals("HOPFIELD")
					&& section.getSubSectionName().equals("NETWORK")) {
				final Map<String, String> params = section.parseParams();
				result.setWeights(section.parseDoubleArray(params,
						PersistConst.WEIGHTS));
				result.setCurrentState(section.parseDoubleArray(params,
						PersistConst.OUTPUT));
				result.setNeuronCount(EncogFileSection.parseInt(params,
						PersistConst.NEURON_COUNT));
			}
		}

		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void save(final OutputStream os, final Object obj) {
		final EncogWriteHelper out = new EncogWriteHelper(os);
		final HopfieldNetwork hopfield = (HopfieldNetwork) obj;
		out.addSection("HOPFIELD");
		out.addSubSection("PARAMS");
		out.addProperties(hopfield.getProperties());
		out.addSubSection("NETWORK");
		out.writeProperty(PersistConst.WEIGHTS, hopfield.getWeights());
		out.writeProperty(PersistConst.OUTPUT, hopfield.getCurrentState()
				.getData());
		out.writeProperty(PersistConst.NEURON_COUNT, hopfield.getNeuronCount());
		out.flush();
	}

}
