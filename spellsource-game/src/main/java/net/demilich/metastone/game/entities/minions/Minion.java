package net.demilich.metastone.game.entities.minions;

import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Actor;
import com.hiddenswitch.spellsource.rpc.Spellsource.EntityTypeMessage.EntityType;
import net.demilich.metastone.game.targeting.IdFactory;
import net.demilich.metastone.game.cards.Attribute;
import com.hiddenswitch.spellsource.rpc.Spellsource.ZonesMessage.Zones;

public final class Minion extends Actor {

	private static final Zones[] BATTLEFIELD_ZONE = new Zones[]{Zones.BATTLEFIELD};

	public Minion(Card sourceCard) {
		super();
		String race = sourceCard.hasAttribute(Attribute.RACE) ? (String) sourceCard.getAttribute(Attribute.RACE) : Race.NONE;
		setRace(race);
		setSourceCard(sourceCard);
	}

	@Override
	public Minion clone() {
		return (Minion) super.clone();
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

	@Override
	public Zones[] getDefaultActiveTriggerZones() {
		return BATTLEFIELD_ZONE;
	}
}
