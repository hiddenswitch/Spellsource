package net.demilich.metastone.game.cards;

import net.demilich.metastone.game.actions.PlayCardAction;
import net.demilich.metastone.game.cards.desc.GroupCardDesc;
import net.demilich.metastone.game.spells.desc.SpellDesc;

public class GroupCard extends Card {

	private SpellDesc[] spells;

	public GroupCard(GroupCardDesc desc) {
		super(desc);
		setGroup(desc.group);
	}

	@Override
	public Card clone() {
		GroupCard clone = (GroupCard) super.clone();
		return clone;
	}

	public SpellDesc[] getGroup() {
		return spells;
	}

	public void setGroup(SpellDesc[] spells) {
		this.spells = spells;
	}

	@Override
	public PlayCardAction play() {
		throw new UnsupportedOperationException("The method .play() should not be called for GroupCard");
	}

}
