package net.demilich.metastone.tests.util;

import net.demilich.metastone.game.actions.PlayCardAction;
import net.demilich.metastone.game.actions.PlaySpellCardAction;
import net.demilich.metastone.game.cards.Card;
import com.hiddenswitch.spellsource.rpc.Spellsource.CardTypeMessage.CardType;
import com.hiddenswitch.spellsource.rpc.Spellsource.RarityMessage.Rarity;
import net.demilich.metastone.game.cards.desc.CardDesc;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.spells.AddSecretSpell;
import net.demilich.metastone.game.spells.DamageSpell;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;
import net.demilich.metastone.game.spells.trigger.PhysicalAttackTrigger;
import net.demilich.metastone.game.spells.trigger.TurnEndTrigger;
import net.demilich.metastone.game.spells.trigger.secrets.Secret;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.targeting.TargetSelection;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.AttributeMap;

public class TestSecretCard extends Card {

	private final int damage;

	private static CardDesc toDesc() {
		CardDesc desc = new CardDesc();
		desc.setId("secret_test_card");
		desc.setName("Trap");
		desc.setRarity(Rarity.FREE);
		desc.setType(CardType.SPELL);
		desc.setHeroClass(HeroClass.ANY);
		desc.setAttributes(new AttributeMap());
		desc.setSecret(new EventTriggerDesc(TurnEndTrigger.class));
		return desc;
	}

	public TestSecretCard() {
		this(1);
	}

	public TestSecretCard(int damage) {
		super(toDesc());
		this.damage = damage;
		getAttributes().put(Attribute.DESCRIPTION, "Secret for unit testing. Deals " + damage + " damage to all enemies");


	}

	@Override
	public PlayCardAction play() {
		SpellDesc damageSpell = DamageSpell.create(EntityReference.ENEMY_CHARACTERS, damage);
		return new PlaySpellCardAction(AddSecretSpell.create(new Secret(new PhysicalAttackTrigger(new EventTriggerDesc(PhysicalAttackTrigger.class)), damageSpell, this)), this, TargetSelection.NONE);
	}
}
