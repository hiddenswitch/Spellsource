package net.demilich.metastone.game.actions;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.targeting.TargetSelection;

import java.util.Collections;
import java.util.List;

public class EndTurnAction extends GameAction {

	public EndTurnAction() {
		setActionType(ActionType.END_TURN);
		setTargetRequirement(TargetSelection.NONE);
	}

	@Override
	@Suspendable
	public void execute(GameContext context, int playerId) {
		context.endTurn();
	}

	@Override
	public String toString() {
		return String.format("[%s]", getActionType());
	}

	@Override
	public Entity getSource(GameContext context) {
		return context.getActivePlayer().getHero();
	}

	@Override
	public List<Entity> getTargets(GameContext context, int player) {
		return Collections.emptyList();
	}

	@Override
	public String getDescription(GameContext context, int playerId) {
		return String.format("%s ended their turn.", context.getActivePlayer().getName());
	}
}
