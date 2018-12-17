package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.cards.Attribute;

/**
 * Refreshes the caster's hero power so it can be used again.
 */
public class RefreshHeroPowerSpell extends Spell {

	private static final long serialVersionUID = 8404171210045792785L;

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		int heroPowerUsages = player.getHero().getHeroPower().hasBeenUsed();
		player.getHero().setAttribute(Attribute.HERO_POWER_USAGES, heroPowerUsages + 1);
	}
}
