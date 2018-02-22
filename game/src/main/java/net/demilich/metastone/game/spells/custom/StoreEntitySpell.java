package net.demilich.metastone.game.spells.custom;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stores the specified {@link net.demilich.metastone.game.spells.desc.SpellArg#TARGET} into a list keyed by the {@code
 * source} of the casting spell.
 * <p>
 * This spell implements Primalfin Champion and Frostmourne.
 * <p>
 * Example:
 * <pre>
 *     {
 *         "class": "custom.StoreEntitySpell",
 *         "target": "PENDING_CARD"
 *     }
 * </pre>
 *
 * @see ReceiveCardsInStorageSpell for a spell that retrieves the {@link net.demilich.metastone.game.cards.Card}
 * entities in storage and puts them in your hand.
 * @see net.demilich.metastone.game.spells.desc.source.StoredEntitiesSource for a {@link
 * net.demilich.metastone.game.spells.desc.source.CardSource} that lets you access the entities stored by this spell.
 */
public class StoreEntitySpell extends Spell {
	private static Logger logger = LoggerFactory.getLogger(StoreEntitySpell.class);

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		checkArguments(logger, context, source, desc);
		final EnvironmentEntityList list = EnvironmentEntityList.getList(context);
		logger.debug("onCast {} {}: The source entity added {} to its stored entity list. The list currently contains: {}", context.getGameId(), source, target, list.getReferences(context, source));
		list.add(source, target);
	}
}

