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
package org.encog.ml.graph.search;

import java.util.HashSet;
import java.util.Set;

import org.encog.EncogError;
import org.encog.ml.graph.BasicEdge;
import org.encog.ml.graph.BasicGraph;
import org.encog.ml.graph.BasicNode;
import org.encog.ml.graph.BasicPath;

public abstract class AbstractGraphSearch implements GraphSearch {
	
	private final BasicGraph graph;
	private final SearchGoal goal;
	private final FrontierHolder frontier = new FrontierHolder(this);
	private final Set<BasicNode> explored = new HashSet<BasicNode>();
	private BasicPath solution;

	public AbstractGraphSearch(BasicGraph theGraph, BasicNode startingPoint, SearchGoal theGoal)
	{
		this.graph = theGraph;
		this.goal = theGoal;
		frontier.add(new BasicPath(startingPoint));
	}

	public BasicGraph getGraph() {
		return graph;
	}

	public SearchGoal getGoal() {
		return goal;
	}
	
	public void iteration() {
		if (solution == null) {
					
			if( this.frontier.size()==0 ) {
				throw new EncogError("Frontier is empty, cannot find solution.");
			}
			
			BasicPath path = this.frontier.pop();

			if (this.goal.isGoalMet(path)) {
				this.solution = path;
				return;
			}

			BasicNode state = path.getDestinationNode();
			this.explored.add(state);
			
			for (BasicEdge connection : state.getConnections()) {
				if( !this.explored.contains(connection.getTo()) &&
					!this.frontier.containsDestination(connection.getTo())) {
					BasicPath path2 = new BasicPath(path, connection.getTo());
					this.frontier.add(path2);
				}
			}
		}
	}

	/**
	 * @return the solution
	 */
	public BasicPath getSolution() {
		return solution;
	}
	
	

}
