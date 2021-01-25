package net.demilich.metastone.game.spells.desc.valueprovider;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.targeting.EntityReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

/**
 * Calculates the number of distinct tribes among the {@link net.demilich.metastone.game.targeting.EntityReference#TARGET}
 * entities.
 * <p>
 * If a {@link ValueProviderArg#RACE} is specified, assumes that 1 target of that race is in the pool.
 */
public final class DistinctTribesValueProvider extends ValueProvider {

	public DistinctTribesValueProvider(ValueProviderDesc desc) {
		super(desc);
	}

	@Override
	protected int provideValue(GameContext context, Player player, Entity target, Entity host) {
		List<Entity> targets;
		if (getDesc().containsKey(ValueProviderArg.TARGET)) {
			targets = context.resolveTarget(player, host, (EntityReference) getDesc().get(ValueProviderArg.TARGET));
		} else if (target != null) {
			targets = new ArrayList<>(Collections.singletonList(target));
		} else {
			targets = new ArrayList<>();
		}
		Stream<String> raceStream = targets.stream().map(Entity::getRace);
		if (getDesc().containsKey(ValueProviderArg.RACE)) {
			raceStream = Stream.concat(Stream.of((String) getDesc().get(ValueProviderArg.RACE)), raceStream);
		}
		return (int) raceStream.distinct().count();
	}
}
