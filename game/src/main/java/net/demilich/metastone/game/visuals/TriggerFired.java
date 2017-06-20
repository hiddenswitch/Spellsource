package net.demilich.metastone.game.visuals;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.events.Notification;
import net.demilich.metastone.game.spells.trigger.SpellTrigger;

public class TriggerFired implements Notification {
	private final GameContext context;
	private final SpellTrigger spellTrigger;

	public TriggerFired(GameContext context, SpellTrigger spellTrigger) {
		this.context = context;
		this.spellTrigger = spellTrigger;
	}

	@Override
	public GameContext getGameContext() {
		return context;
	}

	public SpellTrigger getSpellTrigger() {
		return spellTrigger;
	}
}
