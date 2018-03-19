package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.cards.*;
import net.demilich.metastone.game.cards.desc.CardDesc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.demilich.metastone.game.utils.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;

public class CreateSummonSpell extends Spell {

	Logger logger = LoggerFactory.getLogger(CreateSummonSpell.class);

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		String description = "";
		CardDesc CardDesc = new CardDesc();
		CardDesc.id = context.getLogic().generateCardId();
		CardDesc.name = desc.getString(SpellArg.NAME);
		CardDesc.baseAttack = desc.getValue(SpellArg.ATTACK_BONUS, context, player, target, source, 0);
		CardDesc.baseHp = desc.getValue(SpellArg.HP_BONUS, context, player, target, source, 0);
		CardDesc.heroClass = HeroClass.ANY;
		CardDesc.type = CardType.MINION;
		CardDesc.rarity = Rarity.FREE;
		CardDesc.description = description;
		Attribute attribute = (Attribute) desc.get(SpellArg.ATTRIBUTE);
		if (attribute != null) {
			CardDesc.attributes.put(attribute, true);
		}
		CardDesc.set = CardSet.BASIC;
		CardDesc.collectible = false;
		CardDesc.baseManaCost = desc.getValue(SpellArg.MANA, context, player, target, source, 0);
		Card newCard = CardDesc.create();
		context.addTempCard(newCard);

		int boardPosition = SpellUtils.getBoardPosition(context, player, desc, source);
		int count = desc.getValue(SpellArg.VALUE, context, player, target, source, 1);
		SpellDesc spell = (SpellDesc) desc.get(SpellArg.SPELL);
		SpellDesc successfulSummonSpell = (SpellDesc) desc.get(SpellArg.SPELL1);
		for (int i = 0; i < count; i++) {
			Card card = newCard.clone();
			Minion minion = card.summon();
			if (context.getLogic().summon(player.getId(), minion, null, boardPosition, false) && successfulSummonSpell != null) {
				SpellUtils.castChildSpell(context, player, successfulSummonSpell, source, minion, minion);
			}
			SpellUtils.castChildSpell(context, player, spell, source, target, minion);
		}
	}
}
