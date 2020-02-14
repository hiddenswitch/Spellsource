package net.demilich.metastone.game.cards.costmodifier;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.GameEvent;
import com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum;;
import net.demilich.metastone.game.spells.desc.manamodifier.CardCostModifierDesc;
import net.demilich.metastone.game.spells.trigger.EventTrigger;
import net.demilich.metastone.game.spells.trigger.TurnStartTrigger;

/**
 * A card cost modifier that lasts only one turn.
 */
public final class OneTurnCostModifier extends CardCostModifier {

	private EventTrigger turnStartTrigger = new TurnStartTrigger();

	public OneTurnCostModifier(CardCostModifierDesc desc) {
		super(desc);
	}

	@Override
	public OneTurnCostModifier clone() {
		OneTurnCostModifier clone = (OneTurnCostModifier) super.clone();
		clone.turnStartTrigger = (EventTrigger) turnStartTrigger.clone();
		return clone;
	}

	@Override
	public boolean interestedIn(com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum eventType) {
		if (turnStartTrigger.interestedIn() == eventType) {
			return true;
		}

		return super.interestedIn(eventType);
	}

	@Override
	@Suspendable
	public void onGameEvent(GameEvent event) {
		Entity host = event.getGameContext().resolveSingleTarget(getHostReference());
		if (event.getEventType() == turnStartTrigger.interestedIn() && turnStartTrigger.queues(event, host)) {
			expire();
		}

		super.onGameEvent(event);
	}

	@Override
	public boolean oneTurnOnly() {
		return true;
	}

	@Override
	public void setOwner(int playerIndex) {
		super.setOwner(playerIndex);
		turnStartTrigger.setOwner(playerIndex);
	}

}
