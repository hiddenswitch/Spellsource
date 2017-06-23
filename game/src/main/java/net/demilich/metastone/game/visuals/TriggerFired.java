package net.demilich.metastone.game.visuals;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.Notification;
import net.demilich.metastone.game.spells.trigger.SpellTrigger;
import net.demilich.metastone.game.targeting.EntityReference;

import java.util.List;

public class TriggerFired implements Notification {
	private final SpellTrigger spellTrigger;
	private final EntityReference eventTarget;

	public TriggerFired(GameContext context, SpellTrigger spellTrigger) {
		this.spellTrigger = spellTrigger;
		this.eventTarget = context.getEventTargetStack().peek();
	}


	public SpellTrigger getSpellTrigger() {
		return spellTrigger;
	}

	@Override
	public Entity getSource(GameContext context) {
		return context.resolveSingleTarget(spellTrigger.getHostReference());
	}

	@Override
	public List<Entity> getTargets(GameContext context, int player) {
		return context.resolveTarget(context.getPlayer(player), getSource(context), eventTarget);
	}

	@Override
	public boolean isPowerHistory() {
		return true;
	}

	@Override
	public String getDescription(GameContext context, int playerId) {
		Entity source = getSource(context);
		List<Entity> targets = getTargets(context, playerId);
		Entity target = targets == null ? null : (targets.size() == 0 ? null : targets.get(0));
		if (source != null && target != null) {
			if (target.getName() != null && source.getName() != null) {
				return String.format("%s triggered %s's ability.", target.getName(), source.getName());
			}
		} else if (source == null && target != null) {
			if (target.getName() != null) {
				return String.format("%s triggered these effects.", target.getName());
			}
		} else if (source != null) {
			if (source.getName() != null) {
				return String.format("%s's ability was triggered.", source.getName());
			}
		}
		return "An ability was triggered.";
	}
}
