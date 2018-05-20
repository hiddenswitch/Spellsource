package net.demilich.metastone.game.spells;

import java.util.Map;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.EntityLocation;
import net.demilich.metastone.game.targeting.Zones;
import net.demilich.metastone.game.utils.Attribute;
import net.demilich.metastone.game.utils.AttributeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;

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
			logger.warn("onCast: Could not return null target.");
			return;
		}

		SpellDesc cardSpell = (SpellDesc) desc.get(SpellArg.SPELL);
		Player owner = context.getPlayer(target.getOwner());
		if (owner.getHand().getCount() >= GameLogic.MAX_HAND_CARDS
				&& Actor.class.isAssignableFrom(target.getClass())) {
			logger.debug("onCast: {} is destroyed because {}'s hand is full", target, owner.getName());
			context.getLogic().markAsDestroyed((Actor) target);
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
				context.getLogic().receiveCard(target.getOwner(), returnedCard);
			}
			if (cardSpell != null) {
				SpellUtils.castChildSpell(context, player, cardSpell, source, target, returnedCard);
			}
		}
	}

}

