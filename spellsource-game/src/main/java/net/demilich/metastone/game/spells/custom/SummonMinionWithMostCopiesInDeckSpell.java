package net.demilich.metastone.game.spells.custom;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import com.hiddenswitch.spellsource.rpc.Spellsource.CardTypeMessage.CardType;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.SummonSpell;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Summons a minion from the player's deck with the most copies in the deck. If there are multiple minions with the most
 * copies, summon one at random.
 */
public class SummonMinionWithMostCopiesInDeckSpell extends SummonSpell {
	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		Map<String, Integer> countOfCard = new HashMap<>();
		for (int i = 0; i < player.getDeck().size(); i++) {
			Card card = player.getDeck().get(i);
			if (card.getCardType() != CardType.MINION) {
				continue;
			}

			int newCount = countOfCard.getOrDefault(card.getCardId(), 1);
			countOfCard.put(card.getCardId(), newCount);
		}
		// Find the highest count card
		int maxCount = Integer.MIN_VALUE;
		List<String> maxCardIds = new ArrayList<>();
		for (String cardId : countOfCard.keySet()) {
			int count = countOfCard.get(cardId);
			if (count > maxCount) {
				maxCount = count;
				maxCardIds.clear();
				maxCardIds.add(cardId);
			} else if (count == maxCount) {
				maxCardIds.add(cardId);
			}
		}

		if (maxCardIds.isEmpty()) {
			return;
		}

		SpellDesc summonSpell = new SpellDesc(SummonSpell.class);
		String randomCardId = context.getLogic().removeRandom(maxCardIds);
		summonSpell.put(SpellArg.CARD, randomCardId);
		super.onCast(context, player, summonSpell, source, target);
	}
}
