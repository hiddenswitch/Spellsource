package net.demilich.metastone.game.actions;

import com.hiddenswitch.spellsource.rpc.Spellsource.ActionTypeMessage.ActionType;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.targeting.TargetSelection;

import java.util.Collections;
import java.util.List;

/**
 * This action ends the player's current turn.
 * <p>
 * Sometimes, the action is not available. This is typically due to a pending {@link DiscoverAction} or {@link
 * OpenerAction}.
 */
public class EndTurnAction extends GameAction {

	private int playerId;

	public EndTurnAction(int playerId) {
		this.playerId = playerId;
		setActionType(ActionType.END_TURN);
		setTargetRequirement(TargetSelection.NONE);
	}

	@Override
	public EndTurnAction clone() {
		return (EndTurnAction) super.clone();
	}

	@Override
	public void execute(GameContext context, int playerId) {
		context.endTurn();
	}

	@Override
	public Entity getSource(GameContext context) {
		return context.getPlayer(playerId).getHero();
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
