package net.demilich.metastone.game.spells.aura;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.GameEvent;
;
import net.demilich.metastone.game.spells.desc.aura.AuraArg;
import net.demilich.metastone.game.spells.desc.aura.AuraDesc;
import net.demilich.metastone.game.targeting.EntityReference;

import java.util.HashMap;
import java.util.Map;

/**
 * This class maintains a stateful aura for an {@link Attribute}, increasing or decreasing its value by the appropriate
 * amount whenever the main aura triggers ({@link net.demilich.metastone.game.spells.trigger.WillEndSequenceTrigger} and
 * {@link net.demilich.metastone.game.spells.trigger.BoardChangedTrigger}) fire and any additional triggers in {@link
 * AuraArg#TRIGGERS} fire. The target value that should be contributed by this aura to the attribute's value is
 * determined by {@link AuraArg#VALUE} at the time that any of its triggers fire.
 * <p>
 * For example, suppose you want to provide an attack bonus that changes over time: "Has +1 Attack for each other
 * friendly minion on the battlefield." If you used an {@link BuffAura}, the amount that the buff increases may differ
 * from thea mount the buff decreases throughout the lifetime of the aura, due to the consequences of recalculating the
 * number of minions on the battlefield at different times. This would be incorrect. Instead, use this aura:
 * <pre>
 *   "aura": {
 *     "class": "StatefulAttributeValueAura",
 *     "attribute": "AURA_ATTACK_BONUS",
 *     "value": {
 *       "class": "EntityCountValueProvider",
 *       "target": "OTHER_FRIENDLY_MINIONS"
 *     },
 *     "target": "SELF"
 *   }
 * </pre>
 * Observe that the amount that should be buffed is equal to the value.
 * <p>
 * Consider this more complex example: "Has +1 Attack for each damage your hero has taken this turn." This value should
 * be reevaluated whenever a turn starts and whenever the owner's hero has taken damage. We'll add triggers to the
 * {@link AuraArg#TRIGGERS} array to cause the aura to update whenever those events occur.
 * <pre>
 *   "aura": {
 *     "class": "StatefulAttributeValueAura",
 *     "attribute": "AURA_ATTACK_BONUS",
 *     "value": {
 *       "class": "AttributeValueProvider",
 *       "attribute": "DAMAGE_THIS_TURN",
 *       "target": "FRIENDLY_HERO"
 *     },
 *     "target": "SELF",
 *     "triggers": [
 *       {
 *         "class": "TurnStartTrigger",
 *         "targetPlayer": "BOTH"
 *       },
 *       {
 *         "class": "DamageReceivedTrigger",
 *         "targetEntityType": "HERO",
 *         "targetPlayer": "SELF"
 *       }
 *     ]
 *   }
 * </pre>
 */
public final class StatefulAttributeValueAura extends EffectlessAura {

	private Map<Integer, Integer> currentValues = new HashMap<>();

	public StatefulAttributeValueAura(AuraDesc desc) {
		super(desc);
	}

	@Override
	public StatefulAttributeValueAura clone() {
		StatefulAttributeValueAura clone = (StatefulAttributeValueAura) super.clone();
		clone.currentValues = new HashMap<>(currentValues);
		return clone;
	}

	@Override
	public void onGameEvent(GameEvent event) {
		super.onGameEvent(event);
		GameContext context = event.getGameContext();
		Entity host = context.resolveSingleTarget(getHostReference());

		for (int affectedEntity : getAffectedEntities()) {
			Entity target = context.resolveSingleTarget(new EntityReference(affectedEntity));
			int targetValue = getDesc().getValue(AuraArg.VALUE, context, context.getPlayer(getOwner()), target, host, 0);
			int currentValue = currentValues.get(affectedEntity);
			if (currentValue != targetValue) {
				target.modifyAttribute(getAttribute(), targetValue - currentValue);
				currentValues.put(affectedEntity, targetValue);
			}
		}
	}

	@Override
	protected void applyAuraEffect(GameContext context, Entity target) {
		currentValues.put(target.getId(), 0);
	}

	@Override
	protected void removeAuraEffect(GameContext context, Entity target) {
		// This will sometimes be applied to targets that have been removed from play, which is the correct behaviour!
		target.modifyAttribute(getAttribute(), -currentValues.get(target.getId()));
		currentValues.remove(target.getId());
	}

	protected Attribute getAttribute() {
		return getDesc().getAttribute();
	}
}
