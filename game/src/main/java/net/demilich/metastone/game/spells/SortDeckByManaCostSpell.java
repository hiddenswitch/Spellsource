package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellDesc;

import java.util.Comparator;

/**
 * Sorts a deck by mana costs, putting the lowest cost cards on top.
 */
public final class SortDeckByManaCostSpell extends Spell {

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		player.getDeck().sort(Comparator.comparing((Card c) -> context.getLogic().getModifiedManaCost(player, c)).reversed());
	}
}
