package net.demilich.metastone.game.spells.custom;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.cards.CardZone;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.SpellUtils;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.cards.Attribute;
import com.hiddenswitch.spellsource.rpc.Spellsource.ZonesMessage.Zones;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Transforms the {@code target} card into a card retrieved from {@link SpellUtils#getCards(GameContext, Player, Entity, Entity, SpellDesc)}, keeping important attributes like {@link
 * Attribute#STARTED_IN_DECK}, {@link Attribute#STARTED_IN_HAND} and {@link Attribute#REMOVES_SELF_AT_END_OF_TURN}
 * (ghostly) consistent. Typically used in conjunction with a {@link net.demilich.metastone.game.spells.desc.trigger.EnchantmentDesc#keepAfterTransform}
 * setting.
 */
public class TransformInHandSpell extends Spell {

	private static Logger logger = LoggerFactory.getLogger(TransformInHandSpell.class);

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		Card card = (Card) target;
		boolean startedInDeck = card.hasAttribute(Attribute.STARTED_IN_DECK);
		boolean startedInHand = card.hasAttribute(Attribute.STARTED_IN_HAND);
		boolean removesAtEndOfTurn = card.hasAttribute(Attribute.REMOVES_SELF_AT_END_OF_TURN);

		if (!(player.getZone(card.getZone()) instanceof CardZone)) {
			return;
		}

		Card newCard;
		EntityReference secondaryTarget = (EntityReference) desc.get(SpellArg.SECONDARY_TARGET);
		if (secondaryTarget != null) {
			newCard = context.resolveSingleTarget(player, source, secondaryTarget);
		} else {
			CardList retrievedOneCard = SpellUtils.getCards(context, player, target, source, desc);
			if (retrievedOneCard.size() == 0) {
				logger.warn("onCast {} {}: Tried to transform {} but no cards retrieved", context.getGameId(), source, target);
				return;
			}

			newCard = retrievedOneCard.get(0);
		}

		boolean keepsManaCostModifiers = (boolean) desc.getOrDefault(SpellArg.EXCLUSIVE, true);
		Card replaced = context.getLogic().replaceCard(player.getId(), card, newCard.getCopy(), keepsManaCostModifiers);

		// Cards that are transformed in the hand started in the deck if the originating card started in the deck
		// See https://www.reddit.com/r/hearthstone/comments/7ia60v/shifting_scroll_does_not_work_with_leyline/
		if (startedInDeck) {
			replaced.setAttribute(Attribute.STARTED_IN_DECK);
		}

		if (startedInHand) {
			replaced.setAttribute(Attribute.STARTED_IN_HAND);
		}

		if (removesAtEndOfTurn) {
			replaced.setAttribute(Attribute.REMOVES_SELF_AT_END_OF_TURN);
		}

		if (replaced.getZone() == Zones.HAND && desc.getSpell() != null) {
			SpellUtils.castChildSpell(context, player, desc.getSpell(), source, target, replaced);
		}
	}
}
