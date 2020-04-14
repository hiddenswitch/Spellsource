package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.EntityZone;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swaps the card pointed to by {@link net.demilich.metastone.game.spells.desc.SpellArg#SECONDARY_TARGET} with the
 * {@code target} {@link net.demilich.metastone.game.cards.Card}.
 * <p>
 * For <b>example,</b> put this text on a card to swap it with one in your deck while the card is in your hand:
 * <pre>
 *     "passiveTrigger": {
 *          "eventTrigger": {
 *              "class": "TurnEndTrigger",
 *              "targetPlayer": "SELF"
 *          },
 *          "spell": {
 *              "class": "SwapCardsSpell",
 *              "secondaryTarget": "SELF",
 *              "target": "FRIENDLY_DECK",
 *              "randomTarget": "true"
 *          }
 *      }
 * </pre>
 * {@code passiveTrigger} indicates a {@link net.demilich.metastone.game.spells.trigger.Enchantment} that is active
 * while the host entity (in this case, a card) is in the player's hand.
 */
public class SwapCardsSpell extends Spell {

	private static Logger logger = LoggerFactory.getLogger(SwapCardsSpell.class);

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity spellSource, Entity spellTarget) {
		checkArguments(logger, context, spellSource, desc, SpellArg.SECONDARY_TARGET);
		Entity toSwap = context.resolveSingleTarget(player, spellSource, (EntityReference) desc.getOrDefault(SpellArg.SECONDARY_TARGET, EntityReference.SELF));

		if (!(toSwap instanceof Card)) {
			logger.error("onCast {} {}: Trying to swap {}, which is not a card", context.getGameId(), spellSource, toSwap);
			return;
		}

		if (!(spellTarget instanceof Card)) {
			logger.error("onCast {} {}: Trying to swap {} with the target {}, which is not a card.", context.getGameId(), spellSource, toSwap, spellTarget);
			return;
		}

		Card source = (Card) toSwap;
		Card target = (Card) spellTarget;

		swap(context, source, target);
	}

	/**
	 * Swaps two cards.
	 *
	 * @param context
	 * @param card1
	 * @param card2
	 */
	@Suspendable
	public static void swap(GameContext context, Card card1, Card card2) {
		EntityZone.swap(card1, card2, context);
		context.getLogic().removeEnchantments(card1);
		context.getLogic().removeEnchantments(card2);

		for (Entity entity : new Entity[]{card1, card2}) {
			switch (entity.getZone()) {
				case HAND:
					context.getLogic().processPassiveTriggers(context.getPlayer(entity.getOwner()), (Card) entity);
					context.getLogic().processPassiveAuras(context.getPlayer(entity.getOwner()), (Card) entity);
					break;
				case DECK:
					context.getLogic().processDeckTriggers(context.getPlayer(entity.getOwner()), (Card) entity);
					break;
				case BATTLEFIELD:
					context.getLogic().processBattlefieldEnchantments(context.getPlayer(entity.getOwner()), (Actor) entity);
					break;
			}
		}
	}
}
