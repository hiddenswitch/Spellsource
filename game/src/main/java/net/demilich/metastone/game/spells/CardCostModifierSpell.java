package net.demilich.metastone.game.spells;

import java.util.Map;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.costmodifier.CardCostModifier;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import net.demilich.metastone.game.spells.desc.manamodifier.CardCostModifierArg;
import net.demilich.metastone.game.spells.desc.manamodifier.CardCostModifierDesc;
import net.demilich.metastone.game.spells.desc.valueprovider.AlgebraicOperation;
import net.demilich.metastone.game.targeting.EntityReference;

public class CardCostModifierSpell extends Spell {

	public static SpellDesc create(CardCostModifierDesc cardCostModifierDesc) {
		return create(cardCostModifierDesc, null);
	}

	public static SpellDesc create(CardCostModifierDesc cardCostModifierDesc, EntityFilter cardFilter) {
		Map<SpellArg, Object> arguments = SpellDesc.build(CardCostModifierSpell.class);
		arguments.put(SpellArg.CARD_COST_MODIFIER, cardCostModifierDesc);
		arguments.put(SpellArg.CARD_FILTER, cardFilter);
		return new SpellDesc(arguments);
	}

	public static SpellDesc create(EntityReference target, AlgebraicOperation operation, int value) {
		return CardCostModifierSpell.create(target, operation, value, null);
	}


	public static SpellDesc create(EntityReference target, AlgebraicOperation operation, int value, EntityFilter cardFilter) {
		CardCostModifierDesc manaModifierDesc = new CardCostModifierDesc(CardCostModifierDesc.build(CardCostModifier.class));
		manaModifierDesc = manaModifierDesc.addArg(CardCostModifierArg.OPERATION, operation);
		manaModifierDesc = manaModifierDesc.addArg(CardCostModifierArg.TARGET, target);
		manaModifierDesc = manaModifierDesc.addArg(CardCostModifierArg.VALUE, value);
		return CardCostModifierSpell.create(manaModifierDesc, cardFilter);
	}

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		CardCostModifierDesc manaModifierDesc = (CardCostModifierDesc) desc.get(SpellArg.CARD_COST_MODIFIER);
		// The target is the host of the mana cost modifier.
		context.getLogic().addGameEventListener(player, manaModifierDesc.create(), target == null ? player : target);
	}

}
