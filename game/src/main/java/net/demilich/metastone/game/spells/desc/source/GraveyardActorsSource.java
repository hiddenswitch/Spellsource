package net.demilich.metastone.game.spells.desc.source;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardArrayList;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.TargetPlayer;
import net.demilich.metastone.game.cards.Attribute;

import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toCollection;

public class GraveyardActorsSource extends CardSource implements HasCardCreationSideEffects {

	private static final long serialVersionUID = 8041698581153074954L;

	public GraveyardActorsSource(CardSourceDesc desc) {
		super(desc);
	}

	public static CardSource create() {
		Map<CardSourceArg, Object> args = new CardSourceDesc(GraveyardActorsSource.class);
		return new CardSourceDesc(args).create();
	}

	public static CardSource create(TargetPlayer targetPlayer) {
		Map<CardSourceArg, Object> args = new CardSourceDesc(GraveyardActorsSource.class);
		args.put(CardSourceArg.TARGET_PLAYER, targetPlayer);
		return new CardSourceDesc(args).create();
	}

	@Override
	protected CardList match(GameContext context, Entity source, Player player) {
		return player
				.getGraveyard()
				.stream()
				.filter(e -> e instanceof Actor)
				// Must be actors that actually died
				.filter(e -> !e.isRemovedPeacefully())
				.map(actor -> {
					Card sourceCard = actor.getSourceCard();
					if (sourceCard == null) {
						return null;
					}
					// Include the turn this card died
					sourceCard.getAttributes().put(Attribute.DIED_ON_TURN, actor.getAttributeValue(Attribute.DIED_ON_TURN));
					return sourceCard;
				})
				.filter(Objects::nonNull)
				.collect(toCollection(CardArrayList::new))
				.getCopy();
	}
}
