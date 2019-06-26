package net.demilich.metastone.game.spells.custom;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.environment.Environment;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.SummonSpell;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;

/**
 *
 */
public class ManaCrystalToMinionSpell extends SummonSpell {
	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		int cardCost = source.getSourceCard().getBaseManaCost();
		int summonCount = Math.min(GameLogic.MAX_MINIONS - player.getMinions().size(), player.getMaxMana());
		// Summon minions per summoncount
		for (int i = 0; i < summonCount; i++) {
			SpellDesc summonSpell = new SpellDesc(SummonSpell.class);
			String CardId = "token_mana_treant";
			summonSpell.put(SpellArg.CARD, CardId);
			super.onCast(context, player, summonSpell, source, target);
		}
	}
}