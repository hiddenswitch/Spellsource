package net.demilich.metastone.game.spells.custom;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.DuelSpell;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;

import java.util.Collections;
import java.util.List;

/**
 * Like {@link DuelSpell}, except a random attacker that is not a defender is chosen from the {@link
 * net.demilich.metastone.game.spells.desc.SpellArg#SECONDARY_TARGET}. <b>Ignores its filter.</b>
 * <p>
 * Implements One on One.
 */
public final class DuelRandomSecondarySpell extends DuelSpell {

	@Override
	protected List<Entity> getDefenders(GameContext context, Player player, Entity source, List<Entity> targets, EntityFilter filter) {
		return super.getDefenders(context, player, source, targets, null);
	}

	@Override
	protected List<Entity> getAttackers(GameContext context, Player player, SpellDesc desc, Entity source, EntityFilter filter) {
		return Collections.singletonList(context.getLogic().getRandom(super.getAttackers(context, player, desc, source, null)));
	}
}
