package net.demilich.metastone.game.spells.custom;

import com.hiddenswitch.spellsource.rpc.Spellsource.CardTypeMessage.CardType;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.BuffSpell;
import net.demilich.metastone.game.spells.desc.SpellDesc;

import java.util.Collections;
import java.util.List;

/**
 * Buffs all minions in the player's hand.
 * Implements effects like "Give all minions in your hand +X Attack."
 *
 * @see BuffLeftmostMinionInHandSpell for the single-target variant
 */
public final class BuffAllHandMinionsSpell extends BuffSpell {

	@Override
	public void cast(GameContext context, Player player, SpellDesc desc, Entity source, List<Entity> targets) {
		var minions = player.getHand().filtered(c -> c.getCardType() == CardType.MINION);
		for (Card card : minions) {
			super.cast(context, player, desc, source, Collections.singletonList(card));
		}
	}
}
