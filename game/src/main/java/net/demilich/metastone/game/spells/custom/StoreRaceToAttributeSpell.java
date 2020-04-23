package net.demilich.metastone.game.spells.custom;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.minions.Race;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;

import java.util.List;

/**
 * Stores the race of the {@code target} onto the {@code source} or {@link SpellArg#SECONDARY_TARGET} in the {@link
 * SpellArg#ATTRIBUTE}.
 *
 * @see java.util.Collections.SetFromMap
 */
public class StoreRaceToAttributeSpell extends Spell {

	@Suspendable
	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		String race = target.getRace();
		Attribute attribute = desc.getAttribute();
		Entity realTarget = target;
		if (desc.containsKey(SpellArg.SECONDARY_TARGET)) {
			List<Entity> entities = context.resolveTarget(player, source, desc.getSecondaryTarget());
			realTarget = entities.get(0);
		}
		realTarget.setAttribute(attribute, race);
	}
}
