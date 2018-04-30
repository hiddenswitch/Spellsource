package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.cards.*;
import net.demilich.metastone.game.cards.desc.CardDesc;
import net.demilich.metastone.game.spells.custom.CreateCardFromChoicesSpell;
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

/**
 * @deprecated This spell is fairly brittle and you will be better off implementing the intended effects directly. See
 * {@link CreateCardFromChoicesSpell} for an example.
 */
@Deprecated
public class CreateSummonSpell extends Spell {

	Logger logger = LoggerFactory.getLogger(CreateSummonSpell.class);

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		String description = "";
		CardDesc cardDesc = new CardDesc();
		cardDesc.id = context.getLogic().generateCardId();
		cardDesc.name = desc.getString(SpellArg.NAME);
		cardDesc.baseAttack = desc.getValue(SpellArg.ATTACK_BONUS, context, player, target, source, 0);
		cardDesc.baseHp = desc.getValue(SpellArg.HP_BONUS, context, player, target, source, 0);
		cardDesc.heroClass = HeroClass.ANY;
		cardDesc.type = CardType.MINION;
		cardDesc.rarity = Rarity.FREE;
		cardDesc.description = description;
		Attribute attribute = (Attribute) desc.get(SpellArg.ATTRIBUTE);
		if (attribute != null) {
			cardDesc.attributes.put(attribute, true);
		}
		cardDesc.set = CardSet.BASIC;
		cardDesc.collectible = false;
		cardDesc.baseManaCost = desc.getValue(SpellArg.MANA, context, player, target, source, 0);
		Card newCard = cardDesc.create();
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
