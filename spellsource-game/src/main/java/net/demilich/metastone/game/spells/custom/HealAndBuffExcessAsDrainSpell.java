package net.demilich.metastone.game.spells.custom;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.DrainSpell;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.valueprovider.AttributeValueProvider;

/**
 * Gets the {@link Attribute#LAST_HIT} last amount of damage dealt to the {@code target}, and restores health in that
 * amount to the {@link SpellArg#SECONDARY_TARGET}. Any excess health restoration is converted to a hitpoint buff
 * instead.
 * <p>
 * Implements Blood Moon Rising.
 */
public final class HealAndBuffExcessAsDrainSpell extends Spell {

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		int amount = AttributeValueProvider.create(Attribute.LAST_HIT, target.getReference()).create().getValue(context, player, target, source);
		DrainSpell.drain(context, player, source, amount, context.resolveSingleTarget(player, source, desc.getSecondaryTarget()));
	}
}
