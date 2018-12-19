package net.demilich.metastone.game.spells.custom;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.AddDeathrattleSpell;
import net.demilich.metastone.game.spells.SummonSpell;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;

/**
 * Gives a minion or card, "Deathrattle: Resummon this minion." Uses the card ID to do the resummoning.
 */
public final class ElorthaNoShandraSpell extends AddDeathrattleSpell {

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		SpellDesc deathrattle = SummonSpell.create(target.getSourceCard().getCardId());
		desc = desc.clone();
		desc.put(SpellArg.SPELL, deathrattle);
		super.onCast(context, player, desc, source, target);
	}
}
