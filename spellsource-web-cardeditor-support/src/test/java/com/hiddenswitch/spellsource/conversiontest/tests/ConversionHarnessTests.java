package com.hiddenswitch.spellsource.conversiontest.tests;

import com.hiddenswitch.spellsource.conversiontest.ConversionHarness;
import io.vertx.core.json.Json;
import net.demilich.metastone.game.cards.catalogues.ClasspathCardCatalogue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Execution(ExecutionMode.SAME_THREAD)
public class ConversionHarnessTests {

	@Test
	public void testConversionHarness() throws IOException {
        var cardCatalogue = ClasspathCardCatalogue.INSTANCE;
		var desc = cardCatalogue.getCardById(cardCatalogue.getOneOneNeutralMinionCardId()).getDesc();
		var json = Json.encodePrettily(desc);
		assertTrue(ConversionHarness.assertCardReplaysTheSame(new long[]{1L}, cardCatalogue.getOneOneNeutralMinionCardId(), json), "json was " + json);
	}
}