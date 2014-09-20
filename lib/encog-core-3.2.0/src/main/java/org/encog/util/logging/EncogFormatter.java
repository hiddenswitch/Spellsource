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
package org.encog.util.logging;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;


/**
 * A simple formatter for logging.
 * 
 * @author jheaton
 * 
 */
public class EncogFormatter extends Formatter {

	/**
	 * Format the log record.
	 * 
	 * @param record
	 *            What to log.
	 * @return The formatted log record.
	 */
	@Override
	public String format(final LogRecord record) {
		final StringBuilder result = new StringBuilder();
		result.append("[");
		result.append(record.getLevel());
		result.append("] [");
		result.append(record.getSourceClassName());
		result.append("] ");
		result.append(record.getMessage());
		result.append("\n");
		return result.toString();
	}

}
