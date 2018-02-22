package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.MinionCard;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.utils.Attribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Puts the target minion card into the {@link net.demilich.metastone.game.targeting.Zones#BATTLEFIELD} from the {@link
 * net.demilich.metastone.game.targeting.Zones#DECK}.
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
	 * @param minionCard The {@link MinionCard} to move from the deck to the battlefield.
	 * @return The spell
	 */
	public static SpellDesc create(EntityReference minionCard) {
		Map<SpellArg, Object> arguments = SpellDesc.build(PutMinionOnBoardFromDeckSpell.class);
		arguments.put(SpellArg.TARGET, minionCard);
		return new SpellDesc(arguments);
	}

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		if (target == null) {
			logger.error("onCast {} {}: The target {} is null", context.getGameId(), source, desc.getTarget());
			return;
		}
		if (!(target instanceof MinionCard)) {
			logger.error("onCast {} {}: The target {}  is not a MinionCard", context.getGameId(), source, target);
			return;
		}

		MinionCard minionCard = (MinionCard) target;
		if (!player.getDeck().contains(minionCard)) {
			logger.warn("onCast {} {}: The specified minion card {} was not present in {}'s deck. Exiting.", context.getGameId(), source, minionCard, player);
			return;
		}

		player.getDeck().move(minionCard, player.getSetAsideZone());

		boolean summonSuccess = context.getLogic().summon(player.getId(), minionCard.summon(), null, -1, false);

		player.getSetAsideZone().move(minionCard, player.getDeck());

		if (summonSuccess) {
			minionCard.getAttributes().put(Attribute.PLAYED_FROM_HAND_OR_DECK, context.getTurn());
			context.getLogic().removeCard(minionCard);
		}
	}

}
