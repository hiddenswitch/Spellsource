package com.hiddenswitch.spellsource.testutils;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.hiddenswitch.spellsource.rpc.Spellsource.CardTypeMessage.CardType;
import com.hiddenswitch.spellsource.core.ResourceInputStream;
import io.vertx.core.json.DecodeException;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.*;
import net.demilich.metastone.game.cards.catalogues.ClasspathCardCatalogue;
import net.demilich.metastone.game.spells.SpellUtils;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.filter.EntityFilterArg;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.tests.util.TestBase;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CardValidation {
	static {
	}

	private static final CardParser CARD_PARSER = new CardParser();

	public static Object[][] getCardFiles(String... paths) {
		return getCardFiles(Arrays.stream(paths).map(Path::of).toArray(Path[]::new));
	}

	public static Object[][] getCardFiles(Path... paths) {
		if (paths.length == 0) {
			return new Object[0][0];
		}
		var path = paths[0];
		var j = 0;
		while (!Files.exists(path) && j + 1 < paths.length) {
			j++;
			path = paths[j];
		}
		if (!Files.exists(path)) {
			return new Object[0][0];
		}
		var ALL_CARD_FILES = (List<File>) FileUtils.listFiles(
				path.toFile(),
				new RegexFileFilter("^(.*json)"),
				DirectoryFileFilter.DIRECTORY);

		var size = ALL_CARD_FILES.size();
		File file;
		var matrix = (Object[][]) Array.newInstance(Object.class, size, 1);
		for (var i = 0; i < size; i++) {
			file = ALL_CARD_FILES.get(i);
			matrix[i][0] = file;
		}

		return matrix;
	}

	public static void validateCard(File cardFile) throws IOException {
		var resourceInputStream = new ResourceInputStream(cardFile.getName(), new FileInputStream(cardFile));

		try {
			var record = CARD_PARSER.parseCard(resourceInputStream);
			if (record.getDesc().getType() != CardType.FORMAT) {
				assertFalse(record.getDesc().getHeroClass() == null && (record.getDesc().getHeroClasses() == null || record.getDesc().getHeroClasses().length == 0));
			}

			// Test glow does not cause crash when evaluated from hand
			var testContext = fromTwoRandomDecks();
			testContext.init();
			var card = record.getDesc().create();
			testContext.getLogic().receiveCard(0, card);
			record.getDesc().getGlowConditions().forEach(c -> c.isFulfilled(testContext, testContext.getPlayer1(), card, null));
			var description = record.getDesc().getDescription();
			if (description != null) {
				var attributes = record.getDesc().getAttributes();
				if (description.startsWith("Battlecry:")
						|| description.startsWith("Opener:")) {
					assertTrue(attributes != null && attributes.containsKey(Attribute.BATTLECRY),
							"An Opener card " + cardFile.getAbsolutePath() + " is missing the BATTLECRY attribute.");
					assertNull(record.getDesc().getSpell(), "An Opener card " + cardFile.getAbsolutePath() + " has a spell specified and it probably should be an opener.");
				}

				if (description.startsWith("Deathrattle:")
						|| description.startsWith("Aftermath:")) {
					assertTrue(attributes != null && attributes.containsKey(Attribute.DEATHRATTLES),
							"An Aftermath card" + cardFile.getAbsolutePath() + " is missing the DEATHRATTLES attribute.");
				}

				if (record.getDesc().getDeathrattle() != null) {
					assertNotEquals(record.getDesc().getDeathrattle().getTarget(), EntityReference.ADJACENT_MINIONS,
							"Deathrattles trigger from the graveyard, so they cannot contain a reference to ADJACENT_MINIONS. Use a custom.AdjacentDeathrattleSpell instead.");
				}
			}
		} catch (DecodeException ex) {
			var innerEx = (JsonProcessingException) ex.getCause();
			if (innerEx != null) {
				var location = innerEx.getLocation() != null ? innerEx.getLocation() : new JsonLocation(cardFile, 0, 0, 0);
				fail(String.format("%s\n%s",
						cardFile.getAbsolutePath() + ":" + location.getLineNr(),
						innerEx.getMessage()));
			}
		} catch (Exception ex) {
			fail(cardFile.getName(), ex);
		}
	}

	public static void validateCardReferences(File cardFile) throws IOException {
		var resourceInputStream = new ResourceInputStream(cardFile.getName(), new FileInputStream(cardFile));

		try {
			var record = CARD_PARSER.parseCard(resourceInputStream);
			record.getDesc().bfs().build().forEach(node -> {
				if (node.getKey().equals(SpellArg.CARD) || node.getKey().equals(EntityFilterArg.CARD)) {
					var card = (String) node.getValue();
					if (SpellUtils.getSpecialCards().contains(card)) {
						return;
					}
					ClasspathCardCatalogue.INSTANCE.getCardById(card);
				} else if (node.getKey().equals(SpellArg.CARDS) || node.getKey().equals(EntityFilterArg.CARDS)) {
					if (node.getValue() instanceof String[]) {
						var cards = (String[]) node.getValue();
						for (var card : cards) {
							if (SpellUtils.getSpecialCards().contains(card)) {
								continue;
							}
							ClasspathCardCatalogue.INSTANCE.getCardById(card);
						}
					}
				}
			});
		} catch (DecodeException ex) {
			// Does not deal with this issue here
		} catch (Exception ex) {
			fail("Card " + cardFile.getAbsolutePath() + " references " + ex.getMessage() + " which cannot be found", ex);
		}
	}

	/**
	 * Creates a new game context with two random decks and random play behaviour.
	 *
	 * @return A game context
	 * @see #play() to actually execute the game.
	 */
	public static GameContext fromTwoRandomDecks() {
		return GameContext.fromDecks(Arrays.asList(TestBase.randomDeck(), TestBase.randomDeck()));
	}
}
