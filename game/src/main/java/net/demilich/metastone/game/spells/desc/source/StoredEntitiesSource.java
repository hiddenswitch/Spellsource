package net.demilich.metastone.game.spells.desc.source;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.CardArrayList;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.custom.EnvironmentEntityList;
import net.demilich.metastone.game.targeting.EntityReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Retrieves the cards stored in the {@code source} {@link Entity} or {@link SourceArg#SOURCE} {@link EntityReference}
 * by a {@link net.demilich.metastone.game.spells.custom.StoreEntitySpell}.
 * <p>
 * Example:
 * <pre>
 *     {
 *         "class": "StoreEntitiesSource",
 *         "source": "SELF"
 *     }
 * </pre>
 *
 * @see net.demilich.metastone.game.spells.custom.StoreEntitySpell for more on storing entities with respect to a
 * specific {@code source} {@link Entity}.
 */
public class StoredEntitiesSource extends CardSource {

	private static Logger logger = LoggerFactory.getLogger(StoredEntitiesSource.class);

	public StoredEntitiesSource(SourceDesc desc) {
		super(desc);
	}

	@Override
	protected CardList match(GameContext context, Entity source, Player player) {
		EntityReference sourceOverride = (EntityReference) desc.get(SourceArg.SOURCE);
		if (source == null
				&& sourceOverride == null) {
			logger.error("match {}: A SOURCE argument is required.", context.getGameId());
			return new CardArrayList();
		}
		if (sourceOverride != null) {
			source = context.resolveTarget(player, source, sourceOverride).get(0);
		}
		return EnvironmentEntityList.getList(context).getCards(context, source);
	}
}
