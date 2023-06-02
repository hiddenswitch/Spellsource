package net.demilich.metastone.game.spells.desc.source;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.CardArrayList;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.environment.EnvironmentAftermathTriggeredList;

import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Returns the base cards of the aftermaths triggered by the {@link CardSourceArg#TARGET_PLAYER}.
 */
public class AftermathsCardSource extends CardSource implements HasCardCreationSideEffects {

	public AftermathsCardSource(CardSourceDesc desc) {
		super(desc);
	}

	@Override
	protected CardList match(GameContext context, Entity source, Player player) {
		return context.getAftermaths().getAftermaths()
				.stream()
				.filter(aftermath -> aftermath.getPlayerId() == player.getId())
				.map(EnvironmentAftermathTriggeredList.EnvironmentAftermathTriggeredItem::getCardId)
				.filter(Objects::nonNull)
				.map(context::getCardById)
				.collect(Collectors.toCollection(CardArrayList::new));
	}
}
