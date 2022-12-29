package net.demilich.metastone.game.spells.custom;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.DrainSpell;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.SpellUtils;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;

import java.util.List;

/**
 * Drains {@link SpellArg#VALUE} health for each {@code target} resolved from the perspective of each {@link
 * SpellArg#SECONDARY_TARGET}.
 */
public final class FinalFeastSpell extends Spell {

	@Override
	public void cast(GameContext context, Player player, SpellDesc desc, Entity source, List<Entity> targets) {
		for (Entity drainSource : targets) {
			SpellDesc drain = new SpellDesc(DrainSpell.class);
			drain.put(SpellArg.VALUE, desc.get(SpellArg.VALUE));
			drain.put(SpellArg.TARGET, desc.getSecondaryTarget());
			SpellUtils.castChildSpell(context, player, drain, drainSource, null);
		}
	}

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		throw new UnsupportedOperationException("should not be called");
	}
}
