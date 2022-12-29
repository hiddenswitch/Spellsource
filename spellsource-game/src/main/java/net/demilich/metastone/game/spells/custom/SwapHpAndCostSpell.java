package net.demilich.metastone.game.spells.custom;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.CardCostModifierSpell;
import net.demilich.metastone.game.spells.SetHpSpell;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.SpellUtils;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.valueprovider.AlgebraicOperation;

/**
 * Swaps the HP and the cost of the {@code target}.
 */
public class SwapHpAndCostSpell extends Spell {

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		int cost = context.getLogic().getModifiedManaCost(player, (Card) target);
		int hp = ((Card) target).getHp();

		SpellDesc changeCost = CardCostModifierSpell.create(target.getReference(), AlgebraicOperation.SET, hp);
		SpellDesc changeHp = SetHpSpell.create(cost);
		SpellUtils.castChildSpell(context,player,changeCost,source,target);
		SpellUtils.castChildSpell(context,player,changeHp,source,target);
	}
}
