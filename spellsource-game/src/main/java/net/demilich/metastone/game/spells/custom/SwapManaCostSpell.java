package net.demilich.metastone.game.spells.custom;

import com.hiddenswitch.spellsource.rpc.Spellsource.CardTypeMessage.CardType;
import com.hiddenswitch.spellsource.rpc.Spellsource.ZonesMessage.Zones;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.cards.costmodifier.CardCostModifier;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.SpellUtils;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.manamodifier.CardCostModifierArg;
import net.demilich.metastone.game.spells.desc.manamodifier.CardCostModifierDesc;
import net.demilich.metastone.game.spells.desc.valueprovider.AlgebraicOperation;
import net.demilich.metastone.game.targeting.EntityReference;

/**
 * Draws a minion and a spell from the player's deck, then swaps their costs.
 * <p>
 * Implements Prismatic Lens.
 */
public class SwapManaCostSpell extends Spell {

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		// Draw a minion from deck
		CardList minions = SpellUtils.getCards(player.getDeck(),
				card -> GameLogic.isCardType(card.getCardType(), CardType.MINION));
		Card drawnMinion = null;
		if (!minions.isEmpty()) {
			Card minionInDeck = context.getLogic().getRandom(minions);
			drawnMinion = context.getLogic().receiveCard(player.getId(), minionInDeck, source, true);
			if (drawnMinion.getZone() != Zones.HAND) {
				drawnMinion = null;
			}
		}

		// Draw a spell from deck
		CardList spells = SpellUtils.getCards(player.getDeck(),
				card -> GameLogic.isCardType(card.getCardType(), CardType.SPELL));
		Card drawnSpell = null;
		if (!spells.isEmpty()) {
			Card spellInDeck = context.getLogic().getRandom(spells);
			drawnSpell = context.getLogic().receiveCard(player.getId(), spellInDeck, source, true);
			if (drawnSpell.getZone() != Zones.HAND) {
				drawnSpell = null;
			}
		}

		// If both were drawn, swap their costs
		if (drawnMinion != null && drawnSpell != null) {
			int minionCost = drawnMinion.getBaseManaCost();
			int spellCost = drawnSpell.getBaseManaCost();

			// Set the minion's cost to the spell's original cost
			CardCostModifierDesc minionModDesc = new CardCostModifierDesc(CardCostModifier.class);
			minionModDesc.put(CardCostModifierArg.TARGET, EntityReference.SELF);
			minionModDesc.put(CardCostModifierArg.OPERATION, AlgebraicOperation.SET);
			minionModDesc.put(CardCostModifierArg.VALUE, spellCost);
			CardCostModifier minionMod = minionModDesc.create();
			if (source != null && source.getSourceCard() != null) {
				minionMod.setSourceCard(source.getSourceCard());
			}
			context.getLogic().addEnchantment(player, minionMod, source, drawnMinion);

			// Set the spell's cost to the minion's original cost
			CardCostModifierDesc spellModDesc = new CardCostModifierDesc(CardCostModifier.class);
			spellModDesc.put(CardCostModifierArg.TARGET, EntityReference.SELF);
			spellModDesc.put(CardCostModifierArg.OPERATION, AlgebraicOperation.SET);
			spellModDesc.put(CardCostModifierArg.VALUE, minionCost);
			CardCostModifier spellMod = spellModDesc.create();
			if (source != null && source.getSourceCard() != null) {
				spellMod.setSourceCard(source.getSourceCard());
			}
			context.getLogic().addEnchantment(player, spellMod, source, drawnSpell);
		}
	}
}
