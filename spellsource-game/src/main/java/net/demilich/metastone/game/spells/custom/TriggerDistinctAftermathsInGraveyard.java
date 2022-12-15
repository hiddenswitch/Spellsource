package net.demilich.metastone.game.spells.custom;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.TriggerDeathrattleSpell;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Triggers {@link SpellArg#VALUE} distinct aftermaths in the player's graveyard whose entities match the {@link
 * SpellArg#FILTER} specified.
 */
public final class TriggerDistinctAftermathsInGraveyard extends TriggerDeathrattleSpell {

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		EntityFilter filter = desc.getEntityFilter();
		Stream<Entity> stream = player.getGraveyard().stream();
		if (filter != null) {
			stream = stream.filter(filter.matcher(context, player, source));
		}
		List<Entity> distinctByCardId = stream
				.filter(distinctByKey(e -> e.getSourceCard() != null ? e.getSourceCard().getCardId() : ""))
				.collect(Collectors.toList());
		int count = desc.getValue(SpellArg.VALUE, context, player, target, source, 3);

		while (!distinctByCardId.isEmpty() && count > 0) {
			Entity aftermathEntity = context.getLogic().removeRandom(distinctByCardId);
			super.onCast(context, player, TriggerDeathrattleSpell.create(aftermathEntity.getReference()), source, aftermathEntity);
			count--;
		}
	}

	public static <T> Predicate<T> distinctByKey(
			Function<? super T, ?> keyExtractor) {

		Map<Object, Boolean> seen = new ConcurrentHashMap<>();
		return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
	}
}
