package net.demilich.metastone.game.spells;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import net.demilich.metastone.game.targeting.EntityReference;

/**
 * Removes a {@code target} actor peacefully (without triggering its deathrattles) from its current zone.
 * <p>
 * If a {@link SpellArg#SPELL} is specified, it is cast with the {@code target} as {@link EntityReference#OUTPUT}
 * <b>before</b> it is removed from the zone it is currently in.
 *
 * Does nothing if the target is destroyed or if the target is not in play.
 */
public final class RemoveActorPeacefullySpell extends Spell {

	public static SpellDesc create(EntityReference target, EntityFilter filter, boolean randomTarget) {
		return new SpellDesc(RemoveActorPeacefullySpell.class, target, filter, randomTarget);
	}

	public static SpellDesc create(EntityReference target, EntityFilter filter, boolean randomTarget, SpellDesc beforeRemoved) {
		SpellDesc desc =  new SpellDesc(RemoveActorPeacefullySpell.class, target, filter, randomTarget);
		desc.put(SpellArg.SPELL, beforeRemoved);
		return desc;
	}


	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		if (!target.isDestroyed() && target.isInPlay()) {
			if (desc.getSpell() != null) {
				SpellUtils.castChildSpell(context, player, desc.getSpell(), source, target, target);
			}
			context.getLogic().removeActor((Actor) target, true);
		}
	}
}
