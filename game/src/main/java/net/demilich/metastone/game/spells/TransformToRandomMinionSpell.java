package net.demilich.metastone.game.spells;

import java.util.Map;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.*;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import net.demilich.metastone.game.spells.desc.source.CardSource;
import net.demilich.metastone.game.spells.desc.source.CatalogueSource;

public class TransformToRandomMinionSpell extends TransformMinionSpell {

	public static SpellDesc create() {
		Map<SpellArg, Object> arguments = new SpellDesc(TransformToRandomMinionSpell.class);
		return new SpellDesc(arguments);
	}

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		CardList filteredMinions = desc.getFilteredCards(context, player, source);
		Card randomCard = context.getLogic().getRandom(filteredMinions);
		if (randomCard != null) {
			SpellDesc transformMinionSpell = TransformMinionSpell.create(randomCard.getCardId());
			super.onCast(context, player, transformMinionSpell, source, target);
		}
	}

}
