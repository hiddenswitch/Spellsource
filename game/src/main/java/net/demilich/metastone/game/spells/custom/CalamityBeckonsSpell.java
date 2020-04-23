package net.demilich.metastone.game.spells.custom;

import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.strands.Strand;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import com.hiddenswitch.spellsource.client.models.CardType;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.desc.SpellDesc;

/**
 * Summons all the minions from the player's deck. Destroys the deck. Puts a corpse version of all summoned minions into
 * the graveyard.
 */
public final class CalamityBeckonsSpell extends Spell {

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		var max = player.getDeck().size();
		var i = 0;
		while (!player.getDeck().isEmpty() && i < max) {
			if (Strand.currentStrand().isInterrupted()) {
				break;
			}
			var card = context.getLogic().getRandom(player.getDeck());
			var previousLocation = card.getEntityLocation();
			if (card.getCardType() == CardType.MINION) {
				// summon and put a corpse in
				var minion = card.minion();
				var summoned = context.getLogic().summon(player.getId(), minion, source, -1, false);
				if (summoned) {
					minion = minion.getCopy();
				}
				// Move the corpse directly to the graveyard
				minion.setId(context.getLogic().generateId());
				minion.setOwner(player.getId());
				context.getLogic().corpse(minion, previousLocation, true);
			}
			context.getLogic().removeCard(card);
			i++;
		}
	}
}
