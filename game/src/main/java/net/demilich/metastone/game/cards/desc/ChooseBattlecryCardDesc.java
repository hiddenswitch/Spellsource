package net.demilich.metastone.game.cards.desc;

import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.ChooseBattlecryMinionCard;
import net.demilich.metastone.game.spells.desc.BattlecryDesc;

public class ChooseBattlecryCardDesc extends MinionCardDesc {

	public BattlecryDesc[] options;
	public BattlecryDesc bothOptions;

	@Override
	public Card createInstance() {
		return new ChooseBattlecryMinionCard(this);
	}
}
