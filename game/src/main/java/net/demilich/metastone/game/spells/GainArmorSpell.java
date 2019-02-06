package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;

/**
 * Gives the hero of the {@code target}'s owner {@link net.demilich.metastone.game.spells.desc.SpellArg#VALUE} armor.
 */
public final class GainArmorSpell extends BuffSpell {
	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		SpellDesc armorBuffDesc = new SpellDesc(GainArmorSpell.class);
		armorBuffDesc.put(SpellArg.ARMOR_BONUS, desc.getValue(SpellArg.VALUE, context, player, target, source, 0));
		armorBuffDesc.put(SpellArg.TARGET, context.getPlayer(target.getOwner()).getHero().getReference());
		super.onCast(context, player, armorBuffDesc, source, target);
	}
}
