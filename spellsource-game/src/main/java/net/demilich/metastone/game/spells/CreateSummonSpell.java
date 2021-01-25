package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import com.hiddenswitch.spellsource.rpc.Spellsource.CardTypeMessage.CardType;
import com.hiddenswitch.spellsource.rpc.Spellsource.RarityMessage.Rarity;
import net.demilich.metastone.game.cards.desc.CardDesc;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.spells.custom.CreateCardFromChoicesSpell;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.cards.Attribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This spell is fairly brittle and you will be better off implementing the intended effects directly. See {@link
 * CreateCardFromChoicesSpell} for an example.
 */
@Deprecated
public class CreateSummonSpell extends Spell {

	Logger logger = LoggerFactory.getLogger(CreateSummonSpell.class);

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		String description = "";
		CardDesc cardDesc = new CardDesc();
		cardDesc.setId(context.getLogic().generateCardId());
		cardDesc.setName(desc.getString(SpellArg.NAME));
		if (desc.containsKey(SpellArg.RACE)) {
			cardDesc.setRace((String) desc.get(SpellArg.RACE));
		}
		cardDesc.setBaseAttack(desc.getValue(SpellArg.ATTACK_BONUS, context, player, target, source, 0));
		cardDesc.setBaseHp(desc.getValue(SpellArg.HP_BONUS, context, player, target, source, 0));
		cardDesc.setHeroClass(HeroClass.ANY);
		cardDesc.setType(CardType.MINION);
		cardDesc.setRarity(Rarity.FREE);
		cardDesc.setDescription(description);
		Attribute attribute = (Attribute) desc.get(SpellArg.ATTRIBUTE);
		if (attribute != null) {
			cardDesc.getAttributes().put(attribute, true);
		}
		cardDesc.setSet("BASIC");
		cardDesc.setCollectible(false);
		cardDesc.setBaseManaCost(desc.getValue(SpellArg.MANA, context, player, target, source, 0));
		Card newCard = cardDesc.create();
		context.addTempCard(newCard);

		int boardPosition = SpellUtils.getBoardPosition(context, player, desc, source);
		int count = desc.getValue(SpellArg.VALUE, context, player, target, source, 1);
		SpellDesc spell = (SpellDesc) desc.get(SpellArg.SPELL);
		SpellDesc successfulSummonSpell = (SpellDesc) desc.get(SpellArg.SPELL1);
		for (int i = 0; i < count; i++) {
			Card card = newCard.clone();
			Minion minion = card.minion();
			if (context.getLogic().summon(player.getId(), minion, source, boardPosition, false) && successfulSummonSpell != null) {
				Entity newMinion = minion.transformResolved(context);
				if (minion.isInPlay()) {
					SpellUtils.castChildSpell(context, player, successfulSummonSpell, source, newMinion, newMinion);
				}
			}
			if (spell != null) {
				SpellUtils.castChildSpell(context, player, spell, source, target, minion);
			}
		}
	}
}
