package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;

/**
 * Calls {@link ChangeHeroPowerSpell} with {@link SpellArg#CARD} equal to the casting player's opponent's hero power
 * card ID.
 */
public class CopyHeroPower extends ChangeHeroPowerSpell {

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		Player opponent = context.getOpponent(player);
		String opponentHeroPowerId = opponent.getHeroPowerZone().get(0).getCardId();
		SpellDesc newDesc = new SpellDesc(ChangeHeroPowerSpell.class);
		newDesc.put(SpellArg.CARD, opponentHeroPowerId);
		super.onCast(context, player, newDesc, source, target);
	}

}
