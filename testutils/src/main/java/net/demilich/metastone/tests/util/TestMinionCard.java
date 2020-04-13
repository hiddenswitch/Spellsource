package net.demilich.metastone.tests.util;

import net.demilich.metastone.game.cards.Card;
import com.hiddenswitch.spellsource.client.models.CardType;
import com.hiddenswitch.spellsource.client.models.Rarity;
import net.demilich.metastone.game.cards.desc.CardDesc;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.AttributeMap;

public class TestMinionCard extends Card {

	private static int id = 1;

	private static CardDesc getDesc(int attack, int hp, Attribute... attributes) {
		CardDesc desc = new CardDesc();
		desc.setId("test_minion_card");
		desc.setName("Test monster " + ++id);
		desc.setRarity(Rarity.FREE);
		desc.setBaseAttack(attack);
		desc.setBaseHp(hp);
		desc.setType(CardType.MINION);
		desc.setHeroClass(HeroClass.ANY);
		desc.setAttributes(new AttributeMap());
		for (Attribute gameTag : attributes) {
			desc.getAttributes().put(gameTag, true);
		}
		return desc;
	}

	private final Minion minion;

	public TestMinionCard(int baseAttack, int baseHp, Attribute... tags) {
		super(getDesc(baseAttack, baseHp, tags));

		CardDesc desc = getDesc();
		Minion minion1 = new Minion(this);
		for (Attribute gameTag : getAttributes().keySet()) {
			if (!Card.IGNORED_MINION_ATTRIBUTES.contains(gameTag)) {
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
			if (!Card.IGNORED_MINION_ATTRIBUTES.contains(gameTag)) {
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
