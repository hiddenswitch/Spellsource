package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;

import java.util.Map;

/**
 * Transforms the {@code target} into a random <b>base</b> minion from {@link SpellUtils#getCards(GameContext, Player,
 * Entity, Entity, SpellDesc, int)}.
 *
 * @see net.demilich.metastone.game.logic.GameLogic#transformMinion(SpellDesc, Entity, Minion, Minion, boolean) for the complete rules of
 * 		transforming minions.
 */
public class TransformToRandomMinionSpell extends TransformMinionSpell {

	public static SpellDesc create() {
		Map<SpellArg, Object> arguments = new SpellDesc(TransformToRandomMinionSpell.class);
		return new SpellDesc(arguments);
	}

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		CardList filteredMinions = SpellUtils.getCards(context, player, target, source, desc, 1);
		if (filteredMinions.isEmpty()) {
			return;
		}
		Card randomCard = filteredMinions.get(0);
		SpellDesc transformMinionSpell = TransformMinionSpell.create(randomCard.getCardId());
		super.onCast(context, player, transformMinionSpell, source, target);
	}
}
