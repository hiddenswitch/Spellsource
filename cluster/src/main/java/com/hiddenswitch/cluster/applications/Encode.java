package com.hiddenswitch.cluster.applications;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import com.hiddenswitch.spellsource.client.models.CardType;
import net.demilich.metastone.game.cards.desc.CardDesc;
import net.demilich.metastone.game.cards.desc.Desc;
import net.demilich.metastone.game.cards.desc.HasDesc;
import net.demilich.metastone.game.decks.DeckFormat;
import net.demilich.metastone.game.spells.desc.BattlecryDesc;
import net.demilich.metastone.game.spells.desc.trigger.EnchantmentDesc;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.AttributeMap;
import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;
import java.util.stream.Stream;

public class Encode {
	private static final String FORMAT = "format";


	public static void main(String[] args) throws IOException {
		EncodingConfiguration encodingConfiguration = new EncodingConfiguration().parseCommandLine(args);

		if (!encodingConfiguration.isValid()) {
			return;
		}

		String chosenFormat = encodingConfiguration.getChosenFormat();
		CardCatalogue.loadCardsFromPackage();
		if (chosenFormat.equals("tables")) {
			ListMultimap<String, JsonObject> records = getAsTables();

			for (String key : records.asMap().keySet()) {
				JsonArray jsonArray = new JsonArray(records.get(key));
				FileUtils.writeStringToFile(new File(key + ".json"), jsonArray.encode());
			}
		}
	}

	@NotNull
	public static ListMultimap<String, JsonObject> getAsTables() {
		ListMultimap<String, JsonObject> records = LinkedListMultimap.create();

		for (Card card : CardCatalogue.getAll()) {
			if (!DeckFormat.spellsource().isInFormat(card.getCardSet())) {
				continue;
			}

			if (card.isChooseOne()) {
				continue;
			}

			if (card.isSecret()) {
				continue;
			}

			if (card.getCardType() != CardType.MINION
					&& card.getCardType() != CardType.SPELL) {
				continue;
			}

			// Save card, queue up valid descs
			final CardDesc desc = card.getDesc();

			JsonObject cardRecord = new JsonObject();
			cardRecord.put("class", desc.getType())
					.put("depth", 0)
					.put("rarity", desc.getRarity())
					.put("type", desc.getType())
					.put("heroClass", desc.getHeroClass() == null ? desc.getHeroClasses()[0] : desc.getHeroClass())
					.put("baseManaCost", desc.getBaseManaCost());

			if (desc.getType() == CardType.MINION) {
				cardRecord.put("baseAttack", desc.getBaseAttack())
						.put("baseHp", desc.getBaseHp());
			}

			AttributeMap attributes = new AttributeMap(desc.getAttributes());

			Stream.of(
					Attribute.STEALTH,
					Attribute.CHARGE,
					Attribute.TAUNT,
					Attribute.CANNOT_ATTACK,
					Attribute.LIFESTEAL,
					Attribute.POISONOUS,
					Attribute.DIVINE_SHIELD,
					Attribute.WINDFURY,
					Attribute.PERMANENT
			).forEach(booleanAttribute ->
					cardRecord.put(booleanAttribute.toKeyCase(), (boolean) attributes.getOrDefault(booleanAttribute, false)));

			Stream.of(Attribute.SPELL_DAMAGE,
					Attribute.OVERLOAD
			).forEach(intAttribute ->
					cardRecord.put(intAttribute.toKeyCase(), (int) attributes.getOrDefault(intAttribute, 0)));

			// Breadth first
			ProcessResults results = new ProcessResults();

			// Sub components
			Map<String, ProcessResults> subComponents = new LinkedHashMap<>();
			subComponents.put("spell", process(desc.getSpell(), null, 0, "CardDesc"));
			subComponents.put("battlecry", process(desc.getBattlecry(), null, 0, "CardDesc"));
			subComponents.put("deathrattle", process(desc.getDeathrattle(), null, 0, "CardDesc"));
			subComponents.put("triggers", process(desc.getTrigger(), desc.getTriggers(), 0, "CardDesc"));
			subComponents.put("manaCostModifiers", process(desc.getManaCostModifier(), null, 0, "CardDesc"));
			subComponents.put("auras", process(desc.getAura(), desc.getAuras(), 0, "CardDesc"));
			subComponents.put("passiveAuras", process(null, desc.getPassiveAuras(), 0, "CardDesc"));
			subComponents.put("passiveTriggers", process(desc.getPassiveTrigger(), desc.getPassiveTriggers(), 0, "CardDesc"));
			subComponents.put("deckTriggers", process(desc.getDeckTrigger(), desc.getDeckTriggers(), 0, "CardDesc"));
			subComponents.put("gameTriggers", process(null, desc.getGameTriggers(), 0, "CardDesc"));

			// Process fields for sub components
			for (Map.Entry<String, ProcessResults> res : subComponents.entrySet()) {
				if (res.getValue().hasResult()) {
					final String thisType = res.getValue().thisType;
					if (thisType.equals("EnchantmentDesc")) {
						// Put the spell and trigger key instead of "EnchantmentDesc"
						JsonObject eventTrigger = res.getValue().records.get("EventTriggerDesc").get(0);
						JsonObject spell = res.getValue().records.get("SpellDesc").get(0);
						cardRecord.put(res.getKey() + "EventTrigger", eventTrigger.getString("CLASS"));
						cardRecord.put(res.getKey() + "Spell", spell.getString("CLASS"));
					} else if (thisType.equals("BattlecryDesc")) {
						if (res.getValue().records.get("ConditionDesc").size() > 0) {
							JsonObject condition = res.getValue().records.get("ConditionDesc").get(0);
							cardRecord.put(res.getKey() + "Condition", condition.getString("CLASS"));
						}
						JsonObject spell = res.getValue().records.get("SpellDesc").get(0);
						cardRecord.put(res.getKey() + "Spell", spell.getString("CLASS"));
					} else {
						cardRecord.put(res.getKey(), thisType);
					}
				}
				cardRecord.put(res.getKey() + "Size", res.getValue().records.size());
			}


			records.put("CardDesc", cardRecord);

			LinkedList<ProcessResults> queue = new LinkedList<>();
			ListMultimap<String, JsonObject> components = LinkedListMultimap.create();
			queue.addAll(subComponents.values());
			while (queue.size() > 0) {
				ProcessResults next = queue.pollFirst();
				components.putAll(next.records);
				if (next.hasResult()) {
					for (HasDesc subItem : next.next) {
						queue.push(process(subItem, null, next.depth + 1, next.thisType));
					}
				}
			}

			// All the components that were emitted by this card should inherit some important values
			for (Map.Entry<String, JsonObject> component : components.entries()) {
				component.getValue().put("baseManaCost", desc.getBaseManaCost());
				if (desc.getType() == CardType.MINION) {
					component.getValue()
							.put("baseHp", desc.getBaseHp())
							.put("baseAttack", desc.getBaseAttack());
				}
			}

			records.putAll(components);
			// TODO: Handle card references where the card that it references is itself
		}
		return records;
	}

