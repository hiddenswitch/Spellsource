package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.cards.CardArrayList;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import net.demilich.metastone.game.spells.desc.source.CardSource;
import net.demilich.metastone.game.spells.desc.source.CatalogueSource;
import net.demilich.metastone.game.spells.desc.source.HasCardCreationSideEffects;
import net.demilich.metastone.game.targeting.EntityReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Puts a card in your hand.
 * <p>
 * When a {@link SpellArg#CARD} is specified as the only argument, this spell creates that card and puts it in the
 * player's hand.
 * <p>
 * When {@link SpellArg#CARDS} is specified, this spell will put every card in that array into the player's hand. To
 * choose one of those cards at random, set {@link SpellArg#RANDOM_TARGET} to {@code true}.
 * <p>
 * When a {@link SpellArg#CARD_FILTER} and/or {@link SpellArg#CARD_SOURCE} is specified, this spell will generate the
 * cards specified by that source and filter, assuming an {@link net.demilich.metastone.game.spells.desc.filter.AndFilter}
 * (i.e., all cards accepted) and a {@link net.demilich.metastone.game.spells.desc.source.CatalogueSource} by default.
 * If the available cards to choose from after applying this filter to the source is zero (i.e., there are no cards to
 * choose from), the spell will instead put the card specified by {@link SpellArg#CARD} into the player's hand. This
 * should be interpreted as the "replacement" card.
 * <p>
 * If neither a {@link SpellArg#CARD_FILTER}, {@link SpellArg#CARD_SOURCE}, {@link SpellArg#CARD} nor {@link
 * SpellArg#CARDS} arguments are specified, the spell will interpret the {@code target} (either from the parent spell or
 * the {@link SpellArg#TARGET}) as the card to receive as long as the {@code target} is a {@link Card}.
 * <p>
 * When a {@link SpellArg#VALUE} is specified, the spell will interpret the value as either copies of a single card or
 * how many cards to choose from a collection of {@link SpellArg#CARDS} or the filtered cards. This value is {@code 1}
 * by default.
 * <p>
 * The rules for how to interpret {@link SpellArg#VALUE} go as follows: <ul> <li>{@link SpellArg#CARDS} argument set,
 * {@link SpellArg#RANDOM_TARGET}={@code false} (by default):  {@link SpellArg#VALUE} copies of each card in {@link
 * SpellArg#CARDS}.</li> <li>{@link SpellArg#CARDS} argument set, {@link SpellArg#RANDOM_TARGET}={@code true}: {@link
 * SpellArg#VALUE} cards will be drawn <b>with replacement</b> from the collection specified by the {@link
 * SpellArg#CARDS} argument.</li> <li>Any {@link SpellArg#CARD_FILTER} or {@link SpellArg#CARD_SOURCE}: {@link
 * SpellArg#VALUE} cards will be drawn <b>without replacement</b> from the filtered list of cards.</li><li>All other
 * situations (i.e., {@code target} is a {@link Card}: {@link SpellArg#VALUE} will be ignored.</li></ul>
 * <p>
 * {@link SpellArg#TARGET_PLAYER} indicates who will receive the cards.
 * <p>
 * If the card is successfully put into the player's hand, subspells specified in {@link SpellArg#SPELL} will be cast,
 * where the {@code target} is this spell's {@code target} and {@link EntityReference#OUTPUT} references the created
 * card.
 * <p>
 * For <b>example,</b> to receive one of three cards:
 * <pre>
 *      {
 *          "class": "ReceiveCardSpell",
 *          "cards": [
 *              "spell_i_am_murloc",
 *              "spell_power_of_the_horde",
 *              "spell_rogues_do_it"
 *          ],
 *          "randomTarget": true,
 *          "targetPlayer": "SELF"
 *      }
 * </pre>
 * By contrast, to receive all three cards specified (notice that {@link SpellArg#RANDOM_TARGET} is omitted because it
 * is {@code false} by default):
 * <pre>
 *      {
 *          "class": "ReceiveCardSpell",
 *          "cards": [
 *              "spell_i_am_murloc",
 *              "spell_power_of_the_horde",
 *              "spell_rogues_do_it"
 *          ],
 *          "targetPlayer": "SELF"
 *      }
 * </pre>
 * To get a copy of a random minion in the opponent's deck, or a Shadow of Nothing if your opponent had no minion cards
 * (adapted from Mindgames):
 * <pre>
 *      {
 *          "class": "ReceiveCardSpell",
 *          "card": "token_shadow_of_nothing",
 *          "cardFilter": {
 *              "class": "CardFilter",
 *              "cardType": "MINION"
 *          },
 *          "cardSource": {
 *              "class": "DeckSource",
 *              "targetPlayer": "OPPONENT"
 *          }
 *      }
 * </pre>
 * To receive a random minion and give it +2/+2 in your hand (notice the {@link SpellArg#TARGET} of the {@link
 * BuffSpell} is {@link EntityReference#OUTPUT}):
 * <pre>
 *     {
 *         "class": "ReceiveCardSpell",
 *         "cardFilter": {
 *             "class": "CardFilter",
 *             "cardType": "MINION"
 *         },
 *         "spell": {
 *             "class": "BuffSpell",
 *             "target": "OUTPUT",
 *             "attackBonus": 2,
 *             "hpBonus": 2
 *         }
 *     }
 * </pre>
 * <p>
 * This spell will not receive a {@link Card} {@code target} if its owner does not match the player.
 *
 * @see StealCardSpell for how to steal/move cards from the opponent's possession to the target player's possession.
 * @see CopyCardSpell for how to copy a card.
 */
public class ReceiveCardSpell extends Spell {

	private static Logger logger = LoggerFactory.getLogger(ReceiveCardSpell.class);

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		checkArguments(logger, context, source, desc, SpellArg.VALUE, SpellArg.CARD_FILTER, SpellArg.CARD_SOURCE, SpellArg.CARD, SpellArg.CARDS);
		SpellDesc subSpell = (SpellDesc) desc.get(SpellArg.SPELL);
		int count = desc.getValue(SpellArg.VALUE, context, player, target, source, 1);

		if (count == 0) {
			logger.warn("onCast {} {}: Suspicious call that was receive a VALUE computed to be 0, which is not the default. The VALUE arg is {}", context.getGameId(), source, desc.get(SpellArg.VALUE));
		}

		if (count < -1) {
			logger.error("onCast {} {}: A negative number of cards was specified by the VALUE.", context.getGameId(), source);
			return;
		}

		// If a card is being received from a filter, we're creating new cards
		if (desc.containsKey(SpellArg.CARD_FILTER)
				|| desc.containsKey(SpellArg.CARD_SOURCE)) {

			// The cards are always copies if they are created this way
			if (desc.getCardSource() != null
					&& !(desc.getCardSource() instanceof HasCardCreationSideEffects)) {
				logger.warn("onCast {} {}: Suspicious call with a CARD_SOURCE {} that does not create cards and we're behaving as though it does.", context.getGameId(), source, desc.getCardSource());
			}

			CardList cards = desc.getFilteredCards(context, player, source).getCopy();
			String replacementCard = (String) desc.get(SpellArg.CARD);
			for (int i = 0; i < count; i++) {
				Card card = null;
				if (!cards.isEmpty()) {
					card = context.getLogic().removeRandom(cards);
				} else if (replacementCard != null) {
					logger.debug("onCast {} {}: No cards were produced by the filter or source, so a replacement {} was used instead", context.getGameId(), source, replacementCard);
					card = context.getCardById(replacementCard);
				}

				if (card != null) {
					context.getLogic().receiveCard(player.getId(), card);
					SpellUtils.castChildSpell(context, player, subSpell, source, target, card);
				}
			}
		} else if (desc.containsKey(SpellArg.CARD) || desc.containsKey(SpellArg.CARDS)) {
			// If a card isn't received from a filter, it's coming from a description
			// These cards should always be copies
			boolean chooseRandomly = (boolean) desc.getOrDefault(SpellArg.RANDOM_TARGET, false);
			List<Card> receivableCards = new ArrayList<>(Arrays.asList(SpellUtils.getCards(context, desc)));
			if (!chooseRandomly) {
				for (Card card : receivableCards) {
					// Move at most one card from discover or create a card. Handled by get cards.
					for (int i = 0; i < count; i++) {
						card = card.getCopy();
						context.getLogic().receiveCard(player.getId(), card);
						SpellUtils.castChildSpell(context, player, subSpell, source, target, card);
					}
				}
			} else {
				for (int i = 0; i < count; i++) {
					if (receivableCards.isEmpty()) {
						continue;
					}

					final Card card = context.getLogic().removeRandom(receivableCards).getCopy();
					context.getLogic().receiveCard(player.getId(), card);
					SpellUtils.castChildSpell(context, player, subSpell, source, target, card);

				}
			}
		} else if (target instanceof Card && target.getOwner() == player.getId()) {
			// The card is being moved into the hand from somewhere
			final Card card = (Card) target;
			context.getLogic().receiveCard(player.getId(), card);
			SpellUtils.castChildSpell(context, player, subSpell, source, target, card);
		} else if (!(target instanceof Card)) {
			logger.error("onCast {} {}: Attempting to receive non-Card target {}", context.getGameId(), source, target);
		} else if (!(target.getOwner() == player.getId())) {
			logger.error("onCast {} {}: Attempting to receive a card {} owned by {}, who is not the casting player {}", context.getGameId(), source, target, context.getPlayer(target.getOwner()), player);
		}
	}

	/**
	 * Creates this spell to simply receive the {@code target}.
	 *
	 * @return The spell
	 */
	public static SpellDesc create() {
		Map<SpellArg, Object> arguments = SpellDesc.build(ReceiveCardSpell.class);
		return new SpellDesc(arguments);
	}

	/**
	 * Creates this spell to receive the specified card.
	 *
	 * @param cardId The ID of the card to receive.
	 * @return The spell
	 */
	public static SpellDesc create(String cardId) {
		Map<SpellArg, Object> arguments = SpellDesc.build(ReceiveCardSpell.class);
		arguments.put(SpellArg.CARD, cardId);
		return new SpellDesc(arguments);
	}

	/**
	 * Creates this spell to receive the specified target.
	 *
	 * @param target An {@link EntityReference} to a card.
	 * @return The spell
	 */
	public static SpellDesc create(EntityReference target) {
		Map<SpellArg, Object> arguments = SpellDesc.build(ReceiveCardSpell.class);
		arguments.put(SpellArg.TARGET, target);
		return new SpellDesc(arguments);
	}

	/**
	 * Creates this spell to receive one of the specified cards.
	 *
	 * @param cardIds The cards to choose from, randomly.
	 * @return The spell
	 */
	public static SpellDesc create(String... cardIds) {
		Map<SpellArg, Object> arguments = SpellDesc.build(ReceiveCardSpell.class);
		arguments.put(SpellArg.CARDS, cardIds);
		arguments.put(SpellArg.RANDOM_TARGET, true);
		return new SpellDesc(arguments);
	}

	/**
	 * Creates this spell to put {@code cards} amount of cards filtered from the catalogue into the player's hand.
	 *
	 * @param filter A filter to apply to a {@link net.demilich.metastone.game.spells.desc.source.CatalogueSource}.
	 *               Typically, you should specify a {@link net.demilich.metastone.game.spells.desc.filter.CardFilter}
	 *               with a specified {@link net.demilich.metastone.game.spells.desc.filter.FilterArg#CARD_TYPE}.
	 * @param cards  How many cards should be received from the filtered cards, <b>without replacement</b>. This means
	 *               each card will be distinct as long as the source gave distinct cards.
	 * @return The spell
	 */
	public static SpellDesc create(EntityFilter filter, int cards) {
		return create(CatalogueSource.create(), filter, cards);
	}

	/**
	 * Creates this spell to put {@code cards} amount of cards filtered from the {@code source} into the player's hand.
	 *
	 * @param source The {@link CardSource} to use to filter. When {@code null}, defaults to a {@link CatalogueSource}.
	 * @param filter A filter to apply to the {@code source}. Typically, you should specify a {@link
	 *               net.demilich.metastone.game.spells.desc.filter.CardFilter} with a specified {@link
	 *               net.demilich.metastone.game.spells.desc.filter.FilterArg#CARD_TYPE}.
	 * @param cards  How many cards should be received from the filtered cards, <b>without replacement</b>. This means
	 *               each card will be distinct as long as the source gave distinct cards.
	 * @return The spell
	 */
	public static SpellDesc create(CardSource source, EntityFilter filter, int cards) {
		Map<SpellArg, Object> arguments = SpellDesc.build(ReceiveCardSpell.class);
		arguments.put(SpellArg.CARD_FILTER, filter);
		arguments.put(SpellArg.CARD_SOURCE, source);
		arguments.put(SpellArg.VALUE, cards);
		return new SpellDesc(arguments);
	}
}
