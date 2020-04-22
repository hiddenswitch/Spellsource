package net.demilich.metastone.game.spells.desc.condition;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.entities.Entity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@code true} if the {@link Entity#getSourceCard()} of this {@code source} was drawn the same turn it was played.
 */
public final class SurgeCondition extends Condition {

	private static Logger LOGGER = LoggerFactory.getLogger(SurgeCondition.class);

	public SurgeCondition(ConditionDesc desc) {
		super(desc);
	}

	@Override
	protected boolean isFulfilled(GameContext context, Player player, ConditionDesc desc, Entity source, Entity target) {
		if (source == null) {
			LOGGER.error("onCast: Source was null! Crazy!");
			throw new UnsupportedOperationException(new NullPointerException("source"));
		}
		if (source.getSourceCard() == null) {
			LOGGER.warn("onCast: Trying to evaluate surge on a card with no source card.");
			return false;
		}
		int receivedOnTurn = (int) source.getSourceCard().getAttributes().getOrDefault(Attribute.RECEIVED_ON_TURN, -1);
		return receivedOnTurn == context.getTurn();
	}

	@Override
	protected boolean targetConditionArgOverridesSuppliedTarget() {
		return false;
	}
}
