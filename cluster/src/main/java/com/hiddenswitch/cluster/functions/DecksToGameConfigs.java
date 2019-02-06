package com.hiddenswitch.cluster.functions;

import com.hiddenswitch.cluster.models.TestConfig;
import net.demilich.metastone.game.cards.CardParseException;
import org.apache.spark.api.java.function.PairFlatMapFunction;
import scala.Tuple2;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Iterator;

public class DecksToGameConfigs implements PairFlatMapFunction<String[], TestConfig, Object> {
	private int batches;
	private int gamesPerBatch;

	public DecksToGameConfigs() throws CardParseException, IOException, URISyntaxException {
	}

	public DecksToGameConfigs(int batches, int gamesPerBatch) throws IOException, URISyntaxException, CardParseException {
		this();
		this.batches = batches;
		this.gamesPerBatch = gamesPerBatch;
	}

	@Override
	public Iterator<Tuple2<TestConfig, Object>> call(String[] decklists) throws Exception {
		throw new UnsupportedOperationException();
	}
}
