package net.demilich.metastone.game.spells.trigger.secrets;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.EntityType;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.trigger.EventTrigger;
import net.demilich.metastone.game.spells.trigger.Enchantment;

public class Quest extends Enchantment {
	private Card source;

	public Quest(EventTrigger trigger, SpellDesc spell, Card source, int countUntilCast) {
		super(trigger, spell);
		this.source = source;
		this.setCountUntilCast(countUntilCast);
	}

	@Override
	public Card getSourceCard() {
		return source;
	}

	@Override
	protected void onFire(int ownerId, SpellDesc spell, GameEvent event) {
		setCurrentCount(getCurrentCount() + 1);
		if (getCountUntilCast() == null || getCurrentCount() >= getCountUntilCast()) {
			super.onFire(ownerId, spell, event);
			Player owner = event.getGameContext().getPlayer(ownerId);
			event.getGameContext().getLogic().questTriggered(owner, this);
			expire();
		}
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
		clone.source = source;
		return clone;
	}
}
