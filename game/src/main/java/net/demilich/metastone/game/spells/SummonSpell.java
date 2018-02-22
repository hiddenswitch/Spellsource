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

import java.util.*;
import java.util.stream.Collectors;

/**
 * Summons minions specified by cards; summons random minions from card filters; or copies minions according to
 * targets.
 */
public class SummonSpell extends Spell {

	public static SpellDesc create(MinionCard... minionCards) {
		return create(TargetPlayer.SELF, minionCards);
	}

	public static SpellDesc create(RelativeToSource relativeBoardPosition, MinionCard... minionCards) {
		return create(TargetPlayer.SELF, relativeBoardPosition, minionCards);
	}

	public static SpellDesc create(String minionCard) {
		Map<SpellArg, Object> arguments = SpellDesc.build(SummonSpell.class);
		arguments.put(SpellArg.CARD, minionCard);
		arguments.put(SpellArg.TARGET, EntityReference.NONE);
		return new SpellDesc(arguments);
	}

	public static SpellDesc create(String[] minionCards) {
		Map<SpellArg, Object> arguments = SpellDesc.build(SummonSpell.class);
		arguments.put(SpellArg.CARDS, minionCards);
		arguments.put(SpellArg.TARGET, EntityReference.NONE);
		return new SpellDesc(arguments);
	}

	public static SpellDesc create(TargetPlayer targetPlayer, MinionCard... minionCards) {
		return create(targetPlayer, null, minionCards);
	}

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


		summonedMinions.forEach(summoned -> {
			// Shouldn't cast spells on minions that wound up in the graveyard somehow due to other subspells.
			// This checks if a subspell has ended the sequence with {@link GameLogic#endOfSequence()}
			if (summoned.isDestroyed()
					&& summoned.getZone() == Zones.GRAVEYARD) {
				return;
			}

			desc.subSpells(0).forEach(subSpell -> {
				SpellUtils.castChildSpell(context, player, subSpell, source, target, summoned);
			});
		});
	}

}
