package com.hiddenswitch.proto3.net.impl;

import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.proto3.net.Cards;
import com.hiddenswitch.proto3.net.Service;
import com.hiddenswitch.proto3.net.models.*;
import com.hiddenswitch.proto3.net.util.Broker;
import io.vertx.core.Future;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.CardCollection;
import net.demilich.metastone.game.cards.CardParseException;
import net.demilich.metastone.game.decks.DeckFormat;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by bberman on 1/20/17.
 */
public class CardsImpl extends Service<CardsImpl> implements Cards {
	private Random random = new Random();

	@Override
	public void start() {
		Broker.of(this, Cards.class, vertx.eventBus());
	}

	@Override
	@Suspendable
	public GetCardResponse getCard(GetCardRequest request) {
		return null;
	}

	@Override
	@Suspendable
	public QueryCardsResponse queryCards(QueryCardsRequest request) {
		// For now, just use the CardCatalogue
		try {
			CardCatalogue.loadCardsFromPackage();
		} catch (IOException | URISyntaxException | CardParseException e) {
			throw new RuntimeException("Could not load cards in CardsImpl::queryCards.");
		}

		final QueryCardsResponse response;

		if (request.isBatchRequest()) {
			response = new QueryCardsResponse()
					.withCards(new ArrayList<>());

			for (QueryCardsRequest request1 : request.getRequests()) {
				response.append(this.queryCards(request1));
			}
		} else {
			CardCollection results = CardCatalogue.query(new DeckFormat()
					.withCardSets(request.getSets()), (Card card) -> {
				boolean passes = true;

				if (request.getRarity() != null) {
					passes &= card.getRarity().isRarity(request.getRarity());
				}

				passes &= card.isCollectible();
				return passes;
			});

			int count = results.getCount();

			if (request.isRandomCountRequest()) {
				results.shuffle(random);
				count = Math.min(request.getRandomCount(), count);
			}

			List<Card> cards = results.toList();
			if (count != 0) {
				cards = new ArrayList<>(cards.subList(0, count));
			}

			response = new QueryCardsResponse()
					.withCards(cards);
		}

		return response;
	}

	@Override
	@Suspendable
	public InsertCardResponse insertCard(InsertCardRequest request) {
		return null;
	}

	@Override
	@Suspendable
	public UpdateCardResponse updateCard(UpdateCardRequest request) {
		return null;
	}
}
