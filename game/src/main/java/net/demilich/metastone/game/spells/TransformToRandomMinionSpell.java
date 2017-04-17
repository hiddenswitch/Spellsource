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

public class TransformToRandomMinionSpell extends TransformMinionSpell {

	public static SpellDesc create() {
		Map<SpellArg, Object> arguments = SpellDesc.build(TransformToRandomMinionSpell.class);
		return new SpellDesc(arguments);
	}

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		EntityFilter filter = (EntityFilter) desc.get(SpellArg.CARD_FILTER);

		CardCollection allMinions = CardCatalogue.query(context.getDeckFormat(), CardType.MINION);
		CardCollection filteredMinions = new CardCollectionImpl();
		for (Card card : allMinions) {
			MinionCard minionCard = (MinionCard) card;
			if (filter == null || filter.matches(context, player, card)) {
				filteredMinions.addCard(minionCard);
			}
		}
		MinionCard randomCard = (MinionCard) filteredMinions.getRandom();

		if (randomCard != null) {
			SpellDesc transformMinionSpell = TransformMinionSpell.create(randomCard.getCardId());
			super.onCast(context, player, transformMinionSpell, source, target);
		}
	}

}
