package net.demilich.metastone.game.spells.desc.valueprovider;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.TargetPlayer;

import java.io.Serializable;

public abstract class ValueProvider implements Serializable {
	protected final ValueProviderDesc desc;

	public ValueProvider(ValueProviderDesc desc) {
		this.desc = desc;
	}

	@Suspendable
	public int getValue(GameContext context, Player player, Entity target, Entity host) {
		TargetPlayer targetPlayer = (TargetPlayer) desc.get(ValueProviderArg.TARGET_PLAYER);
		if (targetPlayer == null) {
			targetPlayer = TargetPlayer.SELF;
		}
		Player providingPlayer = null;
		switch (targetPlayer) {
			case ACTIVE:
				providingPlayer = context.getActivePlayer();
				break;
			case BOTH:
				int multiplier = desc.containsKey(ValueProviderArg.MULTIPLIER) ? desc.getInt(ValueProviderArg.MULTIPLIER) : 1;
				int offset = desc.containsKey(ValueProviderArg.OFFSET) ? desc.getInt(ValueProviderArg.OFFSET) : 0;
				int value = 0;
				for (Player selectedPlayer : context.getPlayers()) {
					value += provideValue(context, selectedPlayer, target, host);
				}
				value = value * multiplier + offset;
				return value;
			case INACTIVE:
				providingPlayer = context.getOpponent(context.getActivePlayer());
				break;
			case OPPONENT:
				providingPlayer = context.getOpponent(player);
				break;
			case OWNER:
				providingPlayer = context.getPlayer(host.getOwner());
				break;
			case SELF:
			default:
				providingPlayer = player;
				break;
		}
		int multiplier = desc.containsKey(ValueProviderArg.MULTIPLIER) ? desc.getInt(ValueProviderArg.MULTIPLIER) : 1;
		int offset = desc.getValue(ValueProviderArg.OFFSET, context, player, target, host, 0);
		int value = provideValue(context, providingPlayer, target, host) * multiplier + offset;
		return value;
	}

	@Suspendable
	protected abstract int provideValue(GameContext context, Player player, Entity target, Entity host);
}
