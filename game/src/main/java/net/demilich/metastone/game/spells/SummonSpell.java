package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.MinionCard;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.EntityType;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.entities.minions.RelativeToSource;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
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
 * false}, a random card from the {@link SpellArg#CARDS} is chosen. {@link SpellArg#VALUE} (default 1) choices will be
 * made, summoning a total of {@link SpellArg#VALUE} minions.
 * <p>
 * If a {@link SpellArg#CARD_FILTER} or {@link SpellArg#CARD_SOURCE} is specified, {@link SpellArg#VALUE} minions will
 * be summoned from the generated cards, <b>without replacement</b>. Any {@link SpellArg#CARD} or {@link SpellArg#CARDS}
 * that are also specified when a filter/card source is specified will be append to the possible choices of cards to
 * summon.
 * <p>
 * If {@link SpellArg#CARD}, {@link SpellArg#CARDS}, {@link SpellArg#CARD_FILTER}, and {@link SpellArg#CARD_SOURCE} are
 * all omitted, the spell will try to summon a <b>copy</b> of {@code target}. If the {@code target} is a {@link
 * MinionCard}, it is used as the card to {@link MinionCard#summon()} from; otherwise, if the {@code target} is a {@link
 * Minion}, the target is copied with {@link Actor#getCopy()}, its enchantments are removed, it is summoned, and then
 * the enchantments are copied.
 * <p>
 * All of the successfully summoned minions will get the {@link SpellArg#SPELL} subspell cast on each of them, where
 * {@link EntityReference#OUTPUT} will reference each summoned minion.
 * <p>
 * The minions will be summoned in the last spot on the {@link Zones#BATTLEFIELD} unless the {@link
 * SpellArg#BOARD_POSITION_RELATIVE} argument is set. When set to {@link RelativeToSource#RIGHT}, and the {@code source}
 * of the spell is a {@link Minion}, the summoned minion will appear to the right of the {@code source}.
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
 *
 * @see ResurrectSpell for the effect of resurrecting dead minions without repeats.
 */
public class SummonSpell extends Spell {

	private static Logger logger = LoggerFactory.getLogger(SummonSpell.class);

	/**
	 * Creates this spell to summon the specified minion cards
	 *
	 * @param minionCards One or more minions to summon. Each will be summoned.
	 * @return The spell
	 */
	public static SpellDesc create(MinionCard... minionCards) {
		return create(TargetPlayer.SELF, minionCards);
	}

	/**
	 * Creates this spell to summon the specified minions relative to the source minion (used in a battlecry).
	 *
	 * @param relativeBoardPosition The board position.
	 * @param minionCards           One or more minions to summon. Each will be summoned.
	 * @return The spell
	 */
	public static SpellDesc create(RelativeToSource relativeBoardPosition, MinionCard... minionCards) {
		return create(TargetPlayer.SELF, relativeBoardPosition, minionCards);
	}

	/**
	 * Summons the specified minion card ID
	 *
	 * @param minionCard The String minion card ID
	 * @return The spell
	 */
	public static SpellDesc create(String minionCard) {
		Map<SpellArg, Object> arguments = SpellDesc.build(SummonSpell.class);
		arguments.put(SpellArg.CARD, minionCard);
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
		Map<SpellArg, Object> arguments = SpellDesc.build(SummonSpell.class);
		arguments.put(SpellArg.CARDS, minionCards);
		arguments.put(SpellArg.TARGET, EntityReference.NONE);
		return new SpellDesc(arguments);
	}

	/**
	 * Summons the specified cards for the specified player.
	 *
	 * @param targetPlayer The player on whose battlefield these minions should be summoned
	 * @param minionCards  The minion cards
	 * @return The spell
	 */
	public static SpellDesc create(TargetPlayer targetPlayer, MinionCard... minionCards) {
		return create(targetPlayer, null, minionCards);
	}

	/**
	 * Summons the specified minion cards relative to a given source for the specified player (used for a battlecry).
	 *
	 * @param targetPlayer          The player whose battlefield should be the destination for these minions
	 * @param relativeBoardPosition Relative to the source minion (when played as a battlecry), where should these
	 *                              minions be summoned?
	 * @param minionCards           The cards to summon from
	 * @return The spell
	 */
	public static SpellDesc create(TargetPlayer targetPlayer, RelativeToSource relativeBoardPosition, MinionCard... minionCards) {
		Map<SpellArg, Object> arguments = SpellDesc.build(SummonSpell.class);
		String[] cardNames = new String[minionCards.length];
		for (int i = 0; i < minionCards.length; i++) {
			cardNames[i] = minionCards[i].getCardId();
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
		// Summon minions from the cards or cardIds specified
		List<Minion> summonedMinions = new ArrayList<>();
		int boardPosition = SpellUtils.getBoardPosition(context, player, desc, source);
		int count = desc.getValue(SpellArg.VALUE, context, player, target, source, 1);

		if (count <= 0) {
			logger.error("onCast {} {}: An invalid count of {} was specified. The VALUE argument was {}", context.getGameId(), source, count, desc.get(SpellArg.VALUE));
			return;
		}

		List<Card> cards = new ArrayList<>();

		final boolean hasFilter = desc.getCardFilter() != null || desc.getCardSource() != null;
		if (hasFilter) {
			cards.addAll(desc.getFilteredCards(context, player, source));
			// The SpellArg.CARD field should be interpreted as a replacement card in this scenario.
		} else {
			cards.addAll(Arrays.asList(SpellUtils.getCards(context, desc)));
		}

		if (desc.getBool(SpellArg.EXCLUSIVE)) {
			Set<String> existingCardIds = player.getMinions().stream()
					.map(Minion::getSourceCard)
					.map(Card::getCardId)
					.distinct()
					.collect(Collectors.toSet());
			cards.removeIf(c -> existingCardIds.contains(c.getCardId()));
		}

		if (cards.size() > 0) {
			if (desc.getBool(SpellArg.RANDOM_TARGET)
					|| hasFilter) {
				for (int i = 0; i < count; i++) {
					MinionCard card = (MinionCard) context.getLogic().getRandom(cards);
					if (card == null) {
						if (desc.containsKey(SpellArg.CARD)) {
							String replacementCard = desc.getString(SpellArg.CARD);
							card = (MinionCard) context.getCardById(replacementCard);
						} else {
							// No card or replacement found.
							continue;
						}
					}
					final Minion minion = card.summon();

					if (context.getLogic().summon(player.getId(), minion, null, boardPosition, false)) {
						summonedMinions.add(minion);
						cards.remove(card);
					}
				}
			} else {
				for (Card card : cards) {
					for (int i = 0; i < count; i++) {
						MinionCard minionCard = count == 1 ? (MinionCard) card : (MinionCard) card.clone();
						final Minion minion = minionCard.summon();

						if (context.getLogic().summon(player.getId(), minion, null, boardPosition, false)) {
							summonedMinions.add(minion);
						}
					}
				}
			}
		} else if (target != null
				&& !(target.getReference().equals(EntityReference.NONE))) {
			for (int i = 0; i < count; i++) {
				Minion minion;
				if (target.getEntityType() == EntityType.CARD) {
					minion = ((MinionCard) target.getSourceCard()).summon();
				} else {
					minion = ((Minion) target).getCopy();
					minion.clearEnchantments();
				}

				boolean summoned = context.getLogic().summon(player.getId(), minion, null, boardPosition, false);
				if (!summoned) {
					// It's still possible that, even if a minion was successfully summoned, a subspell later destroys it
					return;
				}
				summonedMinions.add(minion);
				if (target instanceof Actor) {
					for (Trigger trigger : context.getTriggersAssociatedWith(target.getReference())) {
						Trigger triggerClone = trigger.clone();
						context.getLogic().addGameEventListener(player, triggerClone, minion);
					}
				}
			}
		}

		if (summonedMinions.size() == 0) {
			logger.debug("onCast {} {}: No minions were successfully summoned. Usually this is due to a full board or a secret.", context.getGameId(), source);
		}

		summonedMinions.forEach(summoned -> {
			// Shouldn't cast spells on minions that wound up in the graveyard somehow due to other subspells.
			// This checks if a subspell has ended the sequence with {@link GameLogic#endOfSequence()}
			if (summoned.isDestroyed()
					&& summoned.getZone() == Zones.GRAVEYARD) {
				logger.debug("onCast {} {}: The minion {} that was previously summoned successfully is now destroyed or in the graveyard", context.getGameId(), source, summoned);
				return;
			}

			desc.subSpells(0).forEach(subSpell -> {
				SpellUtils.castChildSpell(context, player, subSpell, source, target, summoned);
			});
		});
	}

}
