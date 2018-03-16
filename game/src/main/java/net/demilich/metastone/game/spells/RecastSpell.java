package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;

public class RecastSpell extends Spell {

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		EntityReference secondaryTarget = (EntityReference) desc.get(SpellArg.SECONDARY_TARGET);
		Card card;
		if (secondaryTarget != null) {
			card = context.resolveSingleTarget(player, source, secondaryTarget);
		} else {
			card = SpellUtils.getCard(context, desc);
		}

		if (card == null) {
			return;
		}

		if (card.isSpell()) {
			SpellUtils.castChildSpell(context, player, card.getSpell().removeArg(SpellArg.FILTER), source, target);
		}
	}

}


