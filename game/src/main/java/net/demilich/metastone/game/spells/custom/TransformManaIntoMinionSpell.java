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
public class TransformManaIntoMinionSpell extends SummonSpell {
	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		int cardCost = source.getSourceCard().getBaseManaCost();
		// compare 7 - player.get.Minions().size to player.getMana() + cardCost, take minimum, store it as int summoncount
		int summonCount = Math.min(GameLogic.MAX_MINIONS - player.getMinions().size(), player.getMana() + cardCost);
		// reduced card cost does not change extraCost, because the variable cardCost here reflects how many minions will be summoned as the base count
		// For example, when the card which originally costs 5 mana and summons at least 5 minions now cost 4 mana, if we are to summon 7 minions by the card, the extra cost remains 2. Only the total cost varies.
		int extraCost = summonCount - cardCost;
		// deduct extracost from player.getMana()
		int finalMana = player.getMana() - extraCost;
		// Summon minions per summoncount
		for (int i = 0; i < summonCount; i++) {
			SpellDesc summonSpell = new SpellDesc(SummonSpell.class);
			String CardId = "token_mana_treant";
			summonSpell.put(SpellArg.CARD, CardId);
			super.onCast(context, player, summonSpell, source, target);
		}
		// Correct player's mana count
		player.setMana(finalMana);
	}
}