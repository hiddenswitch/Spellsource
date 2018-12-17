package net.demilich.metastone.game.spells.desc.valueprovider;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.EntityType;
import net.demilich.metastone.game.environment.Environment;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Returns the number of turns the specified player did not player a card.
 */
public final class TurnsNoCardPlayedValueProvider extends ValueProvider {

	private static final long serialVersionUID = 7924103729281642655L;

	public TurnsNoCardPlayedValueProvider(ValueProviderDesc desc) {
		super(desc);
	}

	@Override
	protected int provideValue(GameContext context, Player player, Entity target, Entity host) {
		long turnsTaken = player.getStatistics().getTurnsTaken();
		long turnsCardsPlayed = player
				.getStatistics()
				.getCardsPlayed()
				.values()
				.stream()
				.flatMapToInt(k -> k.keySet().stream().mapToInt(Integer::intValue))
				.distinct()
				.count();

		return (int) (turnsTaken - turnsCardsPlayed);
	}
}
