package net.demilich.metastone.game.events;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.actions.OpenerAction;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import org.jetbrains.annotations.NotNull;

import static com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum.BEFORE_SUMMON;

/**
 * Fires right after the minion has hit the board. Openers have not been resolved, enchantments have not been put into
 * play.
 * <p>
 * Another effect may have still transformed this minion. Transforming here will ensure the newly transformed minion's
 * enchantments written on its text come into play.
 */
public final class BeforeSummonEvent extends SummonEvent {

	public BeforeSummonEvent(@NotNull GameContext context, @NotNull Actor minion, @NotNull Entity source, boolean didResolveOpener, OpenerAction[] openerActions) {
		super(BEFORE_SUMMON, true, context, minion, source, didResolveOpener, openerActions);
	}
}
