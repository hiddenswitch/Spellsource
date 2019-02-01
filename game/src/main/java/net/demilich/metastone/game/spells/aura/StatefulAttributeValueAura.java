package net.demilich.metastone.game.spells.aura;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.spells.NullSpell;
import net.demilich.metastone.game.spells.desc.aura.AuraArg;
import net.demilich.metastone.game.spells.desc.aura.AuraDesc;
import net.demilich.metastone.game.targeting.EntityReference;

import java.util.HashMap;
import java.util.Map;

public final class StatefulAttributeValueAura extends Aura {

	private Map<Integer, Integer> currentValues = new HashMap<>();

	public StatefulAttributeValueAura(AuraDesc desc) {
		super(desc);
		includeExtraTriggers(desc);
		applyAuraEffect = NullSpell.create();
		removeAuraEffect = NullSpell.create();
	}

	@Override
	public StatefulAttributeValueAura clone() {
		StatefulAttributeValueAura clone = (StatefulAttributeValueAura) super.clone();
		clone.currentValues = new HashMap<>(currentValues);
		return clone;
	}

	@Override
	@Suspendable
	public void onGameEvent(GameEvent event) {
		super.onGameEvent(event);

		GameContext context = event.getGameContext();
		Entity host = context.resolveSingleTarget(getHostReference());

		for (Integer affectedEntity : getAffectedEntities()) {
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
		target.modifyAttribute(getAttribute(), -currentValues.get(target.getId()));
		currentValues.remove(target.getId());
	}

	protected Attribute getAttribute() {
		return getDesc().getAttribute();
	}
}
