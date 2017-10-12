package net.demilich.metastone.game.cards.desc;

import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.GroupCard;
import net.demilich.metastone.game.spells.desc.SpellDesc;

public class GroupCardDesc extends CardDesc {

	public SpellDesc[] group;

	@Override
	public Card createInstance() {
		return new GroupCard(this);
	}

}
