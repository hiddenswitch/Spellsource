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
package org.encog.ml.graph;

import java.util.ArrayList;
import java.util.List;

public class BasicPath {
	
	private final List<BasicNode> nodes = new ArrayList<BasicNode>();
	
	public BasicPath(BasicNode startingPoint) {
		this.nodes.add(startingPoint);
	}

	public BasicPath(BasicPath path, BasicNode newNode) {
		this.nodes.addAll(path.getNodes());
		this.nodes.add(newNode);
	}

	public List<BasicNode> getNodes() {
		return nodes;
	}

	public BasicNode getDestinationNode() {
		if( this.nodes.size()==0)
			return null;
		return this.nodes.get(this.nodes.size()-1);
	}

	public int size() {
		return this.nodes.size();
	}
	
	public String toString() {
		boolean first = true;
		StringBuilder result = new StringBuilder();
		result.append("[BasicPath: ");
		for(BasicNode node: this.nodes) {
			if( !first ) {
				result.append(',');
			}
			result.append(node.toString());
			first = false;
		}
		result.append("]");
		return result.toString();
	}
	
}
