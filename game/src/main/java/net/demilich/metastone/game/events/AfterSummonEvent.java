package net.demilich.metastone.game.events;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.actions.BattlecryAction;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import org.jetbrains.annotations.NotNull;

public final class AfterSummonEvent extends SummonEvent {

	public AfterSummonEvent(@NotNull GameContext context, @NotNull Actor minion, @NotNull Entity source, boolean didResolveBattlecry, BattlecryAction... battlecryActions) {
		super(context, minion, source, didResolveBattlecry, battlecryActions);
	}

	@Override
	public GameEventType getEventType() {
		return GameEventType.AFTER_SUMMON;
	}
}
