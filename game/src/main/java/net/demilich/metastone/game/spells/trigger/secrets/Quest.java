package net.demilich.metastone.game.spells.trigger.secrets;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.EntityType;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.trigger.TriggerDesc;
import net.demilich.metastone.game.spells.trigger.EventTrigger;
import net.demilich.metastone.game.spells.trigger.Enchantment;

public class Quest extends Enchantment {

	public Quest(EventTrigger trigger, SpellDesc spell, Card source, int countUntilCast) {
		super(trigger, spell);
		this.setSourceCard(source);
		this.setCountUntilCast(countUntilCast);
	}

	public Quest(TriggerDesc desc, Card source) {
		this(desc.eventTrigger.create(), desc.spell, source, desc.countUntilCast);
		setMaxFires(desc.maxFires);
		setKeepAfterTransform(desc.keepAfterTransform);
		setCountByValue(desc.countByValue);
		setPersistentOwner(desc.persistentOwner);
	}

	@Override
	protected boolean onFire(int ownerId, SpellDesc spell, GameEvent event) {
		final boolean spellFired = super.onFire(ownerId, spell, event);
		if (spellFired) {
			Player owner = event.getGameContext().getPlayer(ownerId);
			event.getGameContext().getLogic().questTriggered(owner, this);
			expire();
		}
		return spellFired;
	}

	@Override
	@Suspendable
	public void onGameEvent(GameEvent event) {
		super.onGameEvent(event);
	}

	@Override
	public EntityType getEntityType() {
		return EntityType.QUEST;
	}

	@Override
	public Quest clone() {
		Quest clone = (Quest) super.clone();
		clone.setSourceCard(getSourceCard());
		return clone;
	}
}
