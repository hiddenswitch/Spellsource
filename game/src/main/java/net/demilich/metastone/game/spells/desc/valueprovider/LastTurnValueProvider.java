package net.demilich.metastone.game.spells.desc.valueprovider;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.cards.Attribute;

/**
 * Returns the turn index of the {@link ValueProviderArg#TARGET_PLAYER}'s previous turn.
 * <p>
 * This value provider correctly accounts for extra turns.
 */
public class LastTurnValueProvider extends ValueProvider {

	public LastTurnValueProvider(ValueProviderDesc desc) {
		super(desc);
	}

	@Override
	protected int provideValue(GameContext context, Player player, Entity target, Entity host) {
		return player.getAttributeValue(Attribute.LAST_TURN);
	}
}
