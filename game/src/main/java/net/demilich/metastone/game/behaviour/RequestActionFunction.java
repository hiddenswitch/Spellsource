package net.demilich.metastone.game.behaviour;

import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.spellsource.common.UtilityBehaviour;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.cards.Card;

import java.util.ArrayList;
import java.util.List;


public class RequestActionFunction extends UtilityBehaviour {
	private final T delegate;

	public RequestActionFunction(T delegate) {
		this.delegate = delegate;
	}

	@FunctionalInterface
	public interface T {
		@Suspendable
		GameAction requestAction(GameContext context, Player player, List<GameAction> validActions);
	}

	@Override
	public String getName() {
		return "Delegate to request action";
	}

	@Override
	public List<Card> mulligan(GameContext context, Player player, List<Card> cards) {
		return new ArrayList<>();
	}

	@Override
	@Suspendable
	public GameAction requestAction(GameContext context, Player player, List<GameAction> validActions) {
		return delegate.requestAction(context, player, validActions);
	}
}
