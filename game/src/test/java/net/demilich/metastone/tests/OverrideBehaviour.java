package net.demilich.metastone.tests;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.ActionType;
import net.demilich.metastone.game.actions.DiscoverAction;
import net.demilich.metastone.game.actions.GameAction;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class OverrideBehaviour extends TestBase.TestBehaviour {
	private final Function<List<DiscoverAction>, GameAction> chooser;
	private GameAction lastChoice;

	public OverrideBehaviour(Function<List<DiscoverAction>, GameAction> chooser) {
		super();
		this.chooser = chooser;
	}

	@Override
	public GameAction requestAction(GameContext context, Player player, List<GameAction> validActions) {
		if (validActions.stream().allMatch(ga -> ga.getActionType() == ActionType.DISCOVER)) {
			lastChoice = chooser.apply(validActions.stream().map(ga -> (DiscoverAction) ga).collect(Collectors.toList()));
			return getLastChoice();
		}
		return super.requestAction(context, player, validActions);
	}

	public GameAction getLastChoice() {
		return lastChoice;
	}
}
