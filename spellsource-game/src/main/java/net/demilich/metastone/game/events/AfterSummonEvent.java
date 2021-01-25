package net.demilich.metastone.game.events;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.actions.OpenerAction;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import org.jetbrains.annotations.NotNull;

import static com.hiddenswitch.spellsource.rpc.Spellsource.GameEventTypeMessage.GameEventType.AFTER_SUMMON;

/**
 * The minion is on the board and its enchantments are in play.
 */
public final class AfterSummonEvent extends SummonEvent {

	public AfterSummonEvent(@NotNull GameContext context, @NotNull Actor minion, @NotNull Entity source, boolean didResolveBattlecry, OpenerAction... openerActions) {
		super(AFTER_SUMMON, context, minion, source, didResolveBattlecry, openerActions);
	}
}
