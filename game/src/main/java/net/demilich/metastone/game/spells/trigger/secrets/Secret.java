package net.demilich.metastone.game.spells.trigger.secrets;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.trigger.GameEventTrigger;
import net.demilich.metastone.game.spells.trigger.SpellTrigger;
import net.demilich.metastone.game.utils.AttributeMap;

public class Secret extends SpellTrigger {

	private Card source;

	public Secret(GameEventTrigger trigger, SpellDesc spell, Card source) {
		super(trigger, spell);
		this.source = source;
		setAttributes((AttributeMap) source.getAttributes().clone());
	}

	public Card getSecretCard() {
		return source;
	}

	@Override
	@Suspendable
	protected void onFire(int ownerId, SpellDesc spell, GameEvent event) {
		Player owner = event.getGameContext().getPlayer(ownerId);
		event.getGameContext().getLogic().secretTriggered(owner, this);
		super.onFire(ownerId, spell, event);
		expire();
	}

	@Override
	@Suspendable
	public void onGameEvent(GameEvent event) {
		if (event.getGameContext().getActivePlayerId() == getOwner()) {
			return;
		}
		super.onGameEvent(event);
	}

	@Override
	public String getName() {
		return getSecretCard().getName();
	}

	@Override
	public Secret clone() {
		Secret clone = (Secret) super.clone();
		clone.source = source;
		return clone;
	}
}
