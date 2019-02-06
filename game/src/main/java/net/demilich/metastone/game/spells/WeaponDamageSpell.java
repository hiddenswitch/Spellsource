package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;

/**
 * Deals magical damage from the weapon equipped by the {@code player} ({@link SpellArg#TARGET_PLAYER}), correctly
 * accounting for effects like {@link net.demilich.metastone.game.cards.Attribute#LIFESTEAL} and {@link
 * net.demilich.metastone.game.cards.Attribute#POISONOUS}.
 */
public final class WeaponDamageSpell extends DamageSpell {

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		if (player.getWeaponZone().isEmpty()) {
			return;
		}
		desc = desc.clone();
		desc.put(SpellArg.IGNORE_SPELL_DAMAGE, true);
		desc.put(SpellArg.VALUE, player.getWeaponZone().get(0).getAttack());
		super.onCast(context, player, desc, player.getHero(), target);
	}
}
