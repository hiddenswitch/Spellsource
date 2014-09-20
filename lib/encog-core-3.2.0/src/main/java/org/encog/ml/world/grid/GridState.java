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
package org.encog.ml.world.grid;

import org.encog.ml.world.basic.BasicState;
import org.encog.util.Format;

public class GridState extends BasicState {
	
	private final int row;
	private final int column;
	private final GridWorld owner;
	
	public GridState(GridWorld theOwner, int theRow, int theColumn, boolean blocked) {
		this.owner = theOwner;
		this.row = theRow;
		this.column = theColumn;
	}

	/**
	 * @return the row
	 */
	public int getRow() {
		return row;
	}

	/**
	 * @return the column
	 */
	public int getColumn() {
		return column;
	}

	/**
	 * @return the owner
	 */
	public GridWorld getOwner() {
		return owner;
	}
	
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append("[GridState: row=");
		result.append(this.row);
		result.append(", col=");
		result.append(this.column);
		
		result.append(", valueFunction= ");
		for(int i=0;i<this.getPolicyValue().length;i++) {
			result.append(Format.formatDouble(getPolicyValue()[i], 4));
			result.append(" ");
		}
		
		result.append("]");
		return result.toString();
	}
	
}
