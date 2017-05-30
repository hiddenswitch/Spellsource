package net.demilich.metastone.game.entities.minions;

import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.cards.MinionCard;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.EntityLocation;
import net.demilich.metastone.game.entities.EntityType;
import net.demilich.metastone.game.targeting.IdFactory;

public class Minion extends Actor {

	public Minion(MinionCard sourceCard) {
		super(sourceCard);
		Race race = sourceCard.hasAttribute(Attribute.RACE) ? (Race) sourceCard.getAttribute(Attribute.RACE) : Race.NONE;
		setRace(race);
	}

	@Override
	public Minion clone() {
		Minion clone = (Minion) super.clone();
		return clone;
	}

	@Override
	public int getAttack() {
		if (hasAttribute(Attribute.ATTACK_EQUALS_HP)) {
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

	public Minion getCopy() {
		Minion clone = this.clone();
		clone.setEntityLocation(EntityLocation.UNASSIGNED);
		clone.setId(IdFactory.UNASSIGNED);
		clone.setOwner(IdFactory.UNASSIGNED);
		return clone;
	}
}
