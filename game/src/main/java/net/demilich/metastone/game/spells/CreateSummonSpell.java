package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardSet;
import net.demilich.metastone.game.cards.CardType;
import net.demilich.metastone.game.cards.Rarity;
import net.demilich.metastone.game.cards.desc.CardDesc;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.entities.minions.Race;
import net.demilich.metastone.game.spells.custom.CreateCardFromChoicesSpell;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.cards.Attribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @deprecated This spell is fairly brittle and you will be better off implementing the intended effects directly. See
 * 		{@link CreateCardFromChoicesSpell} for an example.
 */
@Deprecated
public class CreateSummonSpell extends Spell {

	private static final long serialVersionUID = 2228912755221985590L;
	Logger logger = LoggerFactory.getLogger(CreateSummonSpell.class);

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		String description = "";
		CardDesc cardDesc = new CardDesc();
		cardDesc.setId(context.getLogic().generateCardId());
		cardDesc.setName(desc.getString(SpellArg.NAME));
		if (desc.containsKey(SpellArg.RACE)) {
			cardDesc.setRace((Race) desc.get(SpellArg.RACE));
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
		cardDesc.setSet(CardSet.BASIC);
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
			Minion minion = card.summon();
			if (context.getLogic().summon(player.getId(), minion, null, boardPosition, false) && successfulSummonSpell != null) {
				SpellUtils.castChildSpell(context, player, successfulSummonSpell, source, minion, minion);
			}
			SpellUtils.castChildSpell(context, player, spell, source, target, minion);
		}
	}
}