	public static <T> LinkedList<T> link(T single, T[] multi) {
		if (single == null && (multi == null || multi.length == 0)) {
			return new LinkedList<>();
		}

		LinkedList<T> returnList = new LinkedList<>();
		if (single != null) {
			returnList.add(single);
		}

		if (multi != null
				&& multi.length > 0) {
			returnList.addAll(Arrays.asList(multi));
		}
		return returnList;
	}

	public static ProcessResults process(EnchantmentDesc single, EnchantmentDesc[] multi, int depth, String parent) {
		// Custom handling of enchantment desc
		final LinkedList<EnchantmentDesc> linked = link(single, multi);
		ProcessResults results = new ProcessResults();
		results.depth = depth;
		results.thisType = "EnchantmentDesc";

		final int size = linked.size();
		if (size == 0) {
			return results;
		}


		for (int i = 0; i < size; i++) {
			EnchantmentDesc desc = linked.get(i);
			ProcessResults eventTrigger = process(desc.eventTrigger, null, depth, parent);
			ProcessResults spell = process(desc.spell, null, depth, parent);
			// Include EnchantmentDesc base object
			JsonObject record = new JsonObject();
			record.put("CLASS", "EnchantmentDesc")
					.put("parent", parent)
					.put("index", i)
					.put("size", size)
					.put("depth", depth)
					.put("spell", spell.records.get("SpellDesc").get(0).getString("CLASS"))
					.put("eventTrigger", eventTrigger.records.get("EventTriggerDesc").get(0).getString("CLASS"))
					.put("countByValue", desc.countByValue)
					.put("maxFires", desc.maxFires)
					.put("countUntilCast", desc.countUntilCast)
					.put("keepAfterTransform", desc.keepAfterTransform)
					.put("oneTurn", desc.oneTurn)
					.put("persistentOwner", desc.persistentOwner);
			results.records.put("EnchantmentDesc", record);
			results.merge(spell);
			results.merge(eventTrigger);
		}

		return results;
	}

	public static ProcessResults process(BattlecryDesc single, BattlecryDesc[] multi, int depth, String parent) {
		// Custom handling of enchantment desc
		final LinkedList<BattlecryDesc> linked = link(single, multi);
		ProcessResults results = new ProcessResults();
		results.depth = depth;
		results.thisType = "BattlecryDesc";

		final int size = linked.size();
		if (size == 0) {
			return results;
		}

		for (int i = 0; i < size; i++) {
			BattlecryDesc desc = linked.get(i);
			ProcessResults condition = process(desc.condition, null, depth, parent);
			ProcessResults spell = process(desc.spell, null, depth, parent);
			// Include EnchantmentDesc base object
			JsonObject record = new JsonObject();

			record.put("CLASS", "BattlecryDesc")
					.put("parent", parent)
					.put("index", i)
					.put("size", size)
					.put("depth", depth)
					.put("spell", spell.records.get("SpellDesc").get(0).getString("CLASS"))
					.put("targetSelection", desc.targetSelection);

			if (condition.hasResult()) {
				record.put("condition", condition.records.get("ConditionDesc").get(0).getString("CLASS"));
			}

			results.records.put("BattlecryDesc", record);
			results.merge(spell);
			results.merge(condition);
		}

		return results;
	}

