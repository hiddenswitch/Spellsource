package net.demilich.metastone.game.spells.desc.source;

import net.demilich.metastone.game.cards.desc.Desc;

import java.util.Map;

public class CardSourceDesc extends Desc<CardSourceArg, CardSource> {

	public CardSourceDesc() {
		super();
	}

	public CardSourceDesc(Class<? extends CardSource> cardSourceClass) {
		super(cardSourceClass);
	}

	public CardSourceDesc(Map<CardSourceArg, Object> arguments) {
		super(arguments);
	}

	@Override
	protected Class<? extends Desc> getDescImplClass() {
		return CardSourceDesc.class;
	}

	@Override
	public CardSourceArg getClassArg() {
		return CardSourceArg.CLASS;
	}

	@Override
	public CardSourceDesc clone() {
		return (CardSourceDesc) copyTo(new CardSourceDesc(getDescClass()));
	}
}
