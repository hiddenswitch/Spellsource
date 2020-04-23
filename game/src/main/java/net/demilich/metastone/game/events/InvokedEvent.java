package net.demilich.metastone.game.events;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.HasCard;

/**
 * A card was invoked for {@link net.demilich.metastone.game.spells.desc.valueprovider.EventValueProvider} / {@link
 * #getValue()} mana.
 */
public class InvokedEvent extends CardEvent implements HasValue {
	private final int invokedMana;

	public InvokedEvent(GameContext context, int playerId, Card card, int invokedMana) {
		super(com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum.INVOKED, true, context, playerId, playerId, card);
		this.invokedMana = invokedMana;
	}

	@Override
	public int getValue() {
		return invokedMana;
	}
}

