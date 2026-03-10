package net.demilich.metastone.game.spells.desc.valueprovider;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.targeting.EntityReference;

import java.util.Map;

/**
 * Provides the modified mana cost of the {@code target} entity's source card, as per
 * {@link net.demilich.metastone.game.logic.GameLogic#getModifiedManaCost(net.demilich.metastone.game.Player,
 * net.demilich.metastone.game.cards.Card)}.
 * <p>
 * When used inside a trigger's value provider, the {@code target} comes from event resolution. The source/target
 * mapping varies by event type:
 * <ul>
 *     <li>{@link net.demilich.metastone.game.events.CardPlayedEvent}: {@code getSource()} = Player entity,
 *     {@code getTarget()} = the played card. The default target is the card, so no override is needed.</li>
 *     <li>{@link net.demilich.metastone.game.events.SummonEvent} family (BeforeSummon, Summon, AfterSummon):
 *     {@code getSource()} = source card, {@code getTarget()} = summoned minion.
 *     Use {@code "target": "EVENT_SOURCE"} to check the played card's cost.</li>
 *     <li>{@link net.demilich.metastone.game.events.AfterSpellCastedEvent}: {@code getSource()} = spell card,
 *     {@code getTarget()} = spell's target entity. Use {@code "target": "EVENT_SOURCE"} to check the cast
 *     spell's cost.</li>
 * </ul>
 */
public class ManaCostProvider extends ValueProvider {

	public ManaCostProvider(ValueProviderDesc desc) {
		super(desc);
	}

	public static ValueProviderDesc create() {
		Map<ValueProviderArg, Object> arguments = ValueProviderDesc.build(ManaCostProvider.class);
		return new ValueProviderDesc(arguments);
	}

	@Override
	protected int provideValue(GameContext context, Player player, Entity target, Entity host) {
		EntityReference targetOverride = (EntityReference) getDesc().get(ValueProviderArg.TARGET);
		if (targetOverride != null) {
			var entities = context.resolveTarget(player, host, targetOverride);
			if (entities.size() == 0) {
				return 0;
			}
			target = entities.get(0);
		}
		Card targetCard = target.getSourceCard();
		return context.getLogic().getModifiedManaCost(player, targetCard);
	}
}
