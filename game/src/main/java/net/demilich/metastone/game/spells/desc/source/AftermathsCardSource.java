package net.demilich.metastone.game.spells.desc.source;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.CardArrayList;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.Entity;

import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Returns the cards of the aftermaths triggered by the {@link CardSourceArg#TARGET_PLAYER}.
 */
public class AftermathsCardSource extends CardSource {

	public AftermathsCardSource(CardSourceDesc desc) {
		super(desc);
	}

	@Override
	protected CardList match(GameContext context, Entity source, Player player) {
		/*context.getAftermaths().getAftermaths()
				.stream()
				.filter(aftermath -> aftermath.getPlayerId() == player.getId())
				.map(aftermath -> context.resolveSingleTarget(aftermath.getSource(), false))
				.filter(Objects::nonNull)
				.map(Entity::getSourceCard)
				.filter(Objects::nonNull)
				.collect(Collectors.toCollection(CardArrayList::new));*/
		return context.getAftermaths().getAftermaths()
				.stream()
				.filter(aftermath -> aftermath.getPlayerId() == player.getId())
				.map(aftermath -> aftermath.getCardId())
				.filter(Objects::nonNull)
				.map(context::getCardById)
				.collect(Collectors.toCollection(CardArrayList::new));
	}
}
