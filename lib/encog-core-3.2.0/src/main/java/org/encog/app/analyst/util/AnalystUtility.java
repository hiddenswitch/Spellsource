package org.encog.app.analyst.util;

import java.io.File;

import org.encog.EncogError;
import org.encog.app.analyst.AnalystFileFormat;
import org.encog.app.analyst.EncogAnalyst;
import org.encog.app.analyst.analyze.PerformAnalysis;
import org.encog.app.analyst.csv.TimeSeriesUtil;
import org.encog.app.analyst.csv.normalize.AnalystNormalizeCSV;
import org.encog.app.analyst.script.AnalystScript;
import org.encog.app.analyst.script.normalize.AnalystField;
import org.encog.app.analyst.script.prop.ScriptProperties;
import org.encog.ml.data.MLData;
import org.encog.ml.data.MLDataPair;
import org.encog.ml.data.MLDataSet;
import org.encog.ml.data.basic.BasicMLDataPair;
import org.encog.ml.data.basic.BasicMLDataSet;
import org.encog.util.arrayutil.NormalizationAction;
import org.encog.util.csv.CSVFormat;
import org.encog.util.csv.ReadCSV;
import org.encog.util.logging.EncogLogging;

/**
 * Provides an interface to the analyst usually used by other programs.
 */
public class AnalystUtility {
	/**
	 * The analyst we are using.
	 */
	final private EncogAnalyst analyst;
	
	/**
	 * Construct the analyst utility.
	 * @param theAnalyst The analyst that we are using.
	 */
	public AnalystUtility(EncogAnalyst theAnalyst) {
		this.analyst = theAnalyst;
	}
	
	/**
	 * Encode fields, using the analyst.
	 * @param includeInput Should we include the input fields.
	 * @param includeOutput Should we include the output fields.
	 * @param rawData The raw data to encode from.
	 * @param encodedData The data to encode to.
	 */
	public void encode(
			boolean includeInput,
			boolean includeOutput,
			double[] rawData, 
			MLData encodedData) {
		int rawIndex = 0;
		int outputIndex = 0;
		
		for (final AnalystField stat : analyst.getScript().getNormalize()
				.getNormalizedFields()) {

			if (stat.isIgnored()) {
				continue;
			}
			
			if (stat.isOutput() && !includeOutput ) {
				continue;
			}
			
			if( stat.isInput() && !includeInput ) {
				continue;
			}
			
			if (stat.getAction() == NormalizationAction.Normalize) {
				encodedData.setData(outputIndex++, stat.normalize(rawData[rawIndex++]));
			} else if (stat.getAction() == NormalizationAction.PassThrough) {
				encodedData.setData(outputIndex++, rawData[rawIndex++]);
			} else {
				final double[] d = stat.encode(rawData[rawIndex++]);
				for (final double element : d) {
					encodedData.setData(outputIndex++, element);
				}
			}
		}
	}
	
	/**
	 * Load a CSV file into an MLDataSet.
	 * @param file The file to load.
	 * @return The loaded data set.
	 */
	public MLDataSet loadCSV(File file) {
		if (this.analyst == null) {
			throw new EncogError(
					"Can't normalize yet, file has not been analyzed.");
		}
		
		MLDataSet result = new BasicMLDataSet();
		
		int inputCount = this.analyst.determineInputCount();
		int outputCount = this.analyst.determineOutputCount();
		int totalCount = inputCount+outputCount;
		
		boolean headers = this.analyst.getScript().getProperties()
				.getPropertyBoolean(ScriptProperties.SETUP_CONFIG_INPUT_HEADERS);
		
		final CSVFormat format = this.analyst.getScript().determineFormat();

		CSVHeaders analystHeaders = new CSVHeaders(file, headers,
				format);
		
		ReadCSV csv = new ReadCSV(file.toString(), headers, format);
		
		for (final AnalystField field : analyst.getScript().getNormalize()
				.getNormalizedFields()) {
			field.init();
		}

		TimeSeriesUtil series = new TimeSeriesUtil(analyst,true,
				analystHeaders.getHeaders());
		

		try {
			// write file contents
			while (csv.next()) {

				double[] output = AnalystNormalizeCSV.extractFields(
						this.analyst, analystHeaders, csv, totalCount,
						false);

				if (series.getTotalDepth() > 1) {
					output = series.process(output);
				}

				MLDataPair pair = BasicMLDataPair.createPair(inputCount,outputCount);
				for(int i=0;i<inputCount;i++) {
					pair.getInput().setData(i, output[i]);
				}
				for(int i=0;i<outputCount;i++) {
					pair.getIdeal().setData(i, output[i+inputCount]);
				}
				result.add(pair);
			}
			return result;
		} finally {
			if (csv != null) {
				try {
					csv.close();
				} catch (final Exception ex) {
					EncogLogging.log(ex);
				}
			}
		}
	}

	/**
	 * Load a CSV file into an MLDataSet.
	 * @param file The file to load.
	 * @return The loaded data set.
	 */
	public MLDataSet loadCSV(String filename) {
		return loadCSV(new File(filename));
	}

	/**
	 * Decode fields, using the analyst.
	 * @param includeInput Should we include the input fields.
	 * @param includeOutput Should we include the output fields.
	 * @param rawData The raw data to encode to.
	 * @param encodedData The data to encode from.
	 */
	public void decode(
			boolean includeInput,
			boolean includeOutput,
			double[] rawData, 
			MLData encodedData) {
		int rawIndex = 0;
		int outputIndex = 0;
		
		for (final AnalystField stat : analyst.getScript().getNormalize()
				.getNormalizedFields()) {

			if (stat.isIgnored()) {
				continue;
			}
			
			if (stat.isOutput() && !includeOutput ) {
				continue;
			}
			
			if( stat.isInput() && !includeInput ) {
				continue;
			}
			
			if (stat.getAction() == NormalizationAction.Normalize) {
				rawData[rawIndex++] = stat.deNormalize(encodedData.getData(outputIndex++));
			} else if (stat.getAction() == NormalizationAction.PassThrough) {
				rawData[rawIndex++] = encodedData.getData(outputIndex++);
			} else {
				rawData[rawIndex++] = stat.determineClass(outputIndex, encodedData.getData()).getIndex();
				outputIndex+=stat.getColumnsNeeded();
			}
		}
	}

	public void analyze(File theFilename) {
		final boolean headers = this.analyst.getScript().getProperties().getPropertyBoolean(ScriptProperties.SETUP_CONFIG_INPUT_HEADERS);
		final AnalystFileFormat fmt = this.analyst.getScript().getProperties().getPropertyFormat(ScriptProperties.SETUP_CONFIG_CSV_FORMAT);
		
		
		PerformAnalysis analyze = new PerformAnalysis(
				this.analyst.getScript(),
				theFilename.toString(), 
				headers,
				fmt);
		
		analyze.process(this.analyst);
	}
}
