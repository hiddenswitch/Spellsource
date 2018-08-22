package net.demilich.metastone.game.spells.desc.valueprovider;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.EntityType;
import net.demilich.metastone.game.utils.Attribute;

public class DeadMinionsThisTurn extends ValueProvider {

	public DeadMinionsThisTurn(ValueProviderDesc desc) {
		super(desc);
	}

	@Override
	@Suspendable
	protected int provideValue(GameContext context, Player player, Entity target, Entity host) {
		int currentTurn = context.getTurn();
		int count = 0;
		for (Entity deadEntity : player.getGraveyard()) {
			if (deadEntity.getEntityType() != EntityType.MINION) {
				continue;
			}

			if (deadEntity.getAttributeValue(Attribute.DIED_ON_TURN) == currentTurn) {
				count++;
			}

		}
		return count;
	}

}
