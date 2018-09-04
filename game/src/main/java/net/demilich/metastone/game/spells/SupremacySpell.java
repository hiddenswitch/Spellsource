package net.demilich.metastone.game.spells;

import com.github.fromage.quasi.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.aura.Aura;
import net.demilich.metastone.game.spells.aura.SupremaciesTriggerTwiceAura;
import net.demilich.metastone.game.spells.aura.SupremacyBonusEffectAura;
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
		for (SpellDesc subSpell : getSupremacyBonuses(context, player.getId())) {
			SpellUtils.castChildSpell(context, player, subSpell, source, target);
		}
		player.modifyAttribute(Attribute.SUPREMACIES_THIS_GAME, 1);

		boolean playerHasDoubleSupremacies = hasAura(context, player.getId(), SupremaciesTriggerTwiceAura.class);
		if (playerHasDoubleSupremacies) {
			super.onCast(context, player, desc, source, target);
			for (SpellDesc subSpell : getSupremacyBonuses(context, player.getId())) {
				SpellUtils.castChildSpell(context, player, subSpell, source, target);
			}
			player.modifyAttribute(Attribute.SUPREMACIES_THIS_GAME, 1);
		}
	}

	protected static <T extends Aura> boolean hasAura(GameContext context, int playerId, Class<T> auraClass) {
		return context.getEntities()
				.filter(e -> e.getOwner() == playerId && e.isInPlay())
				.anyMatch(m -> context.getTriggersAssociatedWith(m.getReference()).stream()
						.filter(auraClass::isInstance)
						.map(t -> (Aura) t)
						.anyMatch(((Predicate<Aura>) Aura::isExpired).negate()));
	}

	protected static SpellDesc[] getSupremacyBonuses(GameContext context, int playerId) {
		return context.getEntities()
				.filter(e -> e.getOwner() == playerId && e.isInPlay())
				.flatMap(m -> context.getTriggersAssociatedWith(m.getReference()).stream()
						.filter(t -> t instanceof SupremacyBonusEffectAura)
						.map(t -> (Aura) t)
						.filter(((Predicate<Aura>) Aura::isExpired).negate())
						.map(aura -> aura.getDesc().getApplyEffect()))
				.toArray(SpellDesc[]::new);
	}
}
