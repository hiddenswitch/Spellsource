package net.demilich.metastone.game.spells.desc.filter;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.custom.EnvironmentEntityList;

public class StorageContainsFilter extends EntityFilter {

	public StorageContainsFilter(EntityFilterDesc desc) {
		super(desc);
	}

	@Override
	protected boolean test(GameContext context, Player player, Entity entity, Entity host) {
		var storage = EnvironmentEntityList.getList(context).getCards(context, host);
		for (Card card : storage) {
			if (entity.getSourceCard().getCardId().equals(card.getCardId())) {
				return true;
			}
		}
		return false;
	}
}
