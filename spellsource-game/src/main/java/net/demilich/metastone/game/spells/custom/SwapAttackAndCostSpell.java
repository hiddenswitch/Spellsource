package net.demilich.metastone.game.spells.custom;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.CardCostModifierSpell;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.SpellUtils;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.valueprovider.AlgebraicOperation;
import net.demilich.metastone.game.cards.Attribute;

/**
 * Swaps the Attack and the mana cost of the {@code target} card.
 */
public class SwapAttackAndCostSpell extends Spell {

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		Card card = (Card) target;
		int cost = context.getLogic().getModifiedManaCost(player, card);
		int attack = card.getAttack() + card.getBonusAttack();

		SpellDesc changeCost = CardCostModifierSpell.create(target.getReference(), AlgebraicOperation.SET, attack);
		card.getAttributes().put(Attribute.ATTACK, cost);
		card.getAttributes().put(Attribute.ATTACK_BONUS, 0);
		SpellUtils.castChildSpell(context, player, changeCost, source, target);
	}
}
