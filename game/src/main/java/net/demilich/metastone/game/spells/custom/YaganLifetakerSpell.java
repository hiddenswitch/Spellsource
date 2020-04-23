package net.demilich.metastone.game.spells.custom;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.DrainEvent;
import net.demilich.metastone.game.spells.DrainSpell;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;

import java.util.ArrayList;
import java.util.List;

/**
 * Drains damage from the {@code target} and randomly buffs among the {@link SpellArg#SECONDARY_TARGET} entities.
 */
public final class YaganLifetakerSpell extends Spell {

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		List<Entity> drainTo = context.resolveTarget(player, source, desc.getSecondaryTarget());
		if (drainTo.isEmpty()) {
			return;
		}
		List<DrainEvent> events = new ArrayList<>(1);
		int damageDealt = DrainSpell.drainDamage(context, player, source, target, desc.getValue(SpellArg.VALUE, context, player, target, source, 0), events);
		for (int i = 0; i < damageDealt; i++) {
			drainTo.removeIf(e -> !e.isInPlay());
			if (drainTo.isEmpty()) {
				return;
			}
			DrainSpell.drain(context, player, source, 1, context.getLogic().getRandom(drainTo));
		}
		for (DrainEvent event : events) {
			context.getLogic().fireGameEvent(event);
		}
	}
}
