package net.demilich.metastone.game.events;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.HasCard;

/**
 * Lun was overloaded (i.e. spent ahead of time).
 *
 * @see net.demilich.metastone.game.cards.Attribute#OVERLOAD
 */
public final class OverloadEvent extends ValueEvent {

	public OverloadEvent(GameContext context, int playerId, Card card, int manaCrystalsOverloaded) {
		super(com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum.OVERLOAD, context, playerId, playerId, card, manaCrystalsOverloaded);
	}
}
