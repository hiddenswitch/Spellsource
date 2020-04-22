package com.hiddenswitch.spellsource.net;

import com.hiddenswitch.spellsource.client.models.Rarity;
import io.vertx.core.json.JsonObject;
import net.demilich.metastone.game.cards.*;
import net.demilich.metastone.game.cards.desc.CardDesc;
import net.demilich.metastone.game.spells.ConditionalSpell;
import net.demilich.metastone.game.spells.SummonSpell;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerArg;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;
import net.demilich.metastone.game.spells.desc.trigger.EnchantmentDesc;
import net.demilich.metastone.game.cards.Attribute;
import org.apache.commons.lang3.ClassUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Converts the card JSON into a table format used for various AI-based card generation algorithms.
 */
public class Generator {
	static final String BATTLECRY = "BattlecryTrigger";
	static final String DEATHRATTLE = "DeathrattleTrigger";

	public class Record implements Cloneable {
		public int rank;
		public int baseManaCost;
		public String heroClass;
		public int rarity;
		public Map<String, Object> args = new HashMap<>();

		public Record() {
			this.args = new HashMap<>();
		}

		public Record(Record from) {
			this();
			this.rank = from.rank;
			this.baseManaCost = from.baseManaCost;
			this.heroClass = from.heroClass;
			this.rarity = from.rarity;
		}

		public Card toCard() {
			// TODO: Actually create a card from this record, if it contains everything we need to do that.
			return null;
		}

		public int getRank() {
			return rank;
		}

		public JsonObject toJson() {
			return new JsonObject(args)
					.put("RANK", rank)
					.put("BASE_MANA_COST", baseManaCost)
					.put("HERO_CLASS", heroClass)
					.put("HERO_CLASS_LABEL", heroClass)
					.put("RARITY", rarity)
					.put("RARITY_LABEL", Rarity.values()[rarity]);
		}
	}

	public List<Record> getSpellRecords() throws CardParseException, IOException, URISyntaxException {
		CardCatalogue.loadCardsFromPackage();

		Stream<Record> spells = CardCatalogue.getRecords().values().stream().map(CardCatalogueRecord::getDesc)
				.flatMap(desc -> toRecords(desc, 0));

		return spells.collect(Collectors.toList());
	}

	private Stream<Record> toRecords(CardDesc cardDesc, int rank) {
		return toRecords(cardDesc, rank, false);
	}

	private Record getBaseRecord(CardDesc cardDesc) {
		Record record = new Record();
		record.baseManaCost = cardDesc.getBaseManaCost();
		record.heroClass = cardDesc.getHeroClass().toString();
		record.rarity = cardDesc.getRarity().ordinal();
		return record;
	}

	private Stream<Record> toRecords(CardDesc cardDesc, int rank, boolean terminal) {
		// Create a minion "spell"
		Record record = getBaseRecord(cardDesc);
		record.rank = rank;
		// Represent this as a summon spell
		record.args.put(SpellArg.CLASS.toString(), SummonSpell.class.getSimpleName());
		record.args.put(SpellArg.SUMMON_BASE_HP.toString(), cardDesc.getBaseHp());
		record.args.put(SpellArg.SUMMON_BASE_ATTACK.toString(), cardDesc.getBaseAttack());
		if (cardDesc.getRace() != null) {
			record.args.put(SpellArg.RACE.toString(), cardDesc.getRace().toString());
		}
		if (cardDesc.getAttributes() != null) {
			record.args.putAll(EnumSet.of(Attribute.WINDFURY, Attribute.TAUNT, Attribute.CHARGE, Attribute.DIVINE_SHIELD, Attribute.STEALTH)
					.stream().collect(Collectors.toMap(k -> "SUMMON_" + k, k -> cardDesc.getAttributes().containsKey(k))));
		}

		// TODO: Put together the battlecry, deathrattle, aura and trigger stuff
		List<EnchantmentDesc> enchantmentDescs = new ArrayList<>();
		if (cardDesc.getBattlecry() != null) {
			EnchantmentDesc battlecryTrigger = new EnchantmentDesc();
			battlecryTrigger.setOneTurn(true);
			Map<EventTriggerArg, Object> args = new HashMap<>();
			args.put(EventTriggerArg.CLASS, BATTLECRY);
			battlecryTrigger.setEventTrigger(new EventTriggerDesc(args));
			// TODO: When reconstructing the card, create the appropriate actions.
			if (cardDesc.getBattlecry().getCondition() != null) {
				Map<SpellArg, Object> conditionalSpell = new SpellDesc(ConditionalSpell.class);
				conditionalSpell.put(SpellArg.CONDITION, cardDesc.getBattlecry().getCondition().create());
				conditionalSpell.put(SpellArg.SPELL, cardDesc.getBattlecry().getSpell());
				battlecryTrigger.setSpell(new SpellDesc(conditionalSpell));
			} else {
				battlecryTrigger.setSpell(cardDesc.getBattlecry().getSpell());
			}
			battlecryTrigger.getSpell().put(SpellArg.TARGET_SELECTION, cardDesc.getBattlecry().getTargetSelection());
			enchantmentDescs.add(battlecryTrigger);
		}

		if (cardDesc.getDeathrattle() != null) {
			EnchantmentDesc deathrattleTrigger = new EnchantmentDesc();
			deathrattleTrigger.setOneTurn(true);
			Map<EventTriggerArg, Object> args = new HashMap<>();
			args.put(EventTriggerArg.CLASS, DEATHRATTLE);
			deathrattleTrigger.setSpell(cardDesc.getDeathrattle());
			enchantmentDescs.add(deathrattleTrigger);
		}

		if (cardDesc.getTrigger() != null) {
			enchantmentDescs.add(cardDesc.getTrigger());
		}

		if (cardDesc.getTriggers() != null) {
			enchantmentDescs.addAll(Arrays.asList(cardDesc.getTriggers()));
		}

		record.args.put("SUB_TRIGGER_COUNT", enchantmentDescs.size());
		record.args.put("SUB_TRIGGER_RANK", rank + 1);

		if (enchantmentDescs.size() == 0) {
			record.args.put(SpellArg.SUMMON_TRIGGERS.toString(), enchantmentDescs);
		}

		return Stream.concat(Stream.of(record),
				enchantmentDescs.stream().flatMap(t -> toRecords(t, record, rank + 1, terminal)));
	}

