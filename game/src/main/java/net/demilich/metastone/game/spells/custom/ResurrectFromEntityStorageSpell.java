package net.demilich.metastone.game.spells.custom;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.cards.CardType;
import net.demilich.metastone.game.cards.MinionCard;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.Zones;

public class ResurrectFromEntityStorageSpell extends Spell {
	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		CardList resurrect = EnvironmentEntityList.getList(context).getCards(context, source).shuffle();
		int i = 0;

		while (context.getLogic().canSummonMoreMinions(player)
				&& i < resurrect.getCount()) {
			Card card = resurrect.get(i).getCopy();
			card.setId(context.getLogic().getIdFactory().generateId());
			card.setOwner(player.getId());
			card.moveOrAddTo(context, Zones.SET_ASIDE_ZONE);
			if (card.getCardType() == CardType.MINION) {
				MinionCard minionCard = (MinionCard) card;
				context.getLogic().summon(player.getId(), minionCard.summon(), minionCard, -1, false);
			}
			card.moveOrAddTo(context, Zones.REMOVED_FROM_PLAY);
			context.getLogic().removeCard(card);
			i++;
		}

		EnvironmentEntityList.getList(context).clear(source);
	}
}


