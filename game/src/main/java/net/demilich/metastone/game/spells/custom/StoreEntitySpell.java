package net.demilich.metastone.game.spells.custom;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stores the specified {@link net.demilich.metastone.game.spells.desc.SpellArg#TARGET} into a list keyed by the {@code
 * source} of the casting spell.
 * <p>
 * If a {@link net.demilich.metastone.game.spells.desc.SpellArg#SECONDARY_TARGET} is specified, the {@code target} is
 * stored on the secondary target instead.
 * <p>
 * This spell implements Primalfin Champion and Frostmourne.
 * <p>
 * For <b>example,</b> this effect stores the target of a battlecry on the minion whose battlecry is being cast:
 * <pre>
 *     {
 *         "class": "custom.StoreEntitySpell"
 *     }
 * </pre>
 * Here, the {@code target} is the target chosen by the player.
 *
 * @see ReceiveCardsInStorageSpell for a spell that retrieves the {@link net.demilich.metastone.game.cards.Card}
 * 		entities in storage and puts them in your hand.
 * @see net.demilich.metastone.game.spells.desc.source.StoredEntitiesSource for a {@link
 * 		net.demilich.metastone.game.spells.desc.source.CardSource} that lets you access the entities stored by this spell.
 */
public class StoreEntitySpell extends Spell {
	private static Logger logger = LoggerFactory.getLogger(StoreEntitySpell.class);

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		checkArguments(logger, context, source, desc, SpellArg.SECONDARY_TARGET);
		Entity storageSource = source;
		if (desc.containsKey(SpellArg.SECONDARY_TARGET)) {
			storageSource = context.resolveSingleTarget(player, source, (EntityReference) desc.get(SpellArg.SECONDARY_TARGET));
		}
		final EnvironmentEntityList list = EnvironmentEntityList.getList(context);
		list.add(storageSource, target);
		logger.debug("onCast {} {}: The {} entity added {} to its stored entity list. The list now contains: {}", context.getGameId(), source, storageSource, target, list.getReferences(storageSource));
	}
}

