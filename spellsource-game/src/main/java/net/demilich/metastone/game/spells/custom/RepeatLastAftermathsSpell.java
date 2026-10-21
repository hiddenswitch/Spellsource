package net.demilich.metastone.game.spells.custom;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.environment.EnvironmentAftermathTriggeredList;
import net.demilich.metastone.game.spells.desc.SpellArg;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * Repeats the last {@link net.demilich.metastone.game.spells.desc.SpellArg#VALUE} aftermaths triggered by owner of the
 * {@code source}.
 */
public class RepeatLastAftermathsSpell extends RepeatAllAftermathsSpell {

	@Override
	protected @NotNull ArrayList<EnvironmentAftermathTriggeredList.EnvironmentAftermathTriggeredItem> getAftermaths(GameContext context, Entity source) {
		var aftermaths = super.getAftermaths(context, source);
		// Return the last VALUE aftermaths from the owner of the source
		Collections.reverse(aftermaths);
		long limit = getDesc() != null ? getDesc().getValue(SpellArg.VALUE, context, context.getPlayer(source.getOwner()), null, source, 3) : 3L;
		return aftermaths.stream()
				.takeWhile(item -> item.playerId() == source.getOwner())
				.limit(limit)
				.collect(Collectors.toCollection(ArrayList::new));
	}

	@Override
	protected boolean aftermathPredicate(GameContext context, Player player, Entity source, Entity target, EnvironmentAftermathTriggeredList.EnvironmentAftermathTriggeredItem item) {
		// Already filtered
		return true;
	}
}
