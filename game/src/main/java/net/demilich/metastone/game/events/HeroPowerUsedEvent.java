package net.demilich.metastone.game.events;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;

/**
 * A skill was used.
 */
public class HeroPowerUsedEvent extends CardEvent {

	public HeroPowerUsedEvent(GameContext context, int playerId, Card heroPower) {
		super(com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum.HERO_POWER_USED, true, context, playerId, -1, heroPower);
	}
}
