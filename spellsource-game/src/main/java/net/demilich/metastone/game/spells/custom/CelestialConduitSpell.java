package net.demilich.metastone.game.spells.custom;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.spells.DiscoverSpell;
import net.demilich.metastone.game.spells.ReceiveCardSpell;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.SpellUtils;
import net.demilich.metastone.game.spells.desc.LambdaSpellDesc;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Discovers cards from {@link net.demilich.metastone.game.spells.SpellUtils#getCards(GameContext, Player, Entity,
 * Entity, SpellDesc)} until their total cost is greater or equal to {@link net.demilich.metastone.game.spells.desc.SpellArg#SECONDARY_VALUE}.
 */
public final class CelestialConduitSpell extends Spell {

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		// Create an atomic integer "sum".
		// Then, we will tap into the card that was received using a LambdaSpellDesc.
		// "Tap into" means, we are trying to get the cost of the card received by the discover here:
		// {"class": "DiscoverSpell",
		//  "spell": {
		//    "class": "ReceiveCardSpell",
		//    "spell": {
		//      "class": <Anonymous Spell Class created by the LambdaSpellDesc>,
		//      "target": "OUTPUT" <--get the cost of this card, and add it to the "sum"
		//    }
		// }}
		// This is the only way to get a reference to the card received by the spell discover, and hoist it "back up" to
		// the top of the card.
		var sum = new AtomicInteger();
		var howMany = desc.getValue(SpellArg.HOW_MANY, context, player, target, source, 3);
		var max = desc.getValue(SpellArg.SECONDARY_VALUE, context, player, target, source, 15);

		// This is called by ReceiveCardSpell to add the cost of the card that was received to the sum variable.
		var addToSumSpell = new LambdaSpellDesc((lambdaContext, lambdaPlayer, lambdaDesc, lambdaSource, lambdaTargets, logger) -> {
			if (lambdaTargets == null || lambdaTargets.isEmpty()) {
				sum.set(Integer.MAX_VALUE);
				logger.error("cast {} {}: Unexpected lack of targets", lambdaContext.getGameId(), lambdaSource);
				return;
			}
			for (var receivedCard : lambdaTargets) {
				sum.addAndGet(lambdaContext.getLogic().getModifiedManaCost(lambdaPlayer, receivedCard.getSourceCard()));
			}
		});

		// Make the addToSum spell add the mana cost of the OUTPUT of the receive card spell
		addToSumSpell.put(SpellArg.TARGET, EntityReference.OUTPUT);

		// The receive card spell that puts the card from the discover into the player's hand
		var receiveCardSpell = ReceiveCardSpell.create();

		// The way we tap into the received card's mana cost
		receiveCardSpell.put(SpellArg.SPELL, addToSumSpell);

		// The discover spell which will cast the receive card spell on the selected card
		var discoverSpell = DiscoverSpell.create(receiveCardSpell);
		discoverSpell.put(SpellArg.HOW_MANY, howMany);
		discoverSpell.put(SpellArg.CARD_SOURCE, desc.getCardSource());
		discoverSpell.put(SpellArg.CARDS, desc.getCards());
		discoverSpell.put(SpellArg.CARD_FILTER, desc.getCardFilter());
		var invocations = 0;
		while (sum.get() <= max && invocations < GameLogic.MAX_HAND_CARDS) {
			if (Thread.currentThread().isInterrupted()) {
				break;
			}
			SpellUtils.castChildSpell(context, player, discoverSpell, source, target);
			invocations++;
		}
	}
}
