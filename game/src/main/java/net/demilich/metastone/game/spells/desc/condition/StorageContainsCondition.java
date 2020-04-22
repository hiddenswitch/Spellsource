package net.demilich.metastone.game.spells.desc.condition;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.custom.EnvironmentEntityList;

/**
 * {@code true} if the {@link EnvironmentEntityList} stored on the {@code source} contains any of the {@code target}
 * entities.
 */
public class StorageContainsCondition extends Condition {
	public StorageContainsCondition(ConditionDesc desc) {
		super(desc);
	}

	@Override
	protected boolean isFulfilled(GameContext context, Player player, ConditionDesc desc, Entity source, Entity target) {
		var storage = EnvironmentEntityList.getList(context).getCards(context, source);
		for (Card card : storage) {
			if (target.getSourceCard().getCardId().equals(card.getCardId())) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected boolean multipleTargetsEvaluatedAsOr() {
		return true;
	}

	@Override
	protected boolean multipleTargetsEvaluatedAsAnd() {
		return false;
	}
}
