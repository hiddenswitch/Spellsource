package net.demilich.metastone.game.events;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.Card;

/**
 * A card was invoked for {@link net.demilich.metastone.game.spells.desc.valueprovider.EventValueProvider} / {@link
 * #getValue()} mana.
 */
public class InvokedEvent extends CardEvent implements HasValue {
	private final int invokedMana;

	public InvokedEvent(GameContext context, int playerId, Card card, int invokedMana) {
		super(com.hiddenswitch.spellsource.rpc.Spellsource.GameEventTypeMessage.GameEventType.INVOKED, true, context, playerId, playerId, card);
		this.invokedMana = invokedMana;
	}

	@Override
	public int getValue() {
		return invokedMana;
	}
}

