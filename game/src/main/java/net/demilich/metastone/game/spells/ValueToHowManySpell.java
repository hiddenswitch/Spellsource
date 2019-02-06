package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;

/**
 * Decorates a {@link net.demilich.metastone.game.spells.desc.SpellArg#SPELL}, turning a {@link
 * net.demilich.metastone.game.spells.desc.SpellArg#VALUE} into a {@link net.demilich.metastone.game.spells.desc.SpellArg#HOW_MANY}
 * on the sub-spell.
 */
public final class ValueToHowManySpell extends Spell {

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		SpellDesc spell = desc.getSpell().clone();
		spell.put(SpellArg.HOW_MANY, desc.get(SpellArg.VALUE));
		SpellUtils.castChildSpell(context, player, spell, source, target);
	}
}
