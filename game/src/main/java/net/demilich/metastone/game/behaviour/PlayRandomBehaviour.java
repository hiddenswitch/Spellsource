package net.demilich.metastone.game.behaviour;

import java.util.*;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.cards.Card;
import org.apache.commons.lang3.RandomUtils;

public class PlayRandomBehaviour extends AbstractBehaviour {

	private Random random = new Random();

	@Override
	public String getName() {
		return "Play Random";
	}

	@Override
	public List<Card> mulligan(GameContext context, Player player, List<Card> cards) {
		return new ArrayList<>(randomSubset(cards, RandomUtils.nextInt(1, 4)));
	}

	@Override
	public GameAction requestAction(GameContext context, Player player, List<GameAction> validActions) {
		if (validActions.size() == 1) {
			return validActions.get(0);
		}

		int randomIndex = random.nextInt(validActions.size());
		GameAction randomAction = validActions.get(randomIndex);
		return randomAction;
	}

	public <T> Set<T> randomSubset(List<T> items, int m) {
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
