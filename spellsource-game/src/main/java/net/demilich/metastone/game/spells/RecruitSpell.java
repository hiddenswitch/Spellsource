package net.demilich.metastone.game.spells;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import com.hiddenswitch.spellsource.rpc.Spellsource.CardTypeMessage.CardType;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.filter.AndFilter;
import net.demilich.metastone.game.spells.desc.filter.CardFilter;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import net.demilich.metastone.game.spells.desc.source.CardSource;
import net.demilich.metastone.game.spells.desc.source.DeckSource;
import net.demilich.metastone.game.spells.desc.source.HandSource;
import net.demilich.metastone.game.spells.desc.source.HasCardCreationSideEffects;
import com.hiddenswitch.spellsource.rpc.Spellsource.ZonesMessage.Zones;

import java.util.List;

/**
 * Recruits (summons and removes the source card of) {@link SpellArg#VALUE} minions from {@link SpellArg#CARD_LOCATION}
 * location.
 * <p>
 * If a {@link SpellArg#SPELL} is specified, it is cast on the <b>minion</b> after it is summoned.
 * <p>
 * The card is moved to the {@link Zones#SET_ASIDE_ZONE} before the minion printed on it is summoned. This prevents
 * enchantments from being cleared.
 * <p>
 * Playing cards this way does not count as playing them from the hand or deck.
 * <p>
 * For <b>example,</b> to do the text "Recruit 2 minions that cost (4) or less:"
 * <pre>
 *   {
 *     "class": "RecruitSpell",
 *     "value": 2,
 *     "cardFilter": {
 *       "class": "AndFilter",
 *       "filters": [
 *         {
 *           "class": "CardFilter",
 *           "cardType": "MINION"
 *         },
 *         {
 *           "class": "ManaCostFilter",
 *           "value": 4,
 *           "operation": "LESS_OR_EQUAL"
 *         }
 *       ]
 *     },
 *     "cardLocation": "DECK",
 *     "targetPlayer": "SELF"
 *   }
 * </pre>
 * For the text "Recruit two 1-Cost minions," observe that the card means base mana cost. The only change is the
 * filter:
 * <pre>
 *   {
 *     "class": "RecruitSpell",
 *     "value": 2,
 *     "cardFilter": {
 *       "class": "CardFilter",
 *       "cardType": "MINION",
 *       "manaCost": 1
 *     },
 *     "cardLocation": "DECK",
 *     "targetPlayer": "SELF"
 *   }
 * </pre>
 * <p>
 * To recruit a specific target from the hand, use {@link PutMinionOnBoardSpell}. If you need to only put the minion
 * onto the board if it is in the deck, use {@link PutMinionOnBoardFromDeckSpell}.
 */
public class RecruitSpell extends Spell {

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		int numberToSummon = desc.getValue(SpellArg.VALUE, context, player, target, source, 1);
		List<SpellDesc> subSpells = desc.subSpells(0);
		for (int i = 0; i < numberToSummon; i++) {
			Minion minion = putMinionOntoBoard(context, player, source, desc);
			if (minion != null) {
				for (SpellDesc subSpell : subSpells) {
					SpellUtils.castChildSpell(context, player, subSpell, source, target, minion);
				}
			}
		}
	}

	private Minion putMinionOntoBoard(GameContext context, Player player, Entity source, SpellDesc desc) {
		Card card;
		CardSource cardSource;
		EntityFilter cardFilter;

		if (desc.getCardSource() == null) {
			Zones cardLocation = (Zones) desc.get(SpellArg.CARD_LOCATION);
			if (cardLocation == null || cardLocation == Zones.DECK) {
				cardSource = DeckSource.create();
			} else {
				cardSource = HandSource.create();
			}
		} else {
			cardSource = desc.getCardSource();
		}

		if (cardSource instanceof HasCardCreationSideEffects) {
			throw new UnsupportedOperationException("Cannot recruit from sources that have card creation side effects.");
		}

		if (desc.getCardFilter() == null) {
			cardFilter = CardFilter.create(CardType.MINION);
		} else {
			cardFilter = AndFilter.create(CardFilter.create(CardType.MINION), desc.getCardFilter());
		}

		card = context.getLogic().getRandom(cardSource.getCards(context, source, player).filtered(cardFilter.matcher(context, player, source)));

		if (card == null) {
			return null;
		}

		// we need to remove the card temporarily here, because there are card interactions like Starving Buzzard + Desert Camel
		// which could result in the card being drawn while a minion is summoned
		Zones originalZone = card.getZone();
		if (originalZone == Zones.DECK) {
			player.getDeck().move(card, player.getSetAsideZone());
		}

		Minion summon = card.minion();
		boolean summonSuccess = context.getLogic().summon(player.getId(), summon, source, -1, false);

		// re-add the card here if we removed it before
		if (originalZone == Zones.DECK) {
			player.getSetAsideZone().move(card, player.getDeck());
		}

		// put the card into the graveyard when we're done with it
		if (summonSuccess) {
			context.getLogic().removeCard(card);
			return summon;
		}

		return null;
	}
}
