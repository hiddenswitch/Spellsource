package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.aura.SupremaciesTriggerTwiceAura;
import net.demilich.metastone.game.spells.aura.SupremacyBonusEffectAura;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.cards.Attribute;

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
		for (SpellDesc subSpell : SpellUtils.getBonusesFromAura(context, player.getId(), SupremacyBonusEffectAura.class, source, target)) {
			SpellUtils.castChildSpell(context, player, subSpell, source, target);
		}
		player.modifyAttribute(Attribute.SUPREMACIES_THIS_GAME, 1);

		boolean playerHasDoubleSupremacies = SpellUtils.hasAura(context, player.getId(), SupremaciesTriggerTwiceAura.class);
		if (playerHasDoubleSupremacies) {
			super.onCast(context, player, desc, source, target);
			for (SpellDesc subSpell : SpellUtils.getBonusesFromAura(context, player.getId(), SupremacyBonusEffectAura.class, source, target)) {
				SpellUtils.castChildSpell(context, player, subSpell, source, target);
			}
			player.modifyAttribute(Attribute.SUPREMACIES_THIS_GAME, 1);
		}
	}

}
