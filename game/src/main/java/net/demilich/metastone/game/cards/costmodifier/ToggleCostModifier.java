package net.demilich.metastone.game.cards.costmodifier;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.GameEvent;
;
import net.demilich.metastone.game.spells.desc.manamodifier.CardCostModifierArg;
import net.demilich.metastone.game.spells.desc.manamodifier.CardCostModifierDesc;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;
import net.demilich.metastone.game.spells.trigger.EventTrigger;

/**
 * A card cost modifier that toggles on and off as {@link CardCostModifierArg#TOGGLE_ON_TRIGGER} and {@link
 * CardCostModifierArg#TOGGLE_OFF_TRIGGER} triggers fire.
 */
public final class ToggleCostModifier extends CardCostModifier {

	private EventTrigger toggleOnTrigger;
	private EventTrigger toggleOffTrigger;
	private boolean ready;

	public ToggleCostModifier(CardCostModifierDesc desc) {
		super(desc);
		EventTriggerDesc triggerDesc = (EventTriggerDesc) desc.get(CardCostModifierArg.TOGGLE_ON_TRIGGER);
		this.toggleOnTrigger = triggerDesc.create();

		triggerDesc = (EventTriggerDesc) desc.get(CardCostModifierArg.TOGGLE_OFF_TRIGGER);
		this.toggleOffTrigger = triggerDesc.create();
	}

	@Override
	public boolean appliesTo(GameContext context, Card card, Player player) {
		if (!ready) {
			return false;
		}
		return super.appliesTo(context, card, player);
	}

	@Override
	public CardCostModifier clone() {
		ToggleCostModifier clone = (ToggleCostModifier) super.clone();
		clone.toggleOnTrigger = toggleOnTrigger.clone();
		clone.toggleOffTrigger = toggleOffTrigger.clone();
		return clone;
	}

	@Override
	public boolean interestedIn(com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum eventType) {
		return eventType == toggleOnTrigger.interestedIn() || eventType == toggleOffTrigger.interestedIn();
	}

	@Override
	@Suspendable
	public void onGameEvent(GameEvent event) {
		Entity host = event.getGameContext().resolveSingleTarget(getHostReference());
		var playerId = host.getOwner();
		if (isPersistentOwner()) {
			playerId = getOwner();
		}
		if (toggleOnTrigger.interestedIn() == event.getEventType() && toggleOnTrigger.queues(event, this, host, playerId)) {
			ready = true;
		} else if (toggleOffTrigger.interestedIn() == event.getEventType() && toggleOffTrigger.queues(event, this, host, playerId)) {
			ready = false;
		}
	}
}
