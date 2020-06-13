package net.demilich.metastone.game.spells.desc.filter;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.desc.Desc;
import net.demilich.metastone.game.cards.desc.HasDesc;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.TargetPlayer;
import net.demilich.metastone.game.spells.desc.condition.Condition;
import net.demilich.metastone.game.targeting.EntityReference;

import java.io.Serializable;
import java.util.List;
import java.util.function.Predicate;

/**
 * Filters {@link Entity} objects using its {@link #test(GameContext, Player, Entity, Entity)} implementation.
 */
public abstract class EntityFilter implements Serializable, HasDesc<EntityFilterDesc> {
	private EntityFilterDesc desc;

	@Override
	public EntityFilterDesc getDesc() {
		return desc;
	}

	public EntityFilter(EntityFilterDesc desc) {
		this.desc = desc;
	}

	public Object getArg(EntityFilterArg arg) {
		return getDesc().get(arg);
	}

	public boolean hasArg(EntityFilterArg arg) {
		return getDesc().containsKey(arg);
	}

	public <T extends Entity> Predicate<T> matcher(GameContext context, Player player, Entity host) {
		return (card) -> matches(context, player, card, host);
	}

	/**
	 * A method that calls the subclass's {@link #test(GameContext, Player, Entity, Entity)} implementation that
	 * determines whether or not a given {@code entity} matches the filter.
	 * <p>
	 * {@link EntityFilterArg#TARGET_PLAYER} is interpreted as the point of view from which the {@link #test(GameContext,
	 * Player, Entity, Entity)} call is evaluated.
	 * <p>
	 * If an {@link EntityFilterArg#AND_CONDITION} is specified, the filter's test <b>and</b> the condition must pass.
	 * Works The condition uses the {@code player} passed into this function, not the {@link
	 * EntityFilterArg#TARGET_PLAYER} that is potentially specified on this filter. for any filter. The condition uses the
	 * {@code player} passed into this function, not the {@link EntityFilterArg#TARGET_PLAYER} that is potentially
	 * specified on this filter.
	 *
	 * @param context
	 * @param player
	 * @param entity
	 * @param host
	 * @return {@code true} if the {@code entity} passes the filter, otherwise {@code false}.
	 */
	@Suspendable
	public boolean matches(GameContext context, Player player, Entity entity, Entity host) {
		boolean invert = getDesc().getBool(EntityFilterArg.INVERT);
		TargetPlayer targetPlayer = (TargetPlayer) getDesc().get(EntityFilterArg.TARGET_PLAYER);
		if (targetPlayer == null) {
			targetPlayer = TargetPlayer.SELF;
		}
		Player providingPlayer = null;
		switch (targetPlayer) {
			case ACTIVE:
				providingPlayer = context.getActivePlayer();
				break;
			case BOTH:
				boolean testBoth = true;
				for (Player selectedPlayer : context.getPlayers()) {
					testBoth &= (this.test(context, selectedPlayer, entity, host) != invert);
				}
				return testBoth;
			case EITHER:
				boolean testEither = false;
				for (Player selectedPlayer : context.getPlayers()) {
					testEither |= (this.test(context, selectedPlayer, entity, host) != invert);
				}
				return testEither;
			case INACTIVE:
				providingPlayer = context.getOpponent(context.getActivePlayer());
				break;
			case OPPONENT:
				providingPlayer = context.getOpponent(player);
				break;
			case OWNER:
				providingPlayer = context.getPlayer(entity.getOwner());
				break;
			case PLAYER_1:
				providingPlayer = context.getPlayer1();
				break;
			case PLAYER_2:
				providingPlayer = context.getPlayer2();
				break;
			case SELF:
			default:
				providingPlayer = player;
				break;
		}

		boolean passesCondition = true;
		if (getDesc().containsKey(EntityFilterArg.AND_CONDITION)) {
			Condition condition = (Condition) getDesc().get(EntityFilterArg.AND_CONDITION);
			passesCondition = condition.isFulfilled(context, player, host, entity);
		}
		return passesCondition
				&& this.test(context, providingPlayer, entity, host) != invert;
	}

	/**
	 * The subclasses of this class implement this method to actually perform the logic of the filtering. Observe that
	 * results from filtering other entities are not available here; this function is stateless in the sense that an
	 * earlier acceptance or rejection of an entity cannot influence the acceptance or rejection of a current entity.
	 *
	 * @param context
	 * @param player
	 * @param entity
	 * @param host
	 * @return
	 */
	@Suspendable
	protected abstract boolean test(GameContext context, Player player, Entity entity, Entity host);

	@Override
	public boolean equals(Object other) {
		if (other == null) {
			return false;
		}
		if (!EntityFilter.class.isAssignableFrom(other.getClass())) {
			return false;
		}
		EntityFilter rhs = (EntityFilter) other;
		if ((getDesc() == null) != (rhs.getDesc() == null)) {
			return false;
		}
		return getDesc() == null || getDesc().equals(rhs.getDesc());
	}

	protected List<Entity> getTargetedEntities(GameContext context, Player player, Entity host) {
		EntityReference targetReference = (EntityReference) getDesc().get(EntityFilterArg.TARGET);
		List<Entity> entities = null;
		if (targetReference != null) {
			entities = context.resolveTarget(player, host, targetReference);
		}
		return entities;
	}

	@Override
	public void setDesc(Desc<?, ?> desc) {
		this.desc = (EntityFilterDesc) desc;
	}
}

