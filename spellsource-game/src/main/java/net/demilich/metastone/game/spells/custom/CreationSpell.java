package net.demilich.metastone.game.spells.custom;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.DiscoverAction;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.spells.*;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.trigger.EnchantmentDesc;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerArg;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;
import net.demilich.metastone.game.spells.trigger.CardPlayedTrigger;
import net.demilich.metastone.game.targeting.TargetType;

/**
 * Creates a sideboard of {@link SpellArg#VALUE} cards.
 * <p>
 * The player discovers a card from the sideboard, and receives it. When that card is played, the player chooses another
 * card from the sideboard that remains, until none do.
 */
public final class CreationSpell extends Spell {

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		int value = desc.getValue(SpellArg.VALUE, context, player, target, source, 10);
		if (value == 0) {
			return;
		}
		CardList cards = SpellUtils.getCards(context, player, target, source, desc);
		if (cards.size() == 1) {
			context.getLogic().receiveCard(player.getId(), cards.get(0));
			return;
		}
		SpellDesc desc1 = NullSpell.create();
		desc1.put(SpellArg.SPELL,NullSpell.create());
		DiscoverAction chosen = SpellUtils.discoverCard(context, player, source, desc1, cards);
		if (player.getHand().size() >= GameLogic.MAX_HAND_CARDS) {
			return;
		}
		Card card = chosen.getCard().getCopy();
		String[] cardsLeft = cards.stream()
				.filter(c -> !c.getCardId().equals(card.getCardId()))
				.map(Card::getCardId)
				.toArray(String[]::new);
		int newValue = cardsLeft.length;
		context.getLogic().receiveCard(player.getId(), card);
		SpellDesc nextSpell = new SpellDesc(CreationSpell.class);
		nextSpell.put(SpellArg.CARDS, cardsLeft);
		nextSpell.put(SpellArg.VALUE, newValue);
		EnchantmentDesc nextCard = new EnchantmentDesc();
		nextCard.setMaxFires(1);
		nextCard.setSpell(nextSpell);
		EventTriggerDesc eventTrigger = CardPlayedTrigger.create();
		eventTrigger.put(EventTriggerArg.HOST_TARGET_TYPE, TargetType.IGNORE_OTHER_TARGETS);
		nextCard.setEventTrigger(eventTrigger);
		SpellUtils.castChildSpell(context, player, AddEnchantmentSpell.create(nextCard), source, card);
	}
}
