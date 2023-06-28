package com.hiddenswitch.framework.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.hiddenswitch.framework.Environment;
import com.hiddenswitch.framework.schema.spellsource.tables.daos.CardsDao;
import com.hiddenswitch.framework.schema.spellsource.tables.pojos.Cards;
import com.hiddenswitch.spellsource.rpc.Spellsource;
import io.vertx.await.Async;
import io.vertx.await.impl.VirtualThreadContext;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.impl.ContextInternal;
import io.vertx.core.json.jackson.DatabindCodec;
import io.vertx.pgclient.pubsub.PgSubscriber;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.behaviour.GameStateValueBehaviour;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogueRecord;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.cards.catalogues.ListCardCatalogue;
import net.demilich.metastone.game.cards.desc.CardDesc;
import net.demilich.metastone.game.decks.DeckFormat;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;

import static com.hiddenswitch.framework.schema.spellsource.tables.Cards.CARDS;
import static io.vertx.await.Async.await;

public class SqlCachedCardCatalogue extends ListCardCatalogue {
	public static final String SPELLSOURCE_CARDS_CHANGES_CHANNEL_FROM_DDL = "spellsource_cards_changes_v0";
	Deque<String> invalidated = new ConcurrentLinkedDeque<>();
	WeakVertxMap<PgSubscriber> subscribers = new WeakVertxMap<>(vertx -> PgSubscriber.subscriber(vertx, Environment.pgArgs().connectionOptions()));
	private PgSubscriber subscriber;

	public Future<Void> invalidateAllAndRefreshOnce() {
		Async.lock(lock.writeLock());
		try {
			if (cards.isEmpty()) {
				invalidateAllAndRefresh();
			}
		} finally {
			lock.writeLock().unlock();
		}

		return Future.succeededFuture();
	}

	public void invalidateAllAndRefresh() {
		if (!Thread.currentThread().isVirtual()) {
			throw new IllegalStateException("must run in virtual");
		}
		Async.lock(lock.writeLock());
		try {
			invalidated.clear();
			var cardsDao = new CardsDao(Environment.jooqAkaDaoConfiguration(), Environment.sqlClient());
			var cardDbRecords = await(cardsDao.findManyByCondition(CARDS.IS_PUBLISHED.eq(true).and(CARDS.IS_ARCHIVED.eq(false))));
			clear();
			loadFromCardDbRecords(cardDbRecords);
		} finally {
			lock.writeLock().unlock();
		}
	}

	public void invalidateAll() {
		invalidated.addAll(cards.keySet());
	}

	public Future<Void> subscribe() {
		if (subscriber != null) {
			return Future.succeededFuture();
		}

		// todo: pool these connections
		this.subscriber = subscribers.get();
		var context = Vertx.currentContext();
		subscriber.channel(SPELLSOURCE_CARDS_CHANGES_CHANNEL_FROM_DDL).handler(payload -> context.runOnContext(v -> {
			try {
				var decoded = DatabindCodec.mapper().readValue(payload, SpellsourceCardChangesChannelNotification.class);
				invalidate(decoded.id());
			} catch (JsonProcessingException e) {
				throw new RuntimeException(e);
			}
		}));

		return subscriber.connect();
	}

	private void loadFromCardDbRecords(List<Cards> cardDbRecords) {
		var cardDescs = cardDbRecords.stream().map(card -> card.getCardScript().put("id", card.getId()).mapTo(CardDesc.class)).toList();
		loadCards(cardDescs);
	}

	public void invalidate(String cardId) {
		invalidated.add(cardId);
	}

	@Override
	public @NotNull CardList query(DeckFormat deckFormat, Spellsource.CardTypeMessage.CardType cardType, Spellsource.RarityMessage.Rarity rarity, String heroClass, Attribute tag, boolean clone) {

		refreshInvalid();
		return super.query(deckFormat, cardType, rarity, heroClass, tag, clone);

	}

