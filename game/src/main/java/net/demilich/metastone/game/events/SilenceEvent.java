package net.demilich.metastone.game.events;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;

/**
 * An {@link Actor} was silenced using {@link net.demilich.metastone.game.spells.SilenceSpell}.
 */
public class SilenceEvent extends BasicGameEvent {

	public SilenceEvent(GameContext context, int playerId, Actor target) {
		super(com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum.SILENCE, context, target, playerId, -1);
	}
}
