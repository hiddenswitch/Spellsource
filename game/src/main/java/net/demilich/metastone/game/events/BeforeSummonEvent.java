package net.demilich.metastone.game.events;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.actions.BattlecryAction;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import org.jetbrains.annotations.NotNull;

public final class BeforeSummonEvent extends SummonEvent {

	public BeforeSummonEvent(@NotNull GameContext context, @NotNull Actor minion, @NotNull Entity source, boolean didResolveBattlecry, BattlecryAction battlecryAction) {
		super(context, minion, source, didResolveBattlecry, battlecryAction);
	}

	@Override
	public GameEventType getEventType() {
		return GameEventType.BEFORE_SUMMON;
	}
}
