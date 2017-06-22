package net.demilich.metastone.game.visuals;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.Notification;
import net.demilich.metastone.game.spells.trigger.SpellTrigger;
import net.demilich.metastone.game.targeting.EntityReference;

public class TriggerFired implements Notification {
	private final GameContext context;
	private final SpellTrigger spellTrigger;
	private final EntityReference eventTarget;

	public TriggerFired(GameContext context, SpellTrigger spellTrigger) {
		this.context = context;
		this.spellTrigger = spellTrigger;
		this.eventTarget = context.getEventTargetStack().peek();
	}

	public GameContext getGameContext() {
		return context;
	}

	/**
	 * The source of a {@link TriggerFired} notification is the entity that's hosting the trigger.
	 *
	 * @return A reference.
	 */
	@Override
	public EntityReference getSourceReference() {
		return spellTrigger.getHostReference();
	}

	/**
	 * If performing a specific game action / event caused this trigger to fire, this reference points to the entity
	 * associated with that game action / event.
	 *
	 * @return A reference to an entity that caused this trigger to fire.
	 */
	@Override
	public EntityReference getTargetReference() {
		return eventTarget;
	}

	public SpellTrigger getSpellTrigger() {
		return spellTrigger;
	}

	@Override
	public boolean isPowerHistory() {
		return true;
	}

	@Override
	public String getDescription(GameContext context, int playerId) {
		if (getSourceReference() != null && getTargetReference() != null) {
			final Entity target = this.context.resolveSingleTarget(getTargetReference());
			final Entity source = this.context.resolveSingleTarget(getSourceReference());
			if (target != null && source != null && target.getName() != null && source.getName() != null) {
				return String.format("%s triggered %s's ability.", target.getName(), source.getName());
			}
		} else if (getSourceReference() == null && getTargetReference() != null) {
			final Entity target = this.context.resolveSingleTarget(getTargetReference());
			if (target != null && target.getName() != null) {
				return String.format("%s triggered these effects.", target.getName());
			}
		} else if (getSourceReference() != null && getTargetReference() == null) {
			final Entity source = this.context.resolveSingleTarget(getSourceReference());
			if (source != null && source.getName() != null) {
				return String.format("%s's ability was triggered.", source.getName());
			}
		}
		return "An ability was triggered.";
	}
}
