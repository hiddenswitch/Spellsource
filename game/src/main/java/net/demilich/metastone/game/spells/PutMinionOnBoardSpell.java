package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.MinionCard;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.utils.Attribute;

public class PutMinionOnBoardSpell extends Spell {
	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		MinionCard minionCard = (MinionCard) target;

		if (context.getLogic().summon(player.getId(), minionCard.summon(), null, -1, false)) {
			minionCard.getAttributes().put(Attribute.PLAYED_FROM_HAND_OR_DECK, context.getTurn());
			context.getLogic().removeCard(minionCard);
		}
	}
}
