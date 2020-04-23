package net.demilich.metastone.game.spells.custom;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.DiscoverAction;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardArrayList;
import net.demilich.metastone.game.cards.CardList;
import com.hiddenswitch.spellsource.client.models.CardType;
import net.demilich.metastone.game.cards.desc.CardDesc;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.spells.*;
import net.demilich.metastone.game.spells.desc.OpenerDesc;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.TargetSelection;

/**
 * Gibes a minion {@link SpellArg#HOW_MANY} battlecries that perform the selected spells from {@link
 * net.demilich.metastone.game.spells.desc.filter.CardFilter}.
 */
public class CreateAndTeachSpell extends Spell {

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		int value = desc.getValue(SpellArg.VALUE, context, player, target, source, 1);
		for (int i = 0; i < value; i++) {
			if (player.getHand().size() == GameLogic.MAX_HAND_CARDS) {
				continue;
			}

			CardDesc minionCardDesc = new CardDesc();
			minionCardDesc.setType(CardType.MINION);
			minionCardDesc.setId(context.getLogic().generateCardId());
			minionCardDesc.setBaseManaCost(desc.getValue(SpellArg.MANA, context, player, target, source, 5));
			minionCardDesc.setBaseAttack(desc.getValue(SpellArg.ATTACK_BONUS, context, player, target, source, 5));
			minionCardDesc.setBaseHp(desc.getValue(SpellArg.HP_BONUS, context, player, target, source, 5));
			minionCardDesc.setName(desc.getString(SpellArg.NAME));
			minionCardDesc.setDescription(desc.getString(SpellArg.DESCRIPTION));
			if (source != null) {
				minionCardDesc.setHeroClass(source.getSourceCard().getHeroClass());
			}

			CardList chosenSpells = new CardArrayList();
			int howManyTeach = desc.getValue(SpellArg.HOW_MANY, context, player, target, source, 1);
			TargetSelection chosenTargetSelection = null;
			for (int j = 0; j < howManyTeach; j++) {
				CardList spells = new CardArrayList();
				spells.addAll(SpellUtils.getCards(context, player, target, source, desc, 99));
				spells.removeAll(chosenSpells);
				if (chosenTargetSelection != null) {
					spells.removeIf(card -> card.getTargetSelection() != TargetSelection.NONE);
				}
				spells.shuffle(context.getLogic().getRandom());
				spells = new CardArrayList(spells.subList(0, Math.min(spells.size(), 3)));

				SpellDesc nullSpell = NullSpell.create();
				// Eyeroll emoji.
				nullSpell.put(SpellArg.SPELL, NullSpell.create());

				DiscoverAction discoverAction = SpellUtils.discoverCard(context, player, source, nullSpell, spells);
				Card chosenSpell = discoverAction.getCard();
				chosenSpells.add(chosenSpell);
				if (chosenSpell.getTargetSelection() != TargetSelection.NONE) {
					chosenTargetSelection = chosenSpell.getTargetSelection();
				}
			}


			SpellDesc metaSpellDesc = new SpellDesc(MetaSpell.class);
			SpellDesc[] spells = new SpellDesc[chosenSpells.size()];

			for (int k = 0; k < chosenSpells.size(); k++) {
				Card spell = chosenSpells.get(k);
				spells[k] = CastCardsSpell.create(spell.getCardId());
				minionCardDesc.setDescription(minionCardDesc.getDescription().replace("LESSON" + (k + 1), spell.getName()));
			}
			metaSpellDesc = metaSpellDesc.addArg(SpellArg.SPELLS, spells);
			OpenerDesc openerDesc = new OpenerDesc();
			openerDesc.setSpell(metaSpellDesc);
			openerDesc.setTargetSelection(chosenTargetSelection);
			minionCardDesc.setBattlecry(openerDesc);

			Card card = minionCardDesc.create();
			context.addTempCard(card);
			context.getLogic().receiveCard(player.getId(), card);
		}


	}
}
