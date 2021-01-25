package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.cards.Attribute;

/**
 * Refreshes the caster's hero power so it can be used again.
 */
public class RefreshHeroPowerSpell extends Spell {

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		int uses = player.getHeroPowerZone().get(0).hasBeenUsed();
		int heroPowerUsages = Math.min(context.getLogic().getGreatestAttributeValue(player, Attribute.HERO_POWER_USAGES),
				uses);
		if (heroPowerUsages == GameLogic.INFINITE) {
			return;
		}
		player.getHero().setAttribute(Attribute.HERO_POWER_USAGES, heroPowerUsages + 1);
	}
}
