package net.demilich.metastone.game.spells.desc.condition;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.SpellUtils;
import net.demilich.metastone.game.spells.desc.filter.ComparisonOperation;

/**
 * {@code true} if all the targets' modified mana costs (as per {@link net.demilich.metastone.game.logic.GameLogic#getModifiedManaCost(Player,
 * Card)} satisfies the {@link ConditionArg#OPERATION} with the {@link ConditionArg#VALUE}.
 * <p>
 * When used as a {@code fireCondition} or {@code queueCondition} on an event trigger, the {@code target} parameter
 * comes from {@code event.getTarget()} (the default). The source/target mapping varies by event type:
 * <ul>
 *     <li>{@link net.demilich.metastone.game.events.CardPlayedEvent}: {@code getSource()} = Player entity,
 *     {@code getTarget()} = the played card. The default target is the card, so no override is needed.</li>
 *     <li>{@link net.demilich.metastone.game.events.AfterSpellCastedEvent}: {@code getSource()} = the spell card,
 *     {@code getTarget()} = the spell's target entity (may be {@code null} for untargeted spells). Use
 *     {@code "target": "EVENT_SOURCE"} to check the cast spell's cost.</li>
 * </ul>
 * <p>
 * The Corrupt mechanic (Madness at the Darkmoon Faire) uses a
 * {@link net.demilich.metastone.game.spells.trigger.CardPlayedTrigger} with a {@code ManaCostCondition}
 * to detect when a higher-cost card is played. These cards should NOT specify a target override — the default
 * target ({@code event.getTarget()} = the played card) is correct.
 *
 * @see SpellUtils#evaluateOperation(ComparisonOperation, int, int) for more about comparisons
 */
public class ManaCostCondition extends Condition {

	public ManaCostCondition(ConditionDesc desc) {
		super(desc);
	}

	@Override
	protected boolean isFulfilled(GameContext context, Player player, ConditionDesc desc, Entity source, Entity target) {
		if (!(target instanceof Card)) {
			target = target.getSourceCard();
		}

		var card = (Card) target;
		var value = desc.getValue(ConditionArg.VALUE, context, player, target, source, 0);
		var operation = (ComparisonOperation) desc.getOrDefault(ConditionArg.OPERATION, ComparisonOperation.EQUAL);
		return SpellUtils.evaluateOperation(operation, context.getLogic().getModifiedManaCost(player, card), value);
	}

	@Override
	protected boolean multipleTargetsEvaluatedAsAnd() {
		return true;
	}

	@Override
	protected boolean multipleTargetsEvaluatedAsOr() {
		return false;
	}
}
