package net.demilich.metastone.game.spells.desc.filter;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.TargetPlayer;

import java.util.Objects;

/**
 * Matches a minion if it was summoned by a entity whose owner was {@link EntityFilterArg#TARGET_PLAYER}.
 */
public class SummonedByPlayerFilter extends EntityFilter {

	public SummonedByPlayerFilter(EntityFilterDesc desc) {
		super(desc);
	}

	@Override
	protected boolean test(GameContext context, Player player, Entity entity, Entity host) {
		int playerId;
		switch ((TargetPlayer) getDesc().get(EntityFilterArg.TARGET_PLAYER)) {
			case OPPONENT:
				playerId = context.getOpponent(player).getId();
				break;
			case OWNER:
				playerId = host.getId();
				break;
			case ACTIVE:
				playerId = context.getActivePlayerId();
				break;
			case INACTIVE:
				playerId = context.getNonActivePlayerId();
				break;
			case PLAYER_1:
				playerId = GameContext.PLAYER_1;
				break;
			case PLAYER_2:
				playerId = GameContext.PLAYER_2;
				break;
			default:
			case SELF:
				playerId = player.getId();
				break;
			case BOTH:
				throw new UnsupportedOperationException("SummonedByPlayerFilter cannot support TargetPlayer.BOTH"); }
		return Objects.equals(entity.getAttributes().getOrDefault(Attribute.SUMMONED_BY_PLAYER, -1), playerId);
	}
}
