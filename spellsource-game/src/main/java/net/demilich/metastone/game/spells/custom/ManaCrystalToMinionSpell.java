package net.demilich.metastone.game.spells.custom;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import com.hiddenswitch.spellsource.rpc.Spellsource.CardTypeMessage.CardType;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.spells.SpellUtils;
import net.demilich.metastone.game.spells.SummonSpell;
import net.demilich.metastone.game.spells.desc.SpellDesc;

/**
 * Summon minions based on player's mana crystal count; deduct one mana crystal per minion summoned.
 */
public class ManaCrystalToMinionSpell extends SummonSpell {
	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		int maxMana = player.getMaxMana();
		int summonCount = 0;
		// Summon minions per summoncount
		while (player.getMinions().size() < GameLogic.MAX_MINIONS && maxMana > 0) {
			Card card = SpellUtils.getCard(context, desc);
			if (card.getCardType() != CardType.MINION) {
				throw new UnsupportedOperationException("not a minion");
			}

			boolean summoned = context.getLogic().summon(player.getId(), card.minion(), source, -1, false);
			if (summoned) {
				summonCount++;
				maxMana--;
			} else {
				break;
			}
		}
		player.setMaxMana(player.getMaxMana() - summonCount);
	}
}