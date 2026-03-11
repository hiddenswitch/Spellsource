package com.hiddenswitch.framework.impl;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.rogue.ChoiceSpell;

import static io.vertx.await.Async.await;

public class ChoiceSpellImpl extends ChoiceSpell {

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		var rogueRun = await(RogueManager.getRogueRun((String) player.getAttribute(Attribute.DECK_ID)));

		String[] cards;

		if (desc.containsKey(SpellArg.CARD_FILTER)
				|| desc.containsKey(SpellArg.CARD_SOURCE)) {
			cards = desc.getFilteredCards(context, player, source).stream().map(Card::getCardId).toArray(String[]::new);
		} else {
			cards = desc.getCards();
		}

		var index = desc.getInt(SpellArg.VALUE, -1);
		var pick = desc.getInt(SpellArg.HOW_MANY, 1);


		await(RogueManager.addNewRogueChoice(rogueRun.getId(), cards, index, pick));
	}
}
