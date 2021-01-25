package net.demilich.metastone.game.events;

import com.hiddenswitch.spellsource.rpc.Spellsource.GameEventTypeMessage.GameEventType;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.HasCard;

/**
 * A spell has been casted with {@code source} card and, if the user selected the target, the {@code target}.
 */
public class AfterSpellCastedEvent extends CardEvent implements HasCard {

	public AfterSpellCastedEvent(GameContext context, int playerId, Card sourceCard, Entity target) {
		super(GameEventType.AFTER_SPELL_CASTED, context, playerId, sourceCard, target, sourceCard);
	}
}
