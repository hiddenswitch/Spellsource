package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellDesc;

/**
 * Uses the {@link net.demilich.metastone.game.spells.desc.SpellArg#TARGET_PLAYER}'s hero power, choosing a target
 * randomly.
 */
public class UseHeroPowerSpell extends Spell {

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		Card card = player.getHeroPowerZone().get(0);
		SpellUtils.playCardRandomly(context, player, card, source, true, false, false, true, false);
	}

}
