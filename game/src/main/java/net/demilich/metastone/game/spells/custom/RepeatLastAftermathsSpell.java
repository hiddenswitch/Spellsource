package net.demilich.metastone.game.spells.custom;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.environment.EnvironmentAftermathTriggeredList;
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
		// Just return the last three from the owner of the source
		Collections.reverse(aftermaths);
		return aftermaths.stream()
				.takeWhile(item -> item.getPlayerId() == source.getOwner())
				.limit(3L)
				.collect(Collectors.toCollection(ArrayList::new));
	}

	@Override
	protected boolean aftermathPredicate(GameContext context, Player player, Entity source, Entity target, EnvironmentAftermathTriggeredList.EnvironmentAftermathTriggeredItem item) {
		// Already filtered
		return true;
	}
}
