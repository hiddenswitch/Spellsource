package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Replaces the {@code target} with one of the cards from {@link SpellUtils#getCards(GameContext, Player, Entity,
 * Entity, SpellDesc, int)}.
 * <p>
 * Replacing cards in this way keeps enchantments marked as {@link net.demilich.metastone.game.spells.desc.trigger.EnchantmentDesc#keepAfterTransform}.
 * <p>
 * Casts the {@link net.demilich.metastone.game.spells.desc.SpellArg#SPELL} with the {@link
 * net.demilich.metastone.game.targeting.EntityReference#OUTPUT} pointing to the replacement.
 * <p>
 * For <b>example,</b> this implements the text, "Transform all 1-Cost cards in your deck into Legendary minions:"
 * <pre>
 *   {
 *      "class": "ReplaceCardsSpell",
 *      "target": "FRIENDLY_DECK",
 *      "cardFilter": {
 *        "class": "CardFilter",
 *        "rarity": "LEGENDARY",
 *        "cardType": "MINION"
 *      },
 *      "filter": {
 *        "class": "CardFilter",
 *        "manaCost": 1
 *      }
 *    }
 * </pre>
 */
public class ReplaceCardsSpell extends Spell {

	private static Logger logger = LoggerFactory.getLogger(ReplaceCardsSpell.class);

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		CardList cards = SpellUtils.getCards(context, player, target, source, desc, 1);
		if (cards.isEmpty()) {
			logger.warn("onCast {} {}: No cards found to replace with.", source, context.getGameId());
			return;
		}
		Card replacement = cards.get(0).getCopy();

		replacement = context.getLogic().replaceCard(target.getOwner(), (Card) target, replacement);
		final Card output = replacement;
		for (SpellDesc subSpell : desc.subSpells(0)) {
			SpellUtils.castChildSpell(context, player, subSpell, source, target, output);
		}
	}
}
