package net.demilich.metastone.gui.sandboxmode.actions;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.targeting.TargetSelection;

public class SetMaxManaAction extends GameAction {

	private final int targetPlayerId;
	private final int mana;

	public SetMaxManaAction(int playerId, int mana) {
		this.targetPlayerId = playerId;
		this.mana = mana;
		setTargetRequirement(TargetSelection.NONE);
	}

	@Override
	public void execute(GameContext context, int playerId) {
		Player player = context.getPlayer(targetPlayerId);
		player.setMaxMana(mana);
	}

	@Override
	public String getPromptText() {
		return "[SetMana]";
	}

	@Override
	public boolean isSameActionGroup(GameAction anotherAction) {
		return false;
	}

}
