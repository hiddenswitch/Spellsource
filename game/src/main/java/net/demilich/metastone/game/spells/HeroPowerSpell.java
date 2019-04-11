package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.aura.HeroPowerBonusAura;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;

/**
 * Signifies that the subspells {@link SpellArg#SPELL}, {@link SpellArg#SPELLS}, etc.) represent the "hero power effect"
 * for cards that interact with the hero power.
 */
public final class HeroPowerSpell extends MetaSpell {

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		super.onCast(context, player, desc, source, target);
		for (SpellDesc bonusEffect : SpellUtils.getBonusesFromAura(context, player.getId(), HeroPowerBonusAura.class, source, target)) {
			SpellUtils.castChildSpell(context, player, bonusEffect, source, target);
		}
	}
}
