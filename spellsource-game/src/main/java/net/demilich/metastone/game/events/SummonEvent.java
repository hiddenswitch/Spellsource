package net.demilich.metastone.game.events;

import com.hiddenswitch.spellsource.rpc.Spellsource.GameEventTypeMessage.GameEventType;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.actions.OpenerAction;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import org.jetbrains.annotations.NotNull;

/**
 * A minion was summoned and its openers were resolved if {@link #isResolvedOpener()} is {@code true}.
 */
public class SummonEvent extends CardEvent {

	private final boolean resolvedOpener;
	private final OpenerAction[] openerActions;

	public SummonEvent(@NotNull GameContext context, @NotNull Actor minion, @NotNull Entity source, boolean resolvedOpener, OpenerAction... openerActions) {
		super(GameEventType.SUMMON, true, context, context.getPlayer(source.getOwner()), source, minion, source.getSourceCard());
		this.resolvedOpener = resolvedOpener;
		this.openerActions = openerActions;
	}

	SummonEvent(GameEventType eventType, GameContext context, Actor minion, Entity source, boolean resolvedOpener, OpenerAction[] openerActions) {
		this(eventType, true, context, minion, source, resolvedOpener, openerActions);
	}

	SummonEvent(GameEventType eventType, boolean isClientInterested, GameContext context, Actor minion, Entity source, boolean didResolveOpener, OpenerAction[] openerActions) {
		super(eventType, isClientInterested, context, context.getPlayer(source.getOwner()), source, minion, source.getSourceCard());
		this.resolvedOpener = didResolveOpener;
		this.openerActions = openerActions;
	}

	public boolean isResolvedOpener() {
		return resolvedOpener;
	}

	public OpenerAction[] getOpenerActions() {
		return openerActions;
	}
}
