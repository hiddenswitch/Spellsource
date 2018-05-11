package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.EntityLocation;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.targeting.IdFactory;
import net.demilich.metastone.game.targeting.Zones;

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

		if (card.getId() == IdFactory.UNASSIGNED
				&& card.getEntityLocation().equals(EntityLocation.UNASSIGNED)) {
			card = card.getCopy();
			card.setId(context.getLogic().generateId());
			card.setOwner(player.getId());
			player.getSetAsideZone().add(card);
		}

		if (card.isSpell()) {
			SpellUtils.castChildSpell(context, player, card.getSpell().removeArg(SpellArg.FILTER), card, target);
		}

		if (card.getZone().equals(Zones.SET_ASIDE_ZONE)) {
			card.moveOrAddTo(context, Zones.REMOVED_FROM_PLAY);
		}
	}

}


