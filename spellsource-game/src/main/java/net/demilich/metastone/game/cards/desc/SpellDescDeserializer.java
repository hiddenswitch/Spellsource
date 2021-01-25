package net.demilich.metastone.game.cards.desc;

import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;

/**
 * The serializer that interprets JSON representations of a spell.
 */
public class SpellDescDeserializer extends DescDeserializer<SpellDesc, SpellArg, Spell> {
	protected SpellDescDeserializer(Class<? extends SpellDesc> vc) {
		super(SpellDesc.class);
	}

	public SpellDescDeserializer() {
		super(SpellDesc.class);
	}

	@Override
	protected SpellDesc createDescInstance() {
		return new SpellDesc();
	}

	@Override
	public void init(SerializationContext ctx) {
		ctx.add(SpellArg.ATTACK_BONUS, ParseValueType.VALUE);
		ctx.add(SpellArg.ATTRIBUTE, ParseValueType.ATTRIBUTE);
		ctx.add(SpellArg.AURA, ParseValueType.AURA);
		ctx.add(SpellArg.ARMOR_BONUS, ParseValueType.VALUE);
		ctx.add(SpellArg.BOARD_POSITION_ABSOLUTE, ParseValueType.VALUE);
		ctx.add(SpellArg.BOARD_POSITION_RELATIVE, ParseValueType.BOARD_POSITION_RELATIVE);
		ctx.add(SpellArg.CANNOT_RECEIVE_OWNED, ParseValueType.BOOLEAN);
		ctx.add(SpellArg.CARD, ParseValueType.STRING);
		ctx.add(SpellArg.CARD_COST_MODIFIER, ParseValueType.CARD_COST_MODIFIER);
		ctx.add(SpellArg.CARD_DESC_TYPE, ParseValueType.CARD_DESC_TYPE);
		ctx.add(SpellArg.CARD_FILTER, ParseValueType.ENTITY_FILTER);
		ctx.add(SpellArg.CARD_FILTERS, ParseValueType.ENTITY_FILTER_ARRAY);
		ctx.add(SpellArg.CARD_LOCATION, ParseValueType.CARD_LOCATION);
		ctx.add(SpellArg.CARD_SOURCE, ParseValueType.CARD_SOURCE);
		ctx.add(SpellArg.CARD_SOURCES, ParseValueType.CARD_SOURCE_ARRAY);
		ctx.add(SpellArg.CARD_TYPE, ParseValueType.CARD_TYPE);
		ctx.add(SpellArg.CARDS, ParseValueType.STRING_ARRAY);
		ctx.add(SpellArg.CONDITION, ParseValueType.CONDITION);
		ctx.add(SpellArg.CONDITIONS, ParseValueType.CONDITION_ARRAY);
		ctx.add(SpellArg.DESCRIPTION, ParseValueType.STRING);
		ctx.add(SpellArg.EXCLUSIVE, ParseValueType.BOOLEAN);
		ctx.add(SpellArg.FILTER, ParseValueType.ENTITY_FILTER);
		ctx.add(SpellArg.FULL_MANA_CRYSTALS, ParseValueType.BOOLEAN);
		ctx.add(SpellArg.GROUP, ParseValueType.STRING);
		ctx.add(SpellArg.HOW_MANY, ParseValueType.VALUE);
		ctx.add(SpellArg.HP_BONUS, ParseValueType.VALUE);
		ctx.add(SpellArg.IGNORE_SPELL_DAMAGE, ParseValueType.BOOLEAN);
		ctx.add(SpellArg.MANA, ParseValueType.VALUE);
		ctx.add(SpellArg.NAME, ParseValueType.STRING);
		ctx.add(SpellArg.OPERATION, ParseValueType.ALGEBRAIC_OPERATION);
		ctx.add(SpellArg.QUEST, ParseValueType.QUEST);
		ctx.add(SpellArg.PACT, ParseValueType.QUEST);
		ctx.add(SpellArg.RACE, ParseValueType.STRING);
		ctx.add(SpellArg.RANDOM_TARGET, ParseValueType.BOOLEAN);
		ctx.add(SpellArg.REVERT_TRIGGER, ParseValueType.EVENT_TRIGGER);
		ctx.add(SpellArg.SECRET, ParseValueType.SECRET);
		ctx.add(SpellArg.SECOND_REVERT_TRIGGER, ParseValueType.EVENT_TRIGGER);
		ctx.add(SpellArg.SECONDARY_NAME, ParseValueType.STRING);
		ctx.add(SpellArg.SECONDARY_TARGET, ParseValueType.TARGET_REFERENCE);
		ctx.add(SpellArg.SECONDARY_VALUE, ParseValueType.VALUE);
		ctx.add(SpellArg.SOURCE, ParseValueType.TARGET_REFERENCE);
		ctx.add(SpellArg.SPELL, ParseValueType.SPELL);
		ctx.add(SpellArg.SPELL1, ParseValueType.SPELL);
		ctx.add(SpellArg.SPELL2, ParseValueType.SPELL);
		ctx.add(SpellArg.SPELLS, ParseValueType.SPELL_ARRAY);
		ctx.add(SpellArg.TARGET, ParseValueType.TARGET_REFERENCE);
		ctx.add(SpellArg.TARGET_PLAYER, ParseValueType.TARGET_PLAYER);
		ctx.add(SpellArg.TARGET_SELECTION, ParseValueType.TARGET_SELECTION);
		ctx.add(SpellArg.TRIGGER, ParseValueType.TRIGGER);
		ctx.add(SpellArg.TRIGGERS, ParseValueType.TRIGGERS);
		ctx.add(SpellArg.VALUE, ParseValueType.VALUE);
		ctx.add(SpellArg.BATTLECRY, ParseValueType.BATTLECRY);
		ctx.add(SpellArg.ZONES, ParseValueType.ZONES);
	}

	@Override
	protected Class<Spell> getAbstractComponentClass() {
		return Spell.class;
	}

	@Override
	protected Class<SpellArg> getEnumType() {
		return SpellArg.class;
	}
}

