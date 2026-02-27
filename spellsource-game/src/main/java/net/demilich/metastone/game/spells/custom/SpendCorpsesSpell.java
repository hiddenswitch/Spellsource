package net.demilich.metastone.game.spells.custom;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.SpellUtils;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Spends up to VALUE corpses (default 1) from the player's graveyard.
 *
 * Corpses are "spent" by removing the {@link Attribute#DIED_ON_TURN} attribute from graveyard
 * entities, which prevents {@link net.demilich.metastone.game.spells.desc.valueprovider.GraveyardMinionCountValueProvider}
 * from counting them as available corpses. This is identical to how
 * {@link net.demilich.metastone.game.spells.DrawCardFromGraveyardSpell} marks corpses consumed.
 *
 * If {@link SpellArg#SPELL} is provided, it is cast once for each corpse actually spent,
 * allowing scaling effects like "deal 2 damage per corpse spent (up to 5)."
 */
public class SpendCorpsesSpell extends Spell {

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		int maxToSpend = desc.getValue(SpellArg.VALUE, context, player, target, source, 1);

		// Collect available corpses: graveyard entities that died on the battlefield
		List<Entity> corpses = player.getGraveyard().stream()
				.filter(e -> e.hasAttribute(Attribute.DIED_ON_TURN))
				.collect(Collectors.toList());

		int toSpend = Math.min(maxToSpend, corpses.size());

		// Spend corpses by removing DIED_ON_TURN — same pattern as DrawCardFromGraveyardSpell
		for (int i = 0; i < toSpend; i++) {
			corpses.get(i).getAttributes().remove(Attribute.DIED_ON_TURN);
		}

		// If a sub-spell is provided, cast it once per corpse spent (for scaling effects)
		SpellDesc subSpell = (SpellDesc) desc.get(SpellArg.SPELL);
		if (subSpell != null) {
			for (int i = 0; i < toSpend; i++) {
				SpellUtils.castChildSpell(context, player, subSpell, source, target);
			}
		}
	}
}
