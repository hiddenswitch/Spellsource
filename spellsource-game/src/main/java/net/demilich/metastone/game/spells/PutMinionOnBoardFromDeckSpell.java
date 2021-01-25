package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.cards.Attribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Puts the target minion card into the {@link com.hiddenswitch.spellsource.rpc.Spellsource.ZonesMessage.Zones#BATTLEFIELD} from the {@link
 * com.hiddenswitch.spellsource.rpc.Spellsource.ZonesMessage.Zones#DECK}.
 * <p>
 * The card must be located in the deck. Otherwise, this spell will fail.
 * <p>
 * This differs from a {@link SummonSpell} called on a {@link SpellArg#TARGET} of a {@link
 * net.demilich.metastone.game.cards.Card} because it will mark the card as {@link Attribute#PLAYED_FROM_HAND_OR_DECK},
 * a relevant distinction for some other card effects.
 * <p>
 * For <b>example</b>, to summon the minion with this effect out of your deck, use the following:
 * <pre>
 *     {
 *         "class": "PutMinionOnBoardFromDeckSpell",
 *         "target": "SELF"
 *     }
 * </pre>
 * <p>
 * Implements Patches the Pirate.
 */
public class PutMinionOnBoardFromDeckSpell extends Spell {

	private static Logger logger = LoggerFactory.getLogger(PutMinionOnBoardFromDeckSpell.class);

	/**
	 * Creates this spell for the specified minion card.
	 *
	 * @param card The {@link Card} to move from the deck to the battlefield.
	 * @return The spell
	 */
	public static SpellDesc create(EntityReference card) {
		Map<SpellArg, Object> arguments = new SpellDesc(PutMinionOnBoardFromDeckSpell.class);
		arguments.put(SpellArg.TARGET, card);
		return new SpellDesc(arguments);
	}

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		checkArguments(logger, context, source, desc);
		if (target == null) {
			logger.error("onCast {} {}: The target {} is null", context.getGameId(), source, desc.getTarget());
			return;
		}

		if (!(target instanceof Card)) {
			logger.error("onCast {} {}: The target {} is not a Card", context.getGameId(), source, target);
			return;
		}

		Card card = (Card) target;
		if (!player.getDeck().contains(card)) {
			logger.debug("onCast {} {}: The specified minion card {} was not present in {}'s deck. Exiting.", context.getGameId(), source, card, player);
			return;
		}

		player.getDeck().move(card, player.getSetAsideZone());

		final Minion summoned = card.minion();
		boolean summonSuccess = context.getLogic().summon(player.getId(), summoned, source, -1, false);

		player.getSetAsideZone().move(card, player.getDeck());

		if (summonSuccess) {
			card.getAttributes().put(Attribute.PLAYED_FROM_HAND_OR_DECK, context.getTurn());
			context.getLogic().removeCard(card);

			Entity newMinion = summoned.transformResolved(context);

			for (SpellDesc subSpell : desc.subSpells(0)) {
				if (newMinion.isInPlay()) {
					SpellUtils.castChildSpell(context, player, subSpell, source, target, newMinion);
				}
			}
		}
	}

}
