package com.hiddenswitch.framework.impl;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.spells.rogue.ChoiceSpell;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class RogueChoiceGameContext extends GameContext {

	public RogueChoiceGameContext(@NotNull CardCatalogue cardCatalogue, String heroClass, CardList deck, String userId, String deckId, long seed) {
		super(cardCatalogue, Objects.requireNonNull(cardCatalogue.getFormat("Rogue")));

		var player = new Player(heroClass, cardCatalogue);
		player.setName(userId);
		player.setAttribute(Attribute.USER_ID, userId);
		player.setAttribute(Attribute.DECK_ID, deckId);
		player.getDeck().addAll(deck);

		setPlayer1(player);

		setPlayer2(new Player(heroClass, cardCatalogue));
		
		setLogic(new GameLogic(seed));
	}

	@Override
	public void init() {
		currentContext.set(this);
		getLogic().contextReady();
		startTrace();

		setupExternalLogicComponents();

		for (var card : getPlayer1().getDeck()) {
			if (card.getDesc().getRogueInfo() == null) continue;

			var triggers = card.getDesc().getRogueInfo().getTriggers();

			if (triggers == null) continue;

			for (var trigger : triggers) {

				getLogic().addEnchantment(getPlayer1(), card, card, card, trigger, true);
			}
		}
	}

	@Override
	public void init(int startingPlayerId) {
		init();
	}

	public void setupExternalLogicComponents() {
		externalLogicComponents.put(ChoiceSpell.class, new ChoiceSpellImpl());
	}
}
