package net.demilich.metastone.tests.util;

import net.demilich.metastone.game.actions.PlayCardAction;
import net.demilich.metastone.game.actions.PlaySpellCardAction;
import net.demilich.metastone.game.cards.CardType;
import net.demilich.metastone.game.cards.Rarity;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.desc.CardDesc;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.TargetSelection;
import net.demilich.metastone.game.utils.Attribute;
import net.demilich.metastone.game.utils.AttributeMap;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;

public class TestSpellCard extends Card {

	private final SpellDesc spell;

	private static CardDesc toDesc() {
		CardDesc desc = new CardDesc();
		desc.id = RandomStringUtils.randomAlphanumeric(15);
		desc.name = "Unit Test Spell";
		desc.rarity = Rarity.FREE;
		desc.type = CardType.SPELL;
		desc.heroClass = HeroClass.ANY;
		desc.attributes = new AttributeMap();
		return desc;
	}

	public TestSpellCard(SpellDesc spell) {
		super(toDesc());
		getAttributes().put(Attribute.DESCRIPTION, "This spell can have various effects and should only be used in the context of unit net.demilich.metastone.tests.");
		this.spell = spell;
		setTargetRequirement(TargetSelection.NONE);
	}

	@Override
	public PlayCardAction play() {
		return new PlaySpellCardAction(spell, this, TargetSelection.NONE);
	}
}
