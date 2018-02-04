package net.demilich.metastone.game.behaviour;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.cards.Card;

import java.util.*;

public class PlayRandomBehaviour extends AbstractBehaviour {

	private Random random = new Random();

	@Override
	public String getName() {
		return "Play Random";
	}

	@Override
	public List<Card> mulligan(GameContext context, Player player, List<Card> cards) {
		return new ArrayList<>(randomSubset(cards, getRandom(context).nextInt(3) + 1, getRandom(context)));
	}

	protected Random getRandom(GameContext context) {
		return random;
	}

	@Override
	public GameAction requestAction(GameContext context, Player player, List<GameAction> validActions) {
		if (validActions.size() == 1) {
			return validActions.get(0);
		}

		int randomIndex = getRandom(context).nextInt(validActions.size());
		return validActions.get(randomIndex);
	}

	public <T> Set<T> randomSubset(List<T> items, int m, Random random) {
		HashSet<T> res = new HashSet<T>(m);
		int n = items.size();
		for (int i = n - m; i < n; i++) {
			int pos = random.nextInt(i + 1);
			T item = items.get(pos);
			if (res.contains(item))
				res.add(items.get(i));
			else
				res.add(item);
		}
		return res;
	}

}
