package net.demilich.metastone.game.events;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.trigger.Enchantment;
import net.demilich.metastone.game.targeting.EntityReference;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * A trigger was fired from the specified enchantment.
 */
public final class TriggerFired implements Notification {

	private final WeakReference<Enchantment> enchantment;
	private final EntityReference eventTarget;
	private final WeakReference<Entity> source;

	public TriggerFired(GameContext context, Enchantment enchantment) {
		this.enchantment = new WeakReference<>(enchantment);
		this.source = new WeakReference<>(context.resolveSingleTarget(enchantment.getHostReference()));
		this.eventTarget = context.getEventTargetStack().peek();
	}

	public Enchantment getEnchantment() {
		return enchantment.get();
	}

	@Override
	public Entity getSource() {
		return source.get();
	}

	@Override
	public List<Entity> getTargets(GameContext context, int player) {
		return context.resolveTarget(context.getPlayer(player), getSource(), eventTarget);
	}

	@Override
	public boolean isPowerHistory() {
		return true;
	}

	@Override
	public String getDescription(GameContext context, int playerId) {
		Entity source = getSource();
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

	@Override
	public boolean isClientInterested() {
		return true;
	}
}

