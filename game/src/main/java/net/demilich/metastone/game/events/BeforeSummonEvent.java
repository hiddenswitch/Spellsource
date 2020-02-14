package net.demilich.metastone.game.events;

import com.hiddenswitch.spellsource.client.models.GameEvent;
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
	public GameEvent.EventTypeEnum getEventType() {
		return com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum.BEFORE_SUMMON;
	}
}
