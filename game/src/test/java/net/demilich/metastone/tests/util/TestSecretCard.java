package net.demilich.metastone.tests.util;

import net.demilich.metastone.game.cards.CardType;
import net.demilich.metastone.game.cards.Rarity;
import net.demilich.metastone.game.cards.SecretCard;
import net.demilich.metastone.game.cards.desc.SecretCardDesc;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.spells.DamageSpell;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;
import net.demilich.metastone.game.spells.trigger.PhysicalAttackTrigger;
import net.demilich.metastone.game.spells.trigger.TurnEndTrigger;
import net.demilich.metastone.game.spells.trigger.secrets.Secret;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.utils.AttributeMap;

public class TestSecretCard extends SecretCard {

	private static SecretCardDesc toDesc() {
		SecretCardDesc desc = new SecretCardDesc();
		desc.name = "Trap";
		desc.rarity = Rarity.FREE;
		desc.type = CardType.SPELL;
		desc.heroClass = HeroClass.ANY;
		desc.attributes = new AttributeMap();
		desc.trigger = EventTriggerDesc.createEmpty(TurnEndTrigger.class);
		return desc;
	}

	public TestSecretCard() {
		this(1);
	}

	public TestSecretCard(int damage) {
		super(toDesc());
		setDescription("Secret for unit testing. Deals " + damage + " damage to all enemies");
		setCollectible(false);

		SpellDesc damageSpell = DamageSpell.create(EntityReference.ENEMY_CHARACTERS, damage);
		setSecret(new Secret(new PhysicalAttackTrigger(EventTriggerDesc.createEmpty(PhysicalAttackTrigger.class)), damageSpell, this));
	}

}
