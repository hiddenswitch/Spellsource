package net.demilich.metastone.game.events;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.Card;

/**
 * A skill was used.
 */
public class HeroPowerUsedEvent extends CardEvent {

	public HeroPowerUsedEvent(GameContext context, int playerId, Card heroPower) {
		super(com.hiddenswitch.spellsource.rpc.Spellsource.GameEventTypeMessage.GameEventType.HERO_POWER_USED, true, context, playerId, -1, heroPower);
	}
}
