package net.demilich.metastone.game.spells.custom;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.desc.SpellDesc;

public class ReceiveCardsInStorageSpell extends Spell {
	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		CardList cards = EnvironmentEntityList.getList(context).getCards(context, source).shuffle(context.getLogic().getRandom());
		cards.forEach(c -> context.getLogic().receiveCard(player.getId(), c.getCopy(), source));
		EnvironmentEntityList.getList(context).clear(source);
	}
}
