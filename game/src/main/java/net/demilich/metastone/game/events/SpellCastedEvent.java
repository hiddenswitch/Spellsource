package net.demilich.metastone.game.events;

import com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.HasCard;

/**
 * A spell was casted on the specified target.
 */
public class SpellCastedEvent extends CardEvent {

	public SpellCastedEvent(GameContext context, int playerId, Card sourceCard, Entity target) {
		super(EventTypeEnum.SPELL_CASTED, context, playerId, sourceCard, target, sourceCard);
	}
}
