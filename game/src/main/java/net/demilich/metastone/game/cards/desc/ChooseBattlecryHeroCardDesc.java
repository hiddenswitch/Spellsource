package net.demilich.metastone.game.cards.desc;

import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.ChooseBattlecryHeroCard;
import net.demilich.metastone.game.spells.desc.BattlecryDesc;

public class ChooseBattlecryHeroCardDesc extends HeroCardDesc {
	public BattlecryDesc[] options;
	public BattlecryDesc bothOptions;

	@Override
	public Card createInstance() {
		return new ChooseBattlecryHeroCard(this);
	}
}
