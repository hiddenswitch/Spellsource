package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardType;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.EntityType;
import net.demilich.metastone.game.entities.minions.BoardPositionRelative;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.spells.custom.EnvironmentEntityList;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.source.SummonWithoutReplacementCardSource;
import net.demilich.metastone.game.spells.trigger.Trigger;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.targeting.Zones;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Summons minions specified by cards; summons random minions from card filters; or copies minions according to
 * targets.
 * <p>
 * When a {@link SpellArg#CARD} or {@link SpellArg#CARDS} are specified, all the specified cards are summoned {@link
 * SpellArg#VALUE} times. When {@link SpellArg#RANDOM_TARGET} is also set to {@code true} instead of its default, {@code
 * false}, a random card from the {@link SpellArg#CARDS} is chosen. Choices are made <b>with replacement</b>. {@link
 * SpellArg#VALUE} (default 1) choices will be made, summoning a total of {@link SpellArg#VALUE} minions.
 * <p>
 * If a {@link SpellArg#CARD_FILTER} or {@link SpellArg#CARD_SOURCE} is specified, {@link SpellArg#VALUE} minions will
 * be summoned from the generated cards, <b>without replacement</b>. Any {@link SpellArg#CARD} or {@link SpellArg#CARDS}
 * that are also specified when a filter/card source is specified will be append to the possible choices of cards to
 * summon.
 * <p>
 * If {@link SpellArg#CARD}, {@link SpellArg#CARDS}, {@link SpellArg#CARD_FILTER}, and {@link SpellArg#CARD_SOURCE} are
 * all omitted, the spell will try to summon a <b>copy</b> of {@code target}. If the {@code target} is a {@link Card},
 * it is used as the card to {@link Card#summon()} from; otherwise, if the {@code target} is a {@link Minion}, the
 * target is copied with {@link Actor#getCopy()}, its enchantments are removed, it is summoned, and then the
 * enchantments are copied.
 * <p>
 * This effect will summon {@link SpellArg#VALUE} copies of whatever is specified, defaulting to {@code 1}.
 * <p>
 * All of the successfully summoned minions will get the {@link SpellArg#SPELL} subspell cast on each of them, where
 * {@link EntityReference#OUTPUT} will reference each summoned minion.
 * <p>
 * The minions will be summoned in the last spot on the {@link Zones#BATTLEFIELD} unless the {@link
 * SpellArg#BOARD_POSITION_RELATIVE} argument is set. When set to {@link BoardPositionRelative#RIGHT}, and the {@code
 * source} of the spell is a {@link Minion}, the summoned minion will appear to the right of the {@code source}.
 * <p>
 * If {@link SpellArg#EXCLUSIVE} is specified, the spell will not summon minions whose card IDs are already on the
 * battlefield.
 * <p>
 * Many minions summon tokens to their side in their {@link net.demilich.metastone.game.spells.desc.BattlecryDesc#spell}
 * argument. For <b>example:</b>
 * <pre>
 *     {
 *         "class": "SummonSpell",
 *         "card": "token_ooze",
 *         "boardPositionRelative": "RIGHT",
 *         "targetPlayer": "SELF"
 *     }
 * </pre>
 * You can summon multiple Oozes by specifying a {@link SpellArg#VALUE}:
 * <pre>
 *     {
 *         "class": "SummonSpell",
 *         "card": "token_ooze",
 *         "boardPositionRelative": "RIGHT",
 *         "targetPlayer": "SELF",
 *         "value": 2
 *     }
 * </pre>
 * Other minions commonly summon a copy of themselves in their Battlecries:
 * <pre>
 *     {
 *         "class": "SummonSpell",
 *         "target": "SELF"
 *     }
 * </pre>
 * To summon a 1/1 copy of itself (notice that the {@link SpellArg#TARGET} is changed to {@link
 * EntityReference#OUTPUT}):
 * <pre>
 *     {
 *         "class": "SummonSpell",
 *         "target": "SELF",
 *         "spell": {
 *             "class": "MetaSpell",
 *             "target": "OUTPUT",
 *             "spells": [
 *                  {
 *                      "class": "SetAttackSpell",
 *                      "value": 1
 *                  },
 *                  {
 *                      "class": "SetHpSpell",
 *                      "value": 1
 *                  }
 *             ]
 *         }
 *     }
 * </pre>
 * To summon a random cost-2 minion for the casting player's opponent:
 * <pre>
 *      {
 *          "class": "SummonSpell",
 *          "cardFilter": {
 *              "class": "CardFilter",
 *              "cardType": "MINION",
 *              "manaCost": 2
 *          },
 *          "targetPlayer": "OPPONENT"
 *      }
 * </pre>
 * To summon a minion in the opponent's deck:
 * <pre>
 *     {
 *         "class": "SummonSpell",
 *         "cardFilter": {
 *             "class": "CardFilter",
 *             "cardType": "MINION"
 *         },
 *         "cardSource": {
 *             "class": "DeckSource",
 *             "targetPlayer": "OPPONENT"
 *         }
 *     }
 * </pre>
 * To summon one of four minions that don't already exist on the battlefield:
 * <pre>
 *     {
 *          "class": "SummonSpell",
 *          "cards": [
 *              "token_searing_totem",
 *              "token_healing_totem",
 *              "token_wrath_of_air_totem",
 *              "token_stoneclaw_totem"
 *          ],
 *          "exclusive": true,
 *          "randomTarget": true
 *     }
 * </pre>
 * <p>
 * To summon minions from a list of cards without replacement:
 * <pre>
 *     "spell": {
 *     "class": "SummonSpell",
 *     "value": 2,
 *     "cards": [
 *       "token_bellowing_spirit",
 *       "token_unearthed_spirit",
 *       "token_burning_spirit"
 *     ],
 *     "randomTarget": true,
 *     "cardSource": {
 *       "class": "SummonWithoutReplacementCardSource"
 *     }
 * </pre>
 *
 * @see ResurrectSpell for the effect of resurrecting dead minions without repeats.
 */
public class SummonSpell extends Spell {

	private static Logger logger = LoggerFactory.getLogger(SummonSpell.class);

	/**
	 * Creates this spell to summon the specified minion cards
	 *
	 * @param cards One or more minions to summon. Each will be summoned.
	 * @return The spell
	 */
	public static SpellDesc create(Card... cards) {
		return create(TargetPlayer.SELF, cards);
	}

	/**
	 * Creates this spell to summon the specified minions relative to the source minion (used in a battlecry).
	 *
	 * @param relativeBoardPosition The board position.
	 * @param cards                 One or more minions to summon. Each will be summoned.
	 * @return The spell
	 */
	public static SpellDesc create(BoardPositionRelative relativeBoardPosition, Card... cards) {
		return create(TargetPlayer.SELF, relativeBoardPosition, cards);
	}

	/**
	 * Summons the specified minion card ID
	 *
	 * @param Card The String minion card ID
	 * @return The spell
	 */
	public static SpellDesc create(String Card) {
		Map<SpellArg, Object> arguments = new SpellDesc(SummonSpell.class);
		arguments.put(SpellArg.CARD, Card);
		arguments.put(SpellArg.TARGET, EntityReference.NONE);
		return new SpellDesc(arguments);
	}

	/**
	 * Summons the specified minion cards by their IDs.
	 *
	 * @param minionCards The card IDs to summon.
	 * @return The spell
	 */
	public static SpellDesc create(String[] minionCards) {
		Map<SpellArg, Object> arguments = new SpellDesc(SummonSpell.class);
		arguments.put(SpellArg.CARDS, minionCards);
		arguments.put(SpellArg.TARGET, EntityReference.NONE);
		return new SpellDesc(arguments);
	}

	/**
	 * Summons the specified cards for the specified player.
	 *
	 * @param targetPlayer The player on whose battlefield these minions should be summoned
	 * @param cards        The minion cards
	 * @return The spell
	 */
	public static SpellDesc create(TargetPlayer targetPlayer, Card... cards) {
		return create(targetPlayer, null, cards);
	}

	/**
	 * Summons the specified minion cards relative to a given source for the specified player (used for a battlecry).
	 *
	 * @param targetPlayer          The player whose battlefield should be the destination for these minions
	 * @param relativeBoardPosition Relative to the source minion (when played as a battlecry), where should these minions
	 *                              be summoned?
	 * @param cards                 The cards to summon from
	 * @return The spell
	 */
	public static SpellDesc create(TargetPlayer targetPlayer, BoardPositionRelative relativeBoardPosition, Card... cards) {
		Map<SpellArg, Object> arguments = new SpellDesc(SummonSpell.class);
		String[] cardNames = new String[cards.length];
		for (int i = 0; i < cards.length; i++) {
			cardNames[i] = cards[i].getCardId();
		}
		arguments.put(SpellArg.CARDS, cardNames);
		arguments.put(SpellArg.TARGET, EntityReference.NONE);
		arguments.put(SpellArg.TARGET_PLAYER, targetPlayer);
		if (relativeBoardPosition != null) {
			arguments.put(SpellArg.BOARD_POSITION_RELATIVE, relativeBoardPosition);
		}
		return new SpellDesc(arguments);
	}

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		checkArguments(logger, context, source, desc, SpellArg.VALUE, SpellArg.CARD, SpellArg.CARDS, SpellArg.CARD_FILTER, SpellArg.CARD_SOURCE, SpellArg.BOARD_POSITION_RELATIVE, SpellArg.BOARD_POSITION_ABSOLUTE, SpellArg.EXCLUSIVE);
		// Summon minions from the cards or cardIds specified
		List<Minion> summonedMinions = new ArrayList<>();
		int boardPosition = SpellUtils.getBoardPosition(context, player, desc, source);
		int count = desc.getValue(SpellArg.VALUE, context, player, target, source, 1);

		if (count <= 0) {
			logger.debug("onCast {} {}: A summon count of {} was specified. The VALUE argument was {}", context.getGameId(), source, count, desc.get(SpellArg.VALUE));
			return;
		}

		List<Card> cards = new ArrayList<>();

		final boolean hasFilter = desc.getCardFilter() != null || desc.getCardSource() != null;
		final boolean isSummonWithoutReplacementCardSource = desc.getCardSource() != null
				&& desc.getCardSource().getClass().isAssignableFrom(SummonWithoutReplacementCardSource.class);
		if (hasFilter && !isSummonWithoutReplacementCardSource) {
			cards.addAll(desc.getFilteredCards(context, player, source));
			// The SpellArg.CARD field should be interpreted as a replacement card in this scenario.
		} else {
			cards.addAll(Arrays.asList(SpellUtils.getCards(context, desc)));
		}

		if (desc.getBool(SpellArg.EXCLUSIVE)) {
			Set<String> existingCardIds = player.getMinions().stream()
					.map(Minion::getSourceCard)
					.map(Card::getCardId)
					.collect(Collectors.toSet());
			cards.removeIf(c -> existingCardIds.contains(c.getCardId()));
		}

		// Remove all cards that cannot be summoned (for now, all non-minion cards)
		cards.removeIf(c -> c.getCardType() != CardType.MINION);

		if (cards.size() > 0) {
			// We are summoning one card at a time from the list, however the list of cards was generated
			if (desc.getBool(SpellArg.RANDOM_TARGET)
					|| hasFilter) {
				for (int i = 0; i < count; i++) {
					Card card = context.getLogic().getRandom(cards);
					// The list is empty, use a replacement card
					if (card == null) {
						// Only a single replacement card can be used
						if (desc.containsKey(SpellArg.CARD)) {
							String replacementCard = desc.getString(SpellArg.CARD);
							card = context.getCardById(replacementCard);
						} else {
							// No card or replacement found.
							continue;
						}
					}

					Minion minion = card.summon();
					if (context.getLogic().summon(player.getId(), minion, source, boardPosition, false)) {
						summonedMinions.add(minion);
						// If this is summoning from a filter or card source, as per the rules, the summoning occurs without replacement.
						if (hasFilter) {
							cards.remove(card);
						}
					}
				}
			} else {
				// We're just summoning all the cards in the list for COUNT times.
				for (Card card : cards) {
					for (int i = 0; i < count; i++) {
						card = count == 1 ? card : card.clone();
						final Minion minion = card.summon();

						if (context.getLogic().summon(player.getId(), minion, source, boardPosition, false)) {
							summonedMinions.add(minion);
						}
					}
				}
			}
		} else if (target != null
				&& !(target.getReference().equals(EntityReference.NONE))) {
			// We're cloning from a target (no list of cards or card source / filter specified)
			for (int i = 0; i < count; i++) {
				Minion minion;
				// Keep track if we ultimately summoned from the base card, because we shouldn't copy triggers in that case.
				boolean fromBase = false;
				// Is this a card? Summon it. Is this a non-battlefield minion? If so, summon from the base card too
				if (target.getEntityType() == EntityType.CARD
						|| (target.getEntityType() == EntityType.MINION && !target.isInPlay())) {
					if (!target.getSourceCard().getCardType().isCardType(CardType.MINION)) {
						logger.error("onCast {} {}: Cannot summon {} because it is not a minion", context.getGameId(), source, target);
						return;
					}
					fromBase = true;
					minion = target.getSourceCard().summon();
				} else if (target.getEntityType() != EntityType.MINION) {
					logger.error("onCast {} {}: Cannot summon {} because it is not a minion", context.getGameId(), source, target);
					return;
				} else {
					minion = ((Minion) target).getCopy();
					minion.clearEnchantments();
				}

				boolean summoned = context.getLogic().summon(player.getId(), minion, source, boardPosition, false);
				if (!summoned) {
					// It's still possible that, even if a minion was successfully summoned, a subspell later destroys it
					return;
				}
				summonedMinions.add(minion);
				if (!fromBase) {
					List<Trigger> triggers = context.getTriggersAssociatedWith(target.getReference());
					for (Trigger trigger : triggers) {
						Trigger triggerClone = trigger.clone();
						context.getLogic().addGameEventListener(player, triggerClone, minion);
					}

					// Copy over the stored entities, e.g. the Test Subject + Vivid Nightmare combo
					final EnvironmentEntityList list = EnvironmentEntityList.getList(context);
					for (EntityReference reference : list.getReferences(target)) {
						if (!reference.equals(EntityReference.NONE)) {
							list.add(minion, context.resolveSingleTarget(reference));
						}
					}
				}
			}
		}

		if (summonedMinions.size() == 0) {
			logger.debug("onCast {} {}: No minions were successfully summoned. Usually this is due to a full board or a secret.", context.getGameId(), source);
		}

		for (Minion summonedBeforeTransform : summonedMinions) {
			Entity summoned = summonedBeforeTransform.transformResolved(context);
			// Shouldn't cast spells on minions that wound up in the graveyard somehow due to other subspells.
			// This checks if a subspell has ended the sequence with {@link GameLogic#endOfSequence()}
			if (summoned.isDestroyed()
					&& summoned.getZone() == Zones.GRAVEYARD) {
				logger.debug("onCast {} {}: The minion {} that was previously summoned successfully is now destroyed or in the graveyard", context.getGameId(), source, summoned);
				return;
			}

			List<SpellDesc> collect = desc.subSpells(0);

			for (SpellDesc subSpell : collect) {
				SpellUtils.castChildSpell(context, player, subSpell, source, target, summoned);
			}
		}
	}

}

