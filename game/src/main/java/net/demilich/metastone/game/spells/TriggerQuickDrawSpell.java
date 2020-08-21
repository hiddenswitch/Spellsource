package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.desc.HasEntrySet;
import net.demilich.metastone.game.entities.Entity;
import com.hiddenswitch.spellsource.client.models.EntityType;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;

import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Triggers the Quick Draw effect wrapped by the {@link QuickDrawSpell} written on the {@code target}.
 */
public final class TriggerQuickDrawSpell extends Spell {

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		// Find the quick draw spell by walking the card definition
		var quickDrawSpells = target.getSourceCard().getDesc()
				.bfs()
				.build()
				// Find the quick draw spell by its class key
				.filter(node -> Objects.equals(node.getKey(), SpellArg.CLASS) && Objects.equals(node.getValue(), QuickDrawSpell.class))
				// Now we're pointing to the node that is actually the spell
				.map(HasEntrySet.BfsNode::getParent)
				.map(node -> (SpellDesc) node.getValue())
				.filter(quickDrawSpell -> quickDrawSpell.getSpell() != null)
				.collect(Collectors.toList());

		if (quickDrawSpells.isEmpty()) {
			return;
		}

		for (var quickDrawSpell : quickDrawSpells) {
			// Skip fight spells if the source is a card
			if (GameLogic.isEntityType(source.getEntityType(), EntityType.CARD)
					&& FightSpell.class.isAssignableFrom(quickDrawSpell.getSpell().getDescClass())) {
				continue;
			}
			// Quick draws are not targeted
			SpellUtils.castChildSpell(context, player, quickDrawSpell.clone(), source, null);
		}
	}
}
