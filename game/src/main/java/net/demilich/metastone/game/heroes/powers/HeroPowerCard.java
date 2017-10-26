package net.demilich.metastone.game.heroes.powers;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.HeroPowerAction;
import net.demilich.metastone.game.actions.PlayCardAction;
import net.demilich.metastone.game.cards.SpellCard;
import net.demilich.metastone.game.cards.desc.HeroPowerCardDesc;
import net.demilich.metastone.game.entities.heroes.HeroClass;

public class HeroPowerCard extends SpellCard {

	private int used;

	public HeroPowerCard(HeroPowerCardDesc desc) {
		super(desc);
	}

	public int hasBeenUsed() {
		return used;
	}

	public void markUsed() {
		this.used++;
	}

	public void onWillUse(GameContext context, Player player) {

	}

	@Override
	public PlayCardAction play() {
		return new HeroPowerAction(getSpell(), this, getTargetRequirement());
	}
	
	public void setUsed(int used) {
		this.used = used;
	}

	public void setHeroClass(HeroClass heroClass) {
		desc.heroClass = heroClass;
	}
}
