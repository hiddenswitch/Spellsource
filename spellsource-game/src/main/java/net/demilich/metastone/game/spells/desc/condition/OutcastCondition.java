package net.demilich.metastone.game.spells.desc.condition;

import com.hiddenswitch.spellsource.rpc.Spellsource.ZonesMessage.Zones;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.entities.Entity;

/**
 * Evaluates to {@code true} if the source card was played from the leftmost or rightmost position in hand
 * (i.e. the Outcast bonus should trigger).
 * <p>
 * This checks the {@link Attribute#OUTCAST_TRIGGERED} attribute, which is set by the engine in
 * {@link net.demilich.metastone.game.logic.GameLogic#playCard} when an OUTCAST card is played from the edge of hand.
 * <p>
 * When evaluated against a card still in hand (e.g. for cost modifier glow), it checks the card's current
 * position in the hand zone directly.
 */
public final class OutcastCondition extends Condition {

	public OutcastCondition(ConditionDesc desc) {
		super(desc);
	}

	@Override
	protected boolean isFulfilled(GameContext context, Player player, ConditionDesc desc, Entity source, Entity target) {
		// If already played, check the triggered attribute
		if (source.hasAttribute(Attribute.OUTCAST_TRIGGERED)) {
			return true;
		}
		// If still in hand, check current position
		if (source.getZone() == Zones.HAND) {
			var index = source.getEntityLocation().getIndex();
			var handSize = player.getHand().getCount();
			return index == 0 || index == handSize - 1;
		}
		return false;
	}

	@Override
	protected boolean singleTargetOnly() {
		return true;
	}
}
