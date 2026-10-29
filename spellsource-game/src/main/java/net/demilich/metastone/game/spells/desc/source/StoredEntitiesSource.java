package net.demilich.metastone.game.spells.desc.source;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardArrayList;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.custom.EnvironmentEntityList;
import net.demilich.metastone.game.targeting.EntityReference;

import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.toCollection;

/**
 * Expects {@link CardSourceArg#SOURCE} to returns a single entity from which an {@link
 * EnvironmentEntityList#getList(GameContext)} will be traversed on {@link EnvironmentEntityList#getCards(GameContext,
 * Entity)} to map to a card list. If {@link CardSourceArg#SOURCE} is not defined, the {@code source} is used by
 * default.
 * <p>
 * Notice how this differs from {@link CardSourceArg#TARGET_PLAYER}, which the base class uses to call {@link
 * #match(GameContext, Entity, Player)} potentially twice across both players.
 *
 * @see net.demilich.metastone.game.spells.AddToTopOfDeckSpell for an example of a spell which adds entities to this
 * 		storage list.
 */
public class StoredEntitiesSource extends CardSource {

	private static final StoredEntitiesSource INSTANCE = new StoredEntitiesSource(new CardSourceDesc(StoredEntitiesSource.class));

	public StoredEntitiesSource(CardSourceDesc desc) {
		super(desc);
	}

	protected CardList match(GameContext context, Entity source, Player player, EntityReference sourceReference) {
		CardList list = new CardArrayList();
		if (sourceReference != null) {
			List<Entity> targets = context.resolveTarget(player, source, sourceReference);
			if (targets == null) {
				return list;
			}
			for (Entity target : targets) {
				CardList cards = EnvironmentEntityList.getList(context).getCards(context, target).stream().filter(Objects::nonNull)
						.filter(e -> e.getSourceCard() != null)
						.map(Entity::getSourceCard)
						.map(Card::getCopy)
						.collect(toCollection(CardArrayList::new));
				list.addAll(cards);
			}
		} else {
			list.addAll(EnvironmentEntityList.getList(context).getCards(context, source).stream().filter(Objects::nonNull)
					.filter(e -> e.getSourceCard() != null)
					.map(Entity::getSourceCard)
					.map(Card::getCopy)
					.collect(toCollection(CardArrayList::new)));
		}
		return list;
	}

	public static StoredEntitiesSource create() {
		return INSTANCE;
	}
}
