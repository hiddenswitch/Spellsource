package net.demilich.metastone.game.spells.desc.valueprovider;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.cards.Attribute;

public class SpellDamageValueProvider extends ValueProvider {

	public SpellDamageValueProvider(ValueProviderDesc desc) {
		super(desc);
	}

	@Override
	protected int provideValue(GameContext context, Player player, Entity target, Entity host) {
		int value = getDesc().getValue(ValueProviderArg.VALUE, context, player, target, host, 0);
		value = context.getLogic().applySpellpower(player, host, value);
		value = context.getLogic().applyAmplify(player, value, Attribute.SPELL_DAMAGE_AMPLIFY_MULTIPLIER);
		return value;
	}
}
