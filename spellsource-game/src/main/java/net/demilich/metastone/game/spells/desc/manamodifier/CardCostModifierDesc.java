package net.demilich.metastone.game.spells.desc.manamodifier;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.costmodifier.CardCostModifier;
import net.demilich.metastone.game.cards.desc.CardCostModifierDescDeserializer;
import net.demilich.metastone.game.cards.desc.Desc;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.AbstractEnchantmentDesc;

import java.util.Map;
import java.util.Optional;

@JsonDeserialize(using = CardCostModifierDescDeserializer.class)
public final class CardCostModifierDesc extends Desc<CardCostModifierArg, CardCostModifier> implements AbstractEnchantmentDesc<CardCostModifier> {


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

	@Override
	public Optional<CardCostModifier> tryCreate(GameContext context, Player player, Entity effectSource, Card enchantmentSource, Entity host, boolean force) {
		return context.getLogic().tryCreateCardCostModifier(this, effectSource, enchantmentSource, host, force);
	}
}
