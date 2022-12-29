package net.demilich.metastone.game.spells;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import com.hiddenswitch.spellsource.rpc.Spellsource.ZonesMessage.Zones;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Steals the {@code target} card and moves it to the caster's zone from {@link SpellArg#CARD_LOCATION}. Keeps card cost
 * modifiers.
 * <p>
 * For example, to steal a card from the opponent's deck and put it into the caster's deck:
 * <pre>
 *   {
 *     "class": "StealCardSpell",
 *     "target": "ENEMY_DECK",
 *     "randomTarget": true,
 *     "cardLocation": "DECK"
 *   }
 * </pre>
 */
public class StealCardSpell extends Spell {
	private static Logger logger = LoggerFactory.getLogger(StealCardSpell.class);

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		if (!(target instanceof Card)) {
			logger.error("onCast {} {}: StealCardSpell called on non-Card {}.", context.getGameId(), source, target);
			return;
		}

		context.getLogic().stealCard(player, source, (Card) target, (Zones) desc.get(SpellArg.CARD_LOCATION));
	}
}
