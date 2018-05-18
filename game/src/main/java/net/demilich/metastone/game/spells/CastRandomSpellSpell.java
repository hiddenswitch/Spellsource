package net.demilich.metastone.game.spells;

import java.util.Map;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.cards.*;
import net.demilich.metastone.game.entities.EntityType;
import net.demilich.metastone.game.spells.desc.filter.CardFilter;
import net.demilich.metastone.game.spells.desc.filter.SpecificCardFilter;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.targeting.Zones;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.demilich.metastone.game.utils.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;

/**
 * Casts a random spell from the {@link SpellArg#CARD_SOURCE}, {@link SpellArg#CARD_FILTER} and {@link SpellArg#CARDS}
 * provided.
 * <p>
 * For example, to implement "Whenever a player casts a spell, cast a copy of it for them with random targets.":
 * <pre>
 *   {
 *     "eventTrigger": {
 *       "class": "SpellCastedTrigger",
 *       "sourcePlayer": "BOTH"
 *     },
 *     "spell": {
 *       "class": "CastRandomSpellSpell",
 *       "cardFilter": {
 *         "class": "AndFilter",
 *         "filters": [
 *           {
 *             "class": "SpecificCardFilter",
 *             "secondaryTarget": "EVENT_SOURCE"
 *           },
 *           {
 *             "class": "CardFilter",
 *             "cardType": "SPELL"
 *           }
 *         ]
 *       },
 *       "cardSource": {
 *         "class": "UncollectibleCatalogueSource"
 *       },
 *       "targetPlayer": "ACTIVE"
 *     }
 *   }
 * </pre>
 * Observe that the {@link CardFilter} is a {@link SpecificCardFilter} that is configured, using its {@code
 * "secondaryTarget"} option, to only be {@code true} for cards that are equal to {@link EntityReference#EVENT_SOURCE},
 * i.e., the card that was casted. Only one card (the card that was casted) will match, so that's the card that will be
 * randomly cast.
 */
public class CastRandomSpellSpell extends Spell {
	Logger logger = LoggerFactory.getLogger(CastRandomSpellSpell.class);

	public static SpellDesc create(int value) {
		Map<SpellArg, Object> arguments = new SpellDesc(CastRandomSpellSpell.class);
		arguments.put(SpellArg.VALUE, value);
		return new SpellDesc(arguments);
	}

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		checkArguments(logger, context, source, desc, SpellArg.VALUE, SpellArg.CARD, SpellArg.CARDS, SpellArg.CARD_SOURCE, SpellArg.CARD_FILTER);
		TargetPlayer castingTargetPlayer = desc.getTargetPlayer() == null ? TargetPlayer.OWNER : desc.getTargetPlayer();
		player.setAttribute(Attribute.RANDOM_CHOICES);
		int numberOfSpellsToCast = desc.getValue(SpellArg.VALUE, context, player, target, source, 1);
		CardList spells = SpellUtils.getCards(context, player, target, source, desc, numberOfSpellsToCast);
		for (int i = 0; i < numberOfSpellsToCast; i++) {
			if (spells.isEmpty()) {
				logger.warn("onCast {} {}: An empty number of spells were found with the filter {} and source {}", context.getGameId(), source, desc.getCardFilter(), desc.getCardSource());
				break;
			}
			DetermineCastingPlayer determineCastingPlayer = determineCastingPlayer(context, player, source, castingTargetPlayer);
			if (!determineCastingPlayer.isSourceInPlay()) {
				break;
			}
			Player castingPlayer = determineCastingPlayer.getCastingPlayer();

			// Must retrieve a copy because castWithRandomTargets mutates the incoming spell card
			Card randomCard = context.getLogic().getRandom(spells).getCopy();
			logger.debug("onCast {} {}: Casting random spell {}", context.getGameId(), source, randomCard);
			RandomCardTargetSpell.castCardWithRandomTargets(context, castingPlayer, source, randomCard);
			context.getLogic().endOfSequence();
		}

		player.getAttributes().remove(Attribute.RANDOM_CHOICES);
	}

	public static DetermineCastingPlayer determineCastingPlayer(GameContext context, Player player, Entity source, TargetPlayer castingTargetPlayer) {
		return new DetermineCastingPlayer(context, player, source, castingTargetPlayer).invoke();
	}

	public static class DetermineCastingPlayer {
		private boolean sourceDestroyed;
		private GameContext context;
		private Player player;
		private Entity source;
		private TargetPlayer castingTargetPlayer;
		private Player castingPlayer;

		public DetermineCastingPlayer(GameContext context, Player player, Entity source, TargetPlayer castingTargetPlayer) {
			this.context = context;
			this.player = player;
			this.source = source;
			this.castingTargetPlayer = castingTargetPlayer;
		}

		public boolean isSourceInPlay() {
			return !sourceDestroyed;
		}

		public Player getCastingPlayer() {
			return castingPlayer;
		}

		public DetermineCastingPlayer invoke() {
			// In case Yogg changes sides, this should case who the spells are being cast for.
			switch (castingTargetPlayer) {
				case BOTH:
				case OWNER:
				default:
					castingPlayer = context.getPlayer(source.getOwner());
					break;
				case SELF:
					castingPlayer = player;
					break;
				case OPPONENT:
					castingPlayer = context.getOpponent(player);
					break;
				case ACTIVE:
					castingPlayer = context.getActivePlayer();
					break;
				case INACTIVE:
					castingPlayer = context.getOpponent(context.getActivePlayer());
					break;
			}

			// If Yogg is removed from the board, stop casting spells.
			if (source != null
					&& castingTargetPlayer == TargetPlayer.OWNER
					&& source.getEntityType() == EntityType.MINION
					&& (source.getZone()
					!= Zones.BATTLEFIELD
					|| source.isDestroyed())) {
				sourceDestroyed = true;
				return this;
			}
			sourceDestroyed = false;
			return this;
		}
	}
}
