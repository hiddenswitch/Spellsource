package net.demilich.metastone.game.spells.custom;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.CardList;
import com.hiddenswitch.spellsource.client.models.CardType;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.BuffSpell;
import net.demilich.metastone.game.spells.desc.SpellDesc;

import java.util.Collections;
import java.util.List;

/**
 * Buffs the leftmost minion in the player's hand.
 * <p>
 * Implements Soul Infusion.
 */
public final class BuffLeftmostMinionInHandSpell extends BuffSpell {

	@Override
	@Suspendable
	public void cast(GameContext context, Player player, SpellDesc desc, Entity source, List<Entity> targets) {
		CardList minions = player.getHand().filtered(c -> c.getCardType() == CardType.MINION);
		if (minions.isEmpty()) {
			return;
		}
		super.cast(context, player, desc, source, Collections.singletonList(minions.get(0)));
	}
}

