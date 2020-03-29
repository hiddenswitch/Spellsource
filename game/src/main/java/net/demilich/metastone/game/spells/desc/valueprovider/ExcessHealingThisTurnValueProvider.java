package net.demilich.metastone.game.spells.desc.valueprovider;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.entities.Entity;

/**
 * Returns the amount of excess healing the {@link ValueProviderArg#TARGET_PLAYER} has given to its entities.
 */
public class ExcessHealingThisTurnValueProvider extends ValueProvider {

	public ExcessHealingThisTurnValueProvider(ValueProviderDesc desc) {
		super(desc);
	}

	@Override
	protected int provideValue(GameContext context, Player player, Entity target, Entity host) {
		return player.getAttributeValue(Attribute.EXCESS_HEALING_THIS_TURN, 0);
	}
}
