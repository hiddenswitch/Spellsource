package net.demilich.metastone.game.spells.custom;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.desc.SpellDesc;

import java.util.List;

/**
 * Copies enchantments from the {@link net.demilich.metastone.game.spells.desc.SpellArg#SECONDARY_TARGET} to the {@code
 * target}.
 * <p>
 * Implements Doomlord Urc.
 * <p>
 * To copy enchantments from a card, use {@link CopyCardEnchantmentsSpell}. Note that spell reverses the interpretation
 * of {@code target} and {@link net.demilich.metastone.game.spells.desc.SpellArg#SECONDARY_TARGET}.
 */
public final class CopyEnchantmentsSpell extends Spell {

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		List<Entity> copyFrom = context.resolveTarget(player, source, desc.getSecondaryTarget());

		for (Entity originSource : copyFrom) {
			context.getLogic().copyEnchantments(player, source, originSource, target);
		}
	}
}
