package com.hiddenswitch.spellsource;

import com.google.common.io.Files;
import io.vertx.core.json.JsonObject;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardParseException;
import net.demilich.metastone.game.cards.SpellCard;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.SpellUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.reflections.Reflections;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.junit.Assert.*;

public class GeneratorTest {
	@Test
	public void testSpellRecords() throws Exception {
		Generator generator = new Generator();
		List<Generator.Record> spells = generator.getSpellRecords();
		assertTrue(spells.size() > 0);
		List<JsonObject> jsonObjectStream = spells.stream().map(Generator.Record::toJson).collect(toList());
		Set<String> columns = jsonObjectStream.stream().flatMap(record -> record.fieldNames().stream()).filter(k -> k != null).distinct().collect(toSet());

		// Fix the TARGET argument
		jsonObjectStream.stream().filter(json -> json.containsKey("TARGET")).forEach(json -> {
			json.put("TARGET", -json.getInteger("TARGET") - 1);
		});

		// Fix the CLASS argument
		// Create an "enum"
		Reflections reflections = new Reflections("net.demilich.metastone.game.spells");
		Set<Class<? extends Spell>> spellClasses = reflections.getSubTypesOf(Spell.class);
		int count = spellClasses.size();
		List<String> spellClassNames = spellClasses.stream().map(Class::getSimpleName).sorted().collect(toList());
		Map<String, Integer> spellClassOrdinals =
				IntStream.range(0, count).boxed().collect(toMap(spellClassNames::get, Function.identity()));

		jsonObjectStream.forEach(json -> {
			String classLabel = json.getString("CLASS");
			json.remove("CLASS");
			json.put("CLASS_LABEL", classLabel);
			json.put("CLASS", spellClassOrdinals.get(classLabel));
		});

		columns.add("CLASS_LABEL");

		// Fix binary arguments
		jsonObjectStream.forEach(json -> {
			Map<String, Object> added = new HashMap<>();
			json.getMap().entrySet().stream().filter(kv -> kv.getValue() instanceof Boolean).forEach(kv -> {
				added.put(kv.getKey(), (boolean) kv.getValue() ? 1 : 0);
				added.put(kv.getKey() + "_LABEL", kv.getValue().toString());
				columns.add(kv.getKey() + "_LABEL");
			});

			json.getMap().putAll(added);
		});

		// Perform the category counts (you could do this in Pandas too)
		Map<String, Integer> nCategories = columns.stream()
				// Find all the columns that contain enum values
				.filter(column -> columns.stream().anyMatch(c -> c.equals(column + "_LABEL")) || column.equals("TARGET"))
				.collect(
						// Compute the number of categories
						toMap(Function.identity(), column ->
								// By finding the largest ordinal value in an enum value and adding one to it.
								// Use -1 for the default value because -1 + 1 = 0, which will be the count of categories if all the categories were -1 (i.e., not defined)
								jsonObjectStream.stream().max(Comparator.comparingInt(record1 -> record1.getInteger(column, -1)))
										// If no valid value was ever found, the amount is zero
										.orElse(new JsonObject().put(column, -1)).getInteger(column) + 1));


		// Output CSV
		String csv =
				(columns.stream()
						.sorted()
						// Concatenate
						.reduce((a1, a2) -> a1 + "," + a2).orElse("") + "\n")
						.concat(jsonObjectStream.stream().map(record -> columns.stream().sorted().map(column -> {
							Object value = record.getMap().get(column);
							if (value == null) {
								return "";
							}
							return value.toString();
						}).reduce((a1, a2) -> a1 + "," + a2).orElse("")).reduce((l1, l2) -> l1 + "\n" + l2).orElse(""));
		Files.write(csv, new File("data.csv"), Charset.defaultCharset());

		// Output CGPM population
		Files.write("CREATE POPULATION spells for hearthstone_spells WITH SCHEMA(" + Stream.concat(columns.stream().sorted().map(column -> {
			if (column.endsWith("LABEL") || column.equals("SUMMON_TRIGGERS")) {
				return String.format("IGNORE %s", column);
			} else if (nCategories.containsKey(column)) {
				return String.format("MODEL %s AS CATEGORICAL", column);
			} else if (column.endsWith("RANK") || column.endsWith("COUNT") || column.equals("HOW_MANY")) {
				return String.format("MODEL %s AS COUNTS", column);
			} else {
				return String.format("MODEL %s AS NUMERICAL", column);
			}
		}), Stream.of("IGNORE index")).reduce((a1, a2) -> a1 + ";\n" + a2).orElse("") + ");", new File("population.bql"), Charset.defaultCharset());
	}

	@Test
	public void testNewSummonSpell() throws CardParseException, IOException, URISyntaxException {
		GameContext gc = GameContext.uninitialized(HeroClass.MAGE, HeroClass.HUNTER);
		Player player1 = gc.getPlayer1();
		Card source = (new Generator()).getSpellRecords().get(0).toCard();
		assertNotNull(SpellUtils.getMinionCardFromSummonSpell(gc, player1, source, ((SpellCard) source).getSpell()));
	}

}