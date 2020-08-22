package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.ReturnToHandEvent;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.targeting.Zones;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.AttributeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Returns the {@code target} to the player's hand as a card.
 * <p>
 * After this effect, the card cannot be discarded by random discard effects for the rest of the sequence.
 * <p>
 * Actors removed this way are removed peacefully, i.e. their deathrattles are not triggered.
 * <p>
 * If the player's hand is full, the target minion or actor is destroyed and the deathrattle <b>is</b> triggered.
 * <p>
 * If the target has {@link Attribute#KEEPS_ENCHANTMENTS}, a limited number of buffs are kept. This is used primarily to
 * implement Kingsbane.
 * <p>
 * The {@link SpellArg#SPELL} subspell is cast with {@link EntityReference#OUTPUT} pointing to the returned card.
 * <p>
 * For <b>example</b>, to return a target to hand but make it cost (2) less:
 * <pre>
 *   {
 *     "class": "ReturnTargetToHandSpell",
 *     "spell": {
 *       "class": "CardCostModifierSpell",
 *       "target": "OUTPUT",
 *       "cardCostModifier": {
 *         "class": "CardCostModifier",
 *         "target": "SELF",
 *         "value": 2,
 *         "operation": "SUBTRACT"
 *       }
 *     }
 *   }
 * </pre>
 */
public class ReturnTargetToHandSpell extends Spell {

	private static Logger logger = LoggerFactory.getLogger(ReturnTargetToHandSpell.class);

	public static SpellDesc create() {
		return create(null, null, false);
	}

	public static SpellDesc create(EntityReference target, SpellDesc spell, boolean randomTarget) {
		Map<SpellArg, Object> arguments = new SpellDesc(ReturnTargetToHandSpell.class);
		arguments.put(SpellArg.SPELL, spell);
		arguments.put(SpellArg.TARGET, target);
		arguments.put(SpellArg.RANDOM_TARGET, randomTarget);
		return new SpellDesc(arguments);
	}

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		if (target == null) {
			logger.warn("onCast {} {}: Could not return null target.", context.getGameId(), source);
			return;
		}

		if (target.getZone() == Zones.HAND && !target.hasAttribute(Attribute.DISCARDED)) {
			logger.error("onCast {} {}: The target {} is already in the hand and we're not interrupting a discard.", context.getGameId(), source, target);
			return;
		}

		SpellDesc cardSpell = (SpellDesc) desc.get(SpellArg.SPELL);
		Player owner = context.getPlayer(target.getOwner());
		if (desc.containsKey(SpellArg.TARGET_PLAYER)) {
			owner = player;
		}
		if (owner.getHand().getCount() >= GameLogic.MAX_HAND_CARDS
				&& Actor.class.isAssignableFrom(target.getClass())) {
			logger.debug("onCast: {} is destroyed because {}'s hand is full", target, owner.getName());
			context.getLogic().markAsDestroyed((Actor) target, source);
		} else {
			logger.debug("onCast: {} is returned to {}'s hand", target, owner.getName());
			// The minion might be destroyed or already returned to hand due to Baron Rivendare at this point.
			// Doomerang may have returned Kingsbane
			AttributeMap map = SpellUtils.processKeptEnchantments(target, new AttributeMap());
			if (target.getZone() == Zones.BATTLEFIELD
					|| target.getZone() == Zones.WEAPON) {
				context.getLogic().removeActor((Actor) target, true);
			}
			// The source card may be in the graveyard.
			Card returnedCard;
			if (target instanceof Card) {
				returnedCard = (Card) target;
			} else {
				// Don't get a copy of the source card, get a copy of the BASE card.
				returnedCard = context.getCardById(target.getSourceCard().getCardId());
			}

			// Transferred enchantments
			returnedCard.getAttributes().putAll(map);
			// Prevents cards from being discarded
			if (returnedCard.getZone() == Zones.HAND
					&& returnedCard.hasAttribute(Attribute.DISCARDED)) {
				returnedCard.getAttributes().remove(Attribute.DISCARDED);
			} else {
				context.getLogic().receiveCard(owner.getId(), returnedCard);
			}
			if (cardSpell != null && returnedCard.getZone() == Zones.HAND) {
				SpellUtils.castChildSpell(context, player, cardSpell, source, target, returnedCard);
			}
			// It must still be in the hand to be a returned to hand effect
			if (returnedCard.getZone() == Zones.HAND) {
				context.getLogic().fireGameEvent(new ReturnToHandEvent(context, player.getId(), returnedCard, target));
			}
		}
	}

}

