package net.demilich.metastone.game.spells.desc.filter;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.TargetPlayer;
import net.demilich.metastone.game.spells.desc.condition.ConditionArg;

public class DeckContainsFilter extends EntityFilter {
	public DeckContainsFilter(EntityFilterDesc desc) {
		super(desc);
	}

	@Override
	protected boolean test(GameContext context, Player player, Entity entity, Entity host) {
		EntityFilterDesc desc = getDesc();
		if (desc.containsKey(EntityFilterArg.TARGET_PLAYER)) {
			switch ((TargetPlayer) desc.get(EntityFilterArg.TARGET_PLAYER)) {
				case OPPONENT:
					player = context.getOpponent(player);
					break;
				default:
					break;
			}
		}
		for (Card card : player.getDeck()) {
			if (entity.getSourceCard().getCardId().equalsIgnoreCase(card.getCardId())) {
				return true;
			}
		}
		return false;
	}
}