	private Stream<Record> toRecords(final SpellDesc spellDesc, final Record record, final int rank, final boolean terminal) {
		// Get this spell
		final Record spellRecord = new Record(record);
		spellRecord.rank = rank;

		Map<Object, Object> primitives =
				spellDesc.entrySet().stream()
						.filter(kv -> ClassUtils.isPrimitiveOrWrapper(kv.getValue().getClass()))
						.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

		Map<Object, Object> enumLabels =
				spellDesc.entrySet().stream()
						.filter(kv -> kv.getValue().getClass().isEnum())
						.collect(Collectors.toMap(kv -> kv.getKey().toString() + "_LABEL", kv -> kv.getValue().toString()));

		Map<Object, Object> enumOrdinals =
				spellDesc.entrySet().stream()
						.filter(kv -> kv.getValue().getClass().isEnum())
						.collect(Collectors.toMap(Map.Entry::getKey, kv -> ((Enum) kv.getValue()).ordinal()));

		// Populate the primitives and enums
		spellRecord.args = Stream.of(primitives.entrySet().stream(), enumLabels.entrySet().stream(), enumOrdinals.entrySet().stream())
				.flatMap(Function.identity())
				.collect(Collectors.toMap(kv -> kv.getKey().toString(), Map.Entry::getValue));

		// Handle the spell class specially
		spellRecord.args.put("CLASS", spellDesc.getDescClass().getSimpleName());

		// Handle the target class specially
		if (spellDesc.getTarget() != null) {
			spellRecord.args.put("TARGET", spellDesc.getTarget().getId());
		}

		// Tweak the VALUE
		if (spellRecord.args.containsKey("VALUE")) {
			int value = (int) spellRecord.args.getOrDefault("VALUE", 0);
			spellRecord.args.put("VALUE", Math.abs(value));
			spellRecord.args.put("VALUE_SIGN", value >= 0 ? 1 : 0);
		}


		// Get all the subspells
		SpellArg[] subSpellArgs = {SpellArg.SPELL, SpellArg.SPELL1, SpellArg.SPELL2};
		Stream<Record> subSpells = Stream.of(subSpellArgs)
				.filter(spellDesc::containsKey)
				.flatMap(arg -> toRecords((SpellDesc) spellDesc.get(arg), spellRecord, rank + 1, terminal));

		if (spellDesc.containsKey(SpellArg.SPELLS)) {
			subSpells = Stream.concat(subSpells, Stream.of(((SpellDesc[]) spellDesc.get(SpellArg.SPELLS)))
					.flatMap(desc -> toRecords(desc, spellRecord, rank + 1, terminal)));
		}

		int lowestRankOfSubs = rank + 1;

		if (!terminal) {
			// Handle the card and cards args specially
			int cardCount = 0;
			if (spellDesc.containsKey(SpellArg.CARD)) {
				String cardId = (String) spellDesc.get(SpellArg.CARD);
				Card card = CardCatalogue.getCardById(cardId);
				if (card != null) {
					cardCount += 1;
					int cardRank = card.isCollectible() ? 0 : rank + 1;
					subSpells = Stream.concat(subSpells, toRecords(card.getDesc(), cardRank, true));
					lowestRankOfSubs = Math.min(lowestRankOfSubs, cardRank);
				}
			}

			if (spellDesc.containsKey(SpellArg.CARDS)) {
				String[] cards = (String[]) spellDesc.get(SpellArg.CARDS);
				cardCount += cards.length;
				Stream<Record> subRecords = Arrays.stream(cards).
						flatMap(cardId -> {
							Card card = CardCatalogue.getCardById(cardId);
							if (card != null) {
								return toRecords(card.getDesc(), card.isCollectible() ? 0 : rank + 1, true);
							} else {
								return null;
							}
						}).filter(Objects::nonNull);

				List<Record> subCards = subRecords.collect(Collectors.toList());
				// Find the lowest rank card in these subrecords.
				Record defaultRecord = new Record(record);
				defaultRecord.rank = rank + 1;
				Record lowestRecord = subCards.stream().min(Comparator.comparingInt(Record::getRank)).orElse(defaultRecord);
				lowestRankOfSubs = Math.min(lowestRankOfSubs, lowestRecord.rank);

				subSpells = Stream.concat(subSpells,
						subCards.stream());
			}

			// TODO: In this formulation, cards and spells are equivalent
		}

		List<Record> subSpellList = subSpells.collect(Collectors.toList());

		int subSpellCount = subSpellList.size();
		// Populate the subSpell field
		spellRecord.args.put("SUB_SPELL_COUNT", subSpellCount);
		spellRecord.args.put("SUB_SPELL_RANK", lowestRankOfSubs);
		if (subSpellCount == 0) {
			return Stream.of(spellRecord);
		} else {
			return Stream.concat(Stream.of(spellRecord), subSpellList.stream());
		}
	}

	private Stream<Record> toRecords(EnchantmentDesc enchantmentDesc, Record record, int rank, boolean terminal) {
		// TODO: For now, just emit the spells
		return toRecords(enchantmentDesc.getSpell(), record, rank, terminal);
	}
}
