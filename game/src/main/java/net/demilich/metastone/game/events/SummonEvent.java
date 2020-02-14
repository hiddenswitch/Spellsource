package net.demilich.metastone.game.events;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.actions.BattlecryAction;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.HasCard;
import org.jetbrains.annotations.NotNull;

public class SummonEvent extends GameEvent implements HasCard {

	private final boolean resolvedBattlecry;
	private final BattlecryAction[] battlecryActions;
	private final Actor minion;
	private final Entity source;

	public SummonEvent(@NotNull GameContext context, @NotNull Actor minion, @NotNull Entity source, boolean resolvedBattlecry, BattlecryAction... battlecryActions) {
		super(context, minion.getOwner(), source.getOwner());
		this.minion = minion;
		this.source = source;
		this.resolvedBattlecry = resolvedBattlecry;
		this.battlecryActions = battlecryActions;
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
	public com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum getEventType() {
		return com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum.SUMMON;
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
	@NotNull
	public Card getSourceCard() {
		return source.getSourceCard();
	}

	public boolean isResolvedBattlecry() {
		return resolvedBattlecry;
	}

	public BattlecryAction[] getBattlecryActions() {
		return battlecryActions;
	}
}
