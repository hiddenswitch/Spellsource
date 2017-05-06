package net.demilich.metastone.game.cards.desc;

import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.heroes.powers.HeroPowerCard;
import net.demilich.metastone.game.heroes.powers.HeroPowerChooseOneCard;

public class HeroPowerCardDesc extends SpellCardDesc {
	
	public String[] options;
	public String bothOptions;

	@Override
	public Card createInstance() {
		if (options != null && options.length > 0) {
			return new HeroPowerChooseOneCard(this);
		}
		return new HeroPowerCard(this);
	}

}
