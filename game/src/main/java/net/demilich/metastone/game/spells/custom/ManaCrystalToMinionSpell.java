package net.demilich.metastone.game.spells.custom;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.spells.SpellUtils;
import net.demilich.metastone.game.spells.SummonSpell;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;

/**
 * Summon minions based on player's mana crystal count; deduct one mana crystal per minion summoned.
 */
public class ManaCrystalToMinionSpell extends SummonSpell {
	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		int cardCost = source.getSourceCard().getBaseManaCost();
		int maxMana = player.getMaxMana();
		int summonCount = 0;
		// Summon minions per summoncount
		while (player.getMinions().size() < GameLogic.MAX_MINIONS && maxMana > 0) {
			SpellDesc summonSpell = new SpellDesc(SummonSpell.class);
			String cardId = (SpellUtils.getCard(context, desc)).getCardId();
			summonSpell.put(SpellArg.CARD, cardId);
			super.onCast(context, player, summonSpell, source, target);
			summonCount++;
			maxMana--;
		}
		player.setMaxMana(player.getMaxMana() - summonCount);
	}
}