package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.aura.SupremaciesDrawCardAura;
import net.demilich.metastone.game.spells.aura.SupremaciesTriggerTwiceAura;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.utils.Attribute;

import java.util.function.Predicate;

/**
 * Indicates that the subspells should be cast as the "Supremacy" effect.
 * <p>
 * Implements Gamon's doubling of supremacies effect.
 * <p>
 * Implements Supreme Firelord's card drawing on supremacy effect.
 * <p>
 * Increments {@link Attribute#SUPREMACIES_THIS_GAME}.
 */
public class SupremacySpell extends MetaSpell {
	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {

		super.onCast(context, player, desc, source, target);
		boolean playerSupremaciesDrawCards = context.getEntities()
				.filter(e -> e.getOwner() == player.getId() && e.isInPlay())
				.anyMatch(m -> context.getTriggersAssociatedWith(m.getReference()).stream()
						.filter(t -> t instanceof SupremaciesDrawCardAura)
						.map(t -> (SupremaciesDrawCardAura) t)
						.anyMatch(((Predicate<SupremaciesDrawCardAura>) SupremaciesDrawCardAura::isExpired).negate()));
		if (playerSupremaciesDrawCards) {
			context.getLogic().drawCard(player.getId(), source);
		}

		player.modifyAttribute(Attribute.SUPREMACIES_THIS_GAME, 1);

		boolean playerHasDoubleSupremacies = context.getEntities()
				.filter(e -> e.getOwner() == player.getId() && e.isInPlay())
				.anyMatch(m -> context.getTriggersAssociatedWith(m.getReference()).stream()
						.filter(t -> t instanceof SupremaciesTriggerTwiceAura)
						.map(t -> (SupremaciesTriggerTwiceAura) t)
						.anyMatch(((Predicate<SupremaciesTriggerTwiceAura>) SupremaciesTriggerTwiceAura::isExpired).negate()));

		if (playerHasDoubleSupremacies) {
			super.onCast(context, player, desc, source, target);
			if (playerSupremaciesDrawCards) {
				context.getLogic().drawCard(player.getId(), source);
			}
			player.modifyAttribute(Attribute.SUPREMACIES_THIS_GAME, 1);
		}
	}
}
