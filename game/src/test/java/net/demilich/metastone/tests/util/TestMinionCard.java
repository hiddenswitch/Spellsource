package net.demilich.metastone.tests.util;

import net.demilich.metastone.game.utils.Attribute;
import net.demilich.metastone.game.cards.CardType;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.Rarity;
import net.demilich.metastone.game.cards.desc.CardDesc;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.utils.AttributeMap;
import org.apache.commons.lang3.RandomStringUtils;

public class TestMinionCard extends Card {

	private static int id = 1;

	private static CardDesc getDesc(int attack, int hp, Attribute... attributes) {
		CardDesc desc = new CardDesc();
		desc.id = "test_minion_card";
		desc.name = "Test monster " + ++id;
		desc.rarity = Rarity.FREE;
		desc.baseAttack = attack;
		desc.baseHp = hp;
		desc.type = CardType.MINION;
		desc.heroClass = HeroClass.ANY;
		desc.attributes = new AttributeMap();
		for (Attribute gameTag : attributes) {
			desc.attributes.put(gameTag, true);
		}
		return desc;
	}

	private final Minion minion;

	public TestMinionCard(int baseAttack, int baseHp, Attribute... tags) {
		super(getDesc(baseAttack, baseHp, tags));

		CardDesc desc = getDesc();
		Minion minion1 = new Minion(this);
		for (Attribute gameTag : getAttributes().keySet()) {
			if (!ignoredAttributes.contains(gameTag)) {
				minion1.setAttribute(gameTag, getAttribute(gameTag));
			}
		}

		applyText(minion1);

		minion1.setBaseAttack(getBaseAttack());
		minion1.setAttack(getAttack());
		minion1.setHp(getHp());
		minion1.setMaxHp(getHp());
		minion1.setBaseHp(getBaseHp());
		minion1.setHp(minion1.getMaxHp());

		this.minion = minion1;
		for (Attribute attribute : tags) {
			minion.setAttribute(attribute);
		}
	}

	public TestMinionCard(int baseAttack, int baseHp, int manaCost) {
		super(getDesc(baseAttack, baseHp));
		CardDesc desc = getDesc();
		Minion minion1 = new Minion(this);
		for (Attribute gameTag : getAttributes().keySet()) {
			if (!ignoredAttributes.contains(gameTag)) {
				minion1.setAttribute(gameTag, getAttribute(gameTag));
			}
		}

		applyText(minion1);

		minion1.setBaseAttack(getBaseAttack());
		minion1.setAttack(getAttack());
		minion1.setHp(getHp());
		minion1.setMaxHp(getHp());
		minion1.setBaseHp(getBaseHp());
		minion1.setHp(minion1.getMaxHp());

		this.minion = minion1;
	}

	@Override
	public String getCardId() {
		return "minion_test";
	}

	public Actor getMinion() {
		return minion;
	}

	@Override
	public Minion summon() {
		return minion.clone();
	}

}
