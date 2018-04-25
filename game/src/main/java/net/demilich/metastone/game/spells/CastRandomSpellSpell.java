package net.demilich.metastone.game.spells;

import java.util.Map;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.cards.*;
import net.demilich.metastone.game.entities.EntityType;
import net.demilich.metastone.game.targeting.Zones;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.demilich.metastone.game.utils.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;

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
		CardList spells = desc.getFilteredCards(context, player, source);
		TargetPlayer castingTargetPlayer = desc.getTargetPlayer() == null ? TargetPlayer.OWNER : desc.getTargetPlayer();

		player.setAttribute(Attribute.RANDOM_CHOICES);

		int numberOfSpellsToCast = desc.getValue(SpellArg.VALUE, context, player, target, source, 1);
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
