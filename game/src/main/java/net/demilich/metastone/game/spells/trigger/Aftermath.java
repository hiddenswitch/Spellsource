package net.demilich.metastone.game.spells.trigger;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;
import net.demilich.metastone.game.targeting.Zones;

public final class Aftermath extends Enchantment {
	private static final EventTriggerDesc[] NO_TRIGGERS = new EventTriggerDesc[0];
	private static final Zones[] ACTIVE_ZONES = new Zones[]{Zones.GRAVEYARD};
	private final SpellDesc aftermath;

	@Override
	public Zones[] getZones() {
		return ACTIVE_ZONES;
	}

	public Aftermath(SpellDesc spellDesc, Card sourceCard, Entity host) {
		super();
		setHostReference(host.getReference());
		setOwner(host.getOwner());
		setSourceCard(sourceCard);
		this.aftermath = spellDesc;
	}

	@Override
	public SpellDesc getSpell() {
		return aftermath;
	}

	@Override
	protected EventTriggerDesc[] getDefaultTriggers() {
		return NO_TRIGGERS;
	}

	@Override
	public Aftermath clone() {
		return (Aftermath) super.clone();
	}

	@Override
	@Suspendable
	protected void cast(int ownerId, SpellDesc spell, GameEvent event) {
		// Aftermaths do not cast
	}

	@Override
	public boolean isActivated() {
		return true;
	}

	@Override
	public boolean isCopyToActor() {
		return true;
	}
}

