package com.hiddenswitch.spellsource;

import io.vertx.core.json.JsonObject;
import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.cards.*;
import net.demilich.metastone.game.cards.desc.CardDesc;
import net.demilich.metastone.game.cards.desc.HeroPowerCardDesc;
import net.demilich.metastone.game.cards.desc.MinionCardDesc;
import net.demilich.metastone.game.cards.desc.SpellCardDesc;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.spells.ConditionalSpell;
import net.demilich.metastone.game.spells.SummonSpell;
import net.demilich.metastone.game.spells.desc.BattlecryDesc;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerArg;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;
import net.demilich.metastone.game.spells.desc.trigger.TriggerDesc;
import org.apache.commons.lang3.ClassUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
					.put("HERO_CLASS", HeroClass.valueOf(heroClass).ordinal())
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
		record.baseManaCost = cardDesc.baseManaCost;
		record.heroClass = cardDesc.heroClass.toString();
		record.rarity = cardDesc.rarity.ordinal();
		return record;
	}

	private Stream<Record> toRecords(MinionCardDesc minionCardDesc, int rank, boolean terminal) {
		// Create a minion "spell"
		Record record = getBaseRecord(minionCardDesc);
		record.rank = rank;
		// Represent this as a summon spell
		record.args.put(SpellArg.CLASS.toString(), SummonSpell.class.getSimpleName());
		record.args.put(SpellArg.SUMMON_BASE_HP.toString(), minionCardDesc.baseHp);
		record.args.put(SpellArg.SUMMON_BASE_ATTACK.toString(), minionCardDesc.baseAttack);
		if (minionCardDesc.race != null) {
			record.args.put(SpellArg.SUMMON_RACE.toString(), minionCardDesc.race.toString());
		}
		if (minionCardDesc.attributes != null) {
			record.args.putAll(EnumSet.of(Attribute.WINDFURY, Attribute.TAUNT, Attribute.CHARGE, Attribute.DIVINE_SHIELD, Attribute.STEALTH)
					.stream().collect(Collectors.toMap(k -> "SUMMON_" + k, k -> minionCardDesc.attributes.containsKey(k))));
		}

		// TODO: Put together the battlecry, deathrattle, aura and trigger stuff
		List<TriggerDesc> triggerDescs = new ArrayList<>();
		if (minionCardDesc.battlecry != null) {
			TriggerDesc battlecryTrigger = new TriggerDesc();
			battlecryTrigger.oneTurn = true;
			Map<EventTriggerArg, Object> args = new HashMap<>();
			args.put(EventTriggerArg.CLASS, BATTLECRY);
			battlecryTrigger.eventTrigger = new EventTriggerDesc(args);
			// TODO: When reconstructing the card, create the appropriate actions.
			if (minionCardDesc.battlecry.condition != null) {
				Map<SpellArg, Object> conditionalSpell = SpellDesc.build(ConditionalSpell.class);
				conditionalSpell.put(SpellArg.CONDITION, minionCardDesc.battlecry.condition.create());
				conditionalSpell.put(SpellArg.SPELL, minionCardDesc.battlecry.spell);
				battlecryTrigger.spell = new SpellDesc(conditionalSpell);
			} else {
				battlecryTrigger.spell = minionCardDesc.battlecry.spell;
			}
			battlecryTrigger.spell.put(SpellArg.TARGET_SELECTION, minionCardDesc.battlecry.getTargetSelection());
			triggerDescs.add(battlecryTrigger);
		}

		if (minionCardDesc.deathrattle != null) {
			TriggerDesc deathrattleTrigger = new TriggerDesc();
			deathrattleTrigger.oneTurn = true;
			Map<EventTriggerArg, Object> args = new HashMap<>();
			args.put(EventTriggerArg.CLASS, DEATHRATTLE);
			deathrattleTrigger.spell = minionCardDesc.deathrattle;
			triggerDescs.add(deathrattleTrigger);
		}

		if (minionCardDesc.trigger != null) {
			triggerDescs.add(minionCardDesc.trigger);
		}

		if (minionCardDesc.triggers != null) {
			triggerDescs.addAll(Arrays.asList(minionCardDesc.triggers));
		}

		record.args.put("SUB_TRIGGER_COUNT", triggerDescs.size());
		record.args.put("SUB_TRIGGER_PROBABILTY", triggerDescs.size() == 0 ? 0f : 1f);
		record.args.put("SUB_TRIGGER_RANK", rank + 1);

		if (triggerDescs.size() == 0) {
			record.args.put(SpellArg.SUMMON_TRIGGERS.toString(), triggerDescs);
		}

		return Stream.concat(Stream.of(record),
				triggerDescs.stream().flatMap(t -> toRecords(t, record, rank + 1, terminal)));
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
		spellRecord.args.put("CLASS", spellDesc.getSpellClass().getSimpleName());


		// Get all the subspells
		SpellArg[] subSpellArgs = {SpellArg.SPELL, SpellArg.SPELL_1, SpellArg.SPELL_2};
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
						}).filter(o -> o != null);

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
		spellRecord.args.put("SUB_SPELL_PROBABILITY", subSpellCount == 0 ? 0f : 1f);
		spellRecord.args.put("SUB_SPELL_RANK", lowestRankOfSubs);
		if (subSpellCount == 0) {
			return Stream.of(spellRecord);
		} else {
			return Stream.concat(Stream.of(spellRecord), subSpellList.stream());
		}
	}

	private Stream<Record> toRecords(CardDesc cardDesc, int rank, boolean terminal) {
		if (cardDesc instanceof MinionCardDesc) {
			return toRecords((MinionCardDesc) cardDesc, rank, terminal);
		}

		if (!(cardDesc instanceof SpellCardDesc)) {
			return Stream.empty();
		}

		Record record = getBaseRecord(cardDesc);

		Stream<Record> subCards = Stream.empty();

		// Try to get the first spell out
		SpellCardDesc spellCardDesc = (SpellCardDesc) cardDesc;

		List<SpellDesc> descs = new ArrayList<>();
		if (spellCardDesc.spell != null) {
			SpellDesc spell = spellCardDesc.spell;
			if (spellCardDesc.targetSelection != null) {
				spell.put(SpellArg.TARGET_SELECTION, spellCardDesc.targetSelection);
			}
			descs.add(spell);
		}

		// Get the sub cards (relevant for options)
		if (spellCardDesc instanceof HeroPowerCardDesc) {
			HeroPowerCardDesc heroPowerCardDesc = (HeroPowerCardDesc) spellCardDesc;

			if (heroPowerCardDesc.options != null) {
				// TODO: Actually process the sub cards
				subCards = Arrays.stream(heroPowerCardDesc.options).map(CardCatalogue::getCardById).flatMap(card -> toRecords(card.getDesc(), rank + 1));
			}
		}

		return descs.stream().flatMap(desc -> toRecords(desc, record, rank, terminal));
	}

	private Stream<Record> toRecords(TriggerDesc triggerDesc, Record record, int rank, boolean terminal) {
		// TODO: For now, just emit the spells
		return toRecords(triggerDesc.spell, record, rank, terminal);
	}
}
