package net.demilich.metastone.game.spells;

import com.github.fromage.quasi.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.Zones;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StealCardSpell extends Spell {
	private static Logger logger = LoggerFactory.getLogger(StealCardSpell.class);

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		if (!(target instanceof Card)) {
			logger.error("onCast {} {}: StealCardSpell called on non-Card {}.", context.getGameId(), source, target);
			return;
		}

		context.getLogic().stealCard(player, source, (Card) target, (Zones) desc.get(SpellArg.CARD_LOCATION));
	}
}