	@Override
	public @NotNull CardList getAll() {

		refreshInvalid();
		return super.getAll();

	}

	@Override
	public @NotNull Card getCardById(@NotNull String id) {

		refreshInvalid();
		return super.getCardById(id);

	}

	@Override
	public @Nullable Card getCardByName(String name) {
		refreshInvalid();
		return super.getCardByName(name);
	}

	@Override
	public @NotNull Card getCardByName(String name, String heroClass) {
		refreshInvalid();
		return super.getCardByName(name, heroClass);
	}

	@Override
	public @NotNull Map<String, Card> getCards() {
		refreshInvalid();
		return super.getCards();
	}

	@Override
	public DeckFormat getFormat(String name) {
		refreshInvalid();
		return super.getFormat(name);
	}

	@Override
	public @NotNull Map<String, CardCatalogueRecord> getRecords() {
		refreshInvalid();
		return super.getRecords();
	}

	@Override
	public Card getHeroCard(String heroClass) {
		refreshInvalid();
		return super.getHeroCard(heroClass);
	}

	@Override
	public Card getFormatCard(String name) {
		refreshInvalid();
		return super.getFormatCard(name);
	}

	@Override
	public CardList getClassCards(DeckFormat format) {
		refreshInvalid();
		return super.getClassCards(format);
	}

	@Override
	public List<String> getBaseClasses(DeckFormat deckFormat) {
		refreshInvalid();
		return super.getBaseClasses(deckFormat);
	}

	@Override
	public CardList queryClassCards(DeckFormat format, String hero, Set<String> bannedCards, Spellsource.RarityMessage.Rarity rarity, Set<Spellsource.CardTypeMessage.CardType> validCardTypes) {
		refreshInvalid();
		return super.queryClassCards(format, hero, bannedCards, rarity, validCardTypes);
	}

	@Override
	public CardList queryNeutrals(DeckFormat format, Set<String> bannedCards, Spellsource.RarityMessage.Rarity rarity, Set<Spellsource.CardTypeMessage.CardType> validCardTypes) {
		refreshInvalid();
		return super.queryNeutrals(format, bannedCards, rarity, validCardTypes);
	}

	@Override
	public CardList queryUncollectible(DeckFormat deckFormat) {
		refreshInvalid();
		return super.queryUncollectible(deckFormat);
	}

	private void refreshInvalid() {
		// if we're currently a bot, never refresh
		var gameContext = GameContext.current();
		if (gameContext != null && !gameContext.getBehaviours().get(gameContext.getActivePlayerId()).isHuman() && GameStateValueBehaviour.isInBotSimulation()) {
			return;
		}
		String invalid;
		Set<String> toFetch = null;
		while ((invalid = invalidated.pollFirst()) != null) {
			if (toFetch == null) {
				toFetch = new HashSet<>();
			}
			toFetch.add(invalid);
		}
		if (toFetch != null && !toFetch.isEmpty()) {
			var cardsDao = new CardsDao(Environment.jooqAkaDaoConfiguration(), Environment.sqlClient());
			var getCards = cardsDao.findManyByCondition(CARDS.ID.in(toFetch).and(CARDS.IS_PUBLISHED.eq(true)).and(CARDS.IS_ARCHIVED.eq(false)));
			if (Vertx.currentContext() != null && ((ContextInternal) Vertx.currentContext()).unwrap() instanceof VirtualThreadContext) {
				var cardDbRecords = await(getCards);
				Async.lock(lock.writeLock());
				try {
					loadFromCardDbRecords(cardDbRecords);
				} finally {
					lock.writeLock().unlock();
				}
			} else {
				getCards.onSuccess(cardDbRecords -> {
					Async.lock(lock.writeLock());
					try {
						loadFromCardDbRecords(cardDbRecords);
					} finally {
						lock.writeLock().unlock();
					}
				});
			}
		}
	}

	record SpellsourceCardChangesChannelNotification(String __table, String id) {
	}
}
