package net.demilich.metastone.game.events;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.actions.BattlecryAction;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import org.jetbrains.annotations.NotNull;

public class SummonEvent extends GameEvent implements HasCard {

	private final boolean resolvedBattlecry;
	private final BattlecryAction battlecryAction;
	private final Actor minion;
	private final Entity source;

	public SummonEvent(@NotNull GameContext context, @NotNull Actor minion, @NotNull Entity source, boolean resolvedBattlecry, BattlecryAction battlecryAction) {
		super(context, minion.getOwner(), source.getOwner());
		this.minion = minion;
		this.source = source;
		this.resolvedBattlecry = resolvedBattlecry;
		this.battlecryAction = battlecryAction;
	}

	@Override
	@NotNull
	public Entity getEventTarget() {
		return getMinion();
	}

	@Override
	@NotNull
	public Entity getEventSource() {
		return getSource();
	}

	@Override
	public GameEventType getEventType() {
		return GameEventType.SUMMON;
	}

	@NotNull
	public Actor getMinion() {
		return minion;
	}

	@NotNull
	public Entity getSource() {
		return source;
	}

	@Override
	public boolean isClientInterested() {
		return true;
	}

	@Override
	@NotNull
	public Card getCard() {
		return source.getSourceCard();
	}

	public boolean isResolvedBattlecry() {
		return resolvedBattlecry;
	}

	public BattlecryAction getBattlecryAction() {
		return battlecryAction;
	}
}
