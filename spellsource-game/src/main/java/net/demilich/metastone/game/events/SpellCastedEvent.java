package net.demilich.metastone.game.events;

import com.hiddenswitch.spellsource.rpc.Spellsource.GameEventTypeMessage.GameEventType;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;

/**
 * A spell was casted on the specified target.
 */
public class SpellCastedEvent extends CardEvent {

	public SpellCastedEvent(GameContext context, int playerId, Card sourceCard, Entity target) {
		super(GameEventType.SPELL_CASTED, context, playerId, sourceCard, target, sourceCard);
	}
}