	public static <T extends HasDesc> ProcessResults process(T single, T[] multi, int depth, String parent) {
		final LinkedList<T> linked = link(single, multi);
		ProcessResults results = new ProcessResults();
		results.depth = depth;

		final int size = linked.size();
		if (size == 0) {
			return results;
		}

		String thisType = linked.get(0).getDesc().getDescClass().getSimpleName();
		String thisComponentClass = linked.get(0).getDesc().getClass().getSimpleName();
		results.thisType = thisType;

		for (int i = 0; i < size; i++) {
			Desc<?, ?> desc = linked.get(i).getDesc();
			JsonObject record = new JsonObject();
			results.records.put(thisComponentClass, record);
			record.put("size", size)
					.put("index", i)
					.put("depth", depth)
					.put("parent", parent);

			String thisParent = desc.getDescClass().getSimpleName();

			for (Map.Entry<? extends Enum, Object> kv : desc.entrySet()) {
				if (kv.getValue() instanceof Class) {
					record.put(kv.getKey().name(), ((Class) kv.getValue()).getSimpleName());
				} else if (kv.getValue() instanceof Integer) {
					// Record as a specially postfixed attribute, because it's almost always alternatively a value
					record.put(kv.getKey().name() + "Int", kv.getValue())
							.put(kv.getKey().name(), "Integer");
				} else if (kv.getValue() instanceof EnchantmentDesc) {
					// Process it now
					results.merge(process((EnchantmentDesc) kv.getValue(), null, depth, parent));
				} else if (kv.getValue() instanceof HasDesc) {
					Desc subDesc = ((HasDesc) kv.getValue()).getDesc();
					// Retrieve the desc class and put it into the field, then queue it up for processing
					record.put(kv.getKey().name(), subDesc.getDescClass().getSimpleName());
					results.next.add(subDesc);
				} else if (kv.getValue().getClass().isArray()) {
					if (kv.getValue().getClass().getComponentType().isAssignableFrom(HasDesc.class)) {
						// Link the array elements
						ProcessResults subArray = process(null, (HasDesc[]) kv.getValue(), depth, thisParent);
						results.merge(subArray);
						// Record the class of the first item and the size
						final int subSize = results.records.size();
						record.put(kv.getKey() + "Size", subSize);
						if (subSize > 0) {
							record.put(kv.getKey().name(), subArray.records.entries().iterator().next().getValue().getString("CLASS"));
						}
					} else if (kv.getValue().getClass().getComponentType().isPrimitive()) {
						// Skip these for now, just record their length
						record.put(kv.getKey().name(), Array.getLength(kv.getValue()));
					}
				} else if (kv.getValue().getClass().isPrimitive()
						|| kv.getValue() instanceof Boolean) {
					record.put(kv.getKey().name(), kv.getValue());
				} else {
					// Just works for enums and strings, the values we haven't encountered yet
					record.put(kv.getKey().name(), kv.getValue().toString());
				}
			}
		}

		return results;
	}

	public static class ProcessResults {
		public String thisType;
		public int depth = 0;
		public List<HasDesc> next = new LinkedList<>();
		public ListMultimap<String, JsonObject> records = ArrayListMultimap.create();

		public boolean hasResult() {
			return records.size() > 0;
		}

		public ProcessResults merge(ProcessResults other) {
			next.addAll(other.next);
			records.putAll(other.records);
			return this;
		}
	}

	static class EncodingConfiguration {
		private boolean invalid;
		private String chosenFormat;

		public EncodingConfiguration() {
		}

		boolean isValid() {
			return !invalid;
		}

		public String getChosenFormat() {
			return chosenFormat;
		}

		public EncodingConfiguration parseCommandLine(String... args) {
			Options options = new Options();
			Option format = new Option(Character.toString(FORMAT.charAt(0)), FORMAT, true, "The format to encode to. Valid options are \"tables\", \"tensorflow\"");
			format.setRequired(true);
			options.addOption(format);
			CommandLineParser parser = new DefaultParser();
			CommandLine cmd = null;
			try {
				cmd = parser.parse(options, args);
			} catch (ParseException e) {
				System.err.println(e.getMessage());
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp("--format tables", options);
				invalid = true;
				return this;
			}

			chosenFormat = cmd.getOptionValue(FORMAT);
			invalid = false;
			return this;
		}
	}
}
