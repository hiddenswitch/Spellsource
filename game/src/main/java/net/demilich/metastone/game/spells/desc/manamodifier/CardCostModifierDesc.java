package net.demilich.metastone.game.spells.desc.manamodifier;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import net.demilich.metastone.game.cards.costmodifier.CardCostModifier;
import net.demilich.metastone.game.cards.desc.CardCostModifierDescDeserializer;
import net.demilich.metastone.game.cards.desc.Desc;

import java.util.Map;

@JsonDeserialize(using = CardCostModifierDescDeserializer.class)
public final class CardCostModifierDesc extends Desc<CardCostModifierArg, CardCostModifier> {

	private static final long serialVersionUID = -8740306319574751013L;

	public CardCostModifierDesc() {
		super(CardCostModifierArg.class);
	}

	public CardCostModifierDesc(Class<? extends CardCostModifier> clazz) {
		super(clazz, CardCostModifierArg.class);
	}

	public CardCostModifierDesc(Map<CardCostModifierArg, Object> arguments) {
		super(arguments, CardCostModifierArg.class);
	}

	@Override
	protected Class<? extends Desc> getDescImplClass() {
		return CardCostModifierDesc.class;
	}

	@Override
	public CardCostModifierArg getClassArg() {
		return CardCostModifierArg.CLASS;
	}

	public CardCostModifierDesc addArg(CardCostModifierArg cardCostModififerArg, Object value) {
		CardCostModifierDesc clone = clone();
		clone.put(cardCostModififerArg, value);
		return clone;
	}

	public CardCostModifierDesc removeArg(CardCostModifierArg cardCostModififerArg) {
		CardCostModifierDesc clone = clone();
		clone.remove(cardCostModififerArg);
		return clone;
	}

	@Override
	public CardCostModifierDesc clone() {
		return (CardCostModifierDesc) copyTo(new CardCostModifierDesc(getDescClass()));
	}

}
