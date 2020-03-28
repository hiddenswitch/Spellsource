package net.demilich.metastone.game.cards.desc;

import net.demilich.metastone.game.spells.desc.source.CardSource;
import net.demilich.metastone.game.spells.desc.source.CardSourceArg;
import net.demilich.metastone.game.spells.desc.source.CardSourceDesc;

public class CardSourceDescDeserializer extends DescDeserializer<CardSourceDesc, CardSourceArg, CardSource> {

	public CardSourceDescDeserializer() {
		super(CardSourceDesc.class);
	}

	@Override
	protected CardSourceDesc createDescInstance() {
		return new CardSourceDesc();
	}

	@Override
	public void init(SerializationContext ctx) {
		ctx.add(CardSourceArg.TARGET_PLAYER, ParseValueType.TARGET_PLAYER);
		ctx.add(CardSourceArg.COLLECTION_NAME, ParseValueType.STRING);
		ctx.add(CardSourceArg.INVERT, ParseValueType.BOOLEAN);
		ctx.add(CardSourceArg.SOURCE, ParseValueType.TARGET_REFERENCE);
		ctx.add(CardSourceArg.DISTINCT, ParseValueType.BOOLEAN);
		ctx.add(CardSourceArg.FORMAT, ParseValueType.STRING);
		ctx.add(CardSourceArg.HERO_CLASS, ParseValueType.STRING);
		ctx.add(CardSourceArg.CARD_SOURCES, ParseValueType.CARD_SOURCE_ARRAY);
		ctx.add(CardSourceArg.VALUE, ParseValueType.VALUE);
	}

	@Override
	protected Class<CardSource> getAbstractComponentClass() {
		return CardSource.class;
	}

	@Override
	protected Class<CardSourceArg> getEnumType() {
		return CardSourceArg.class;
	}
}
