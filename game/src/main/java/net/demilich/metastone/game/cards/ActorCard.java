package net.demilich.metastone.game.cards;

import net.demilich.metastone.game.cards.desc.ActorCardDesc;
import net.demilich.metastone.game.spells.desc.SpellDesc;

import java.util.ArrayList;
import java.util.List;

public abstract class ActorCard extends Card {
	protected List<SpellDesc> deathrattleEnchantments = new ArrayList<>();

	public ActorCard(ActorCardDesc desc) {
		super(desc);
	}

	public boolean hasDeathrattle() {
		return ((ActorCardDesc) desc).deathrattle != null;
	}

	public void addDeathrattle(SpellDesc deathrattle) {
		// TODO: Should Forlorn Stalker affect cards with deathrattle added this way?
		deathrattleEnchantments.add(deathrattle);
	}
}
