package net.demilich.metastone.game.entities.minions;

import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Actor;
import com.hiddenswitch.spellsource.client.models.EntityType;
import net.demilich.metastone.game.targeting.IdFactory;
import net.demilich.metastone.game.cards.Attribute;

public final class Minion extends Actor {

	public Minion(Card sourceCard) {
		super(sourceCard);
		String race = sourceCard.hasAttribute(Attribute.RACE) ? (String) sourceCard.getAttribute(Attribute.RACE) : Race.NONE;
		setRace(race);
	}

	@Override
	public Minion clone() {
		Minion clone = (Minion) super.clone();
		return clone;
	}

	@Override
	public int getAttack() {
		if (hasAttribute(Attribute.ATTACK_EQUALS_HP) || hasAttribute(Attribute.AURA_ATTACK_EQUALS_HP)) {
			return getHp();
		}
		return super.getAttack();
	}

	@Override
	public EntityType getEntityType() {
		return EntityType.MINION;
	}

	protected void setBaseStats(int baseAttack, int baseHp) {
		setBaseAttack(baseAttack);
		setBaseHp(baseHp);
	}

	@Override
	public boolean equals(Object other) {
		if (other == null) {
			return false;
		}

		if (!(other instanceof Minion)) {
			return false;
		}

		Minion rhs = (Minion) other;
		if (getId() == IdFactory.UNASSIGNED || ((Minion) other).getId() == IdFactory.UNASSIGNED) {
			return super.equals(other);
		}

		return this.getId() == rhs.getId();
	}

	@Override
	public Minion getCopy() {
		return (Minion) super.getCopy();
	}
}
