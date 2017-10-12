package net.demilich.metastone.game.spells.desc.filter;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.TargetPlayer;

import java.io.Serializable;

public abstract class EntityFilter implements Serializable {
	protected final FilterDesc desc;

	public FilterDesc getDesc() {
		return desc;
	}

	public EntityFilter(FilterDesc desc) {
		this.desc = desc;
	}

	public Object getArg(FilterArg arg) {
		return desc.get(arg);
	}

	public boolean hasArg(FilterArg arg) {
		return desc.containsKey(arg);
	}

	public boolean matches(GameContext context, Player player, Entity entity) {
		boolean invert = desc.getBool(FilterArg.INVERT);
		TargetPlayer targetPlayer = (TargetPlayer) desc.get(FilterArg.TARGET_PLAYER);
		if (targetPlayer == null) {
			targetPlayer = TargetPlayer.SELF;
		}
		Player providingPlayer = null;
		switch (targetPlayer) {
			case ACTIVE:
				providingPlayer = context.getActivePlayer();
				break;
			case BOTH:
				boolean test = false;
				for (Player selectedPlayer : context.getPlayers()) {
					test |= (this.test(context, selectedPlayer, entity) != invert);
				}
				return test;
			case INACTIVE:
				providingPlayer = context.getOpponent(context.getActivePlayer());
				break;
			case OPPONENT:
				providingPlayer = context.getOpponent(player);
				break;
			case OWNER:
				providingPlayer = context.getPlayer(entity.getOwner());
				break;
			case SELF:
			default:
				providingPlayer = player;
				break;
		}
		return this.test(context, providingPlayer, entity) != invert;
	}

	protected abstract boolean test(GameContext context, Player player, Entity entity);

	@Override
	public boolean equals(Object other) {
		if (other == null) {
			return false;
		}
		if (!EntityFilter.class.isAssignableFrom(other.getClass())) {
			return false;
		}
		EntityFilter rhs = (EntityFilter) other;
		if ((desc == null) != (rhs.desc == null)) {
			return false;
		}
		return desc == null || desc.equals(rhs.desc);
	}

}
