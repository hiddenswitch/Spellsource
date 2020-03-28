package net.demilich.metastone.game.spells.custom;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardArrayList;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.EntityType;
import net.demilich.metastone.game.environment.EnvironmentAftermathTriggeredList;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.SpellUtils;
import net.demilich.metastone.game.spells.TargetPlayer;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.TargetNotFoundException;
import net.demilich.metastone.game.targeting.Zones;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Repeats all other aftermaths the casting player has triggered as long as the source is in play.
 */
public class RepeatAllAftermathsSpell extends Spell {

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		if (SpellUtils.isRecursive(RepeatAllAftermathsSpell.class)) {
			return;
		}

		var executedOn = EnvironmentEntityList.getList(context);
		if (!executedOn.getReferences(context, source).isEmpty()) {
			return;
		}

		TargetPlayer castingTargetPlayer = desc.getTargetPlayer() == null ? TargetPlayer.OWNER : desc.getTargetPlayer();
		List<EnvironmentAftermathTriggeredList.EnvironmentAftermathTriggeredItem> getAftermaths = getAftermaths(context, source);
		boolean isItselfAftermath = desc.containsKey(SpellArg.AFTERMATH_ID);

		int index;
		if (isItselfAftermath) {
			index = desc.getInt(SpellArg.BOARD_POSITION_ABSOLUTE);
		} else {
			index = -1;
		}
		if (isItselfAftermath) {
			CardList cards = new CardArrayList();
			List<SpellDesc> spells = getAftermaths.stream().filter(item -> {
				if (RepeatAllAftermathsSpell.class.isAssignableFrom(item.getSpell().getDescClass())) {
					return false;
				}

				if (!aftermathPredicate(context, player, source, target, item)) {
					return false;
				}
				// We are only interested in source cards, so it's okay if we're accepting removed from play entities
				var sourceCard = context.resolveSingleTarget(item.getSource(), false).getSourceCard();
				cards.add(sourceCard);
				executedOn.add(source, sourceCard);
				return true;
			}).map(EnvironmentAftermathTriggeredList.EnvironmentAftermathTriggeredItem::getSpell)
					.collect(Collectors.toList());

			// The source is valid, we don't use the aftermath's source
			context.getLogic().resolveAftermaths(player.getId(), source.getReference(), spells, player.getId(), index, false);
			// TODO: We should probably reveal the cards one by one
			cards.forEach(card -> context.getLogic().revealCard(player, card));
		} else {
			var aftermathSource = source;
			for (EnvironmentAftermathTriggeredList.EnvironmentAftermathTriggeredItem item : getAftermaths) {
				SpellDesc spell = item.getSpell();
				if (RepeatAllAftermathsSpell.class.isAssignableFrom(spell.getDescClass())) {
					continue;
				}

				if (!aftermathPredicate(context, player, source, target, item)) {
					continue;
				}

				// if the source is a minion, execute the spells from this point of view
				// otherwise, use the original source
				if (source.getEntityType() == EntityType.MINION && (source.isInPlay() || source.getZone() == Zones.GRAVEYARD)) {
					aftermathSource = source;
				} else {
					aftermathSource = context.resolveSingleTarget(item.getSource());
				}

				if (aftermathSource.getZone() != Zones.GRAVEYARD && !aftermathSource.isInPlay()) {
					// We can't call aftermaths on resolved targets that are not in play or not in the graveyard
					try {
						var results = context.resolveTarget(player, aftermathSource, spell.getTarget());
						if (results != null && results.stream().allMatch(e -> e.getZone() == Zones.REMOVED_FROM_PLAY)) {
							continue;
						}
					} catch (TargetNotFoundException targetNotFound) {
						continue;
					}
				}

				// requires valid source card because we want something to show to the player when it is cast
				if (aftermathSource.getSourceCard() == null) {
					continue;
				}

				SpellUtils.DetermineCastingPlayer determineCastingPlayer = SpellUtils.determineCastingPlayer(context, player, aftermathSource, castingTargetPlayer);
				Player castingPlayer = determineCastingPlayer.getCastingPlayer();
				var sourceCard = context.resolveSingleTarget(item.getSource()).getSourceCard();
				executedOn.add(source, sourceCard);
				context.getLogic().resolveAftermaths(castingPlayer.getId(), aftermathSource.getReference(), Collections.singletonList(spell), aftermathSource.getOwner(), index, false);
				context.getLogic().revealCard(player, sourceCard);
			}
		}
		executedOn.clear(source);
	}

	protected boolean aftermathPredicate(GameContext context, Player player, Entity source, Entity target, EnvironmentAftermathTriggeredList.EnvironmentAftermathTriggeredItem item) {
		if (item.getPlayerId() != player.getId()) {
			return false;
		}
		return true;
	}

	@NotNull
	protected ArrayList<EnvironmentAftermathTriggeredList.EnvironmentAftermathTriggeredItem> getAftermaths(GameContext context, Entity source) {
		return new ArrayList<>(context.getAftermaths().getAftermaths());
	}
}

