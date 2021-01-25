package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.OpenerDesc;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.IdFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Gives the {@code target} card a {@link OpenerDesc} specified in {@link
 * net.demilich.metastone.game.spells.desc.SpellArg#BATTLECRY}.
 */
public class AddBattlecrySpell extends Spell {
	private static Logger LOGGER = LoggerFactory.getLogger(AddBattlecrySpell.class);

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		Card card = target.getSourceCard();
		if (card == null) {
			return;
		}
		if (card.getId() == IdFactory.UNASSIGNED) {
			LOGGER.error("onCast {} {}: Trying to mutate a catalogue card {} very dangerously", context.getGameId(), source, card.getCardId());
			return;
		}
		var openerDesc = (OpenerDesc) desc.get(SpellArg.BATTLECRY);
		context.getLogic().addEnchantment(player,source,source.getSourceCard(),target,openerDesc,true,true);
	}
}

