package com.hiddenswitch.framework.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.hiddenswitch.framework.Environment;
import com.hiddenswitch.framework.rpc.Hiddenswitch;
import com.hiddenswitch.framework.schema.spellsource.tables.daos.CardsDao;
import com.hiddenswitch.framework.schema.spellsource.tables.pojos.Cards;
import com.hiddenswitch.spellsource.rpc.Spellsource;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.binder.BaseUnits;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import io.vertx.await.Async;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.impl.ContextInternal;
import io.vertx.core.json.jackson.DatabindCodec;
import io.vertx.pgclient.pubsub.PgSubscriber;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.behaviour.GameStateValueBehaviour;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.cards.catalogues.ListCardCatalogue;
import net.demilich.metastone.game.cards.desc.CardDesc;
import net.demilich.metastone.game.decks.DeckFormat;
import net.openhft.hashing.LongHashFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.redisson.api.RBucket;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import static com.hiddenswitch.framework.schema.spellsource.tables.Cards.CARDS;
import static io.micrometer.core.instrument.Metrics.globalRegistry;
import static io.vertx.await.Async.await;

public class SqlCachedCardCatalogue extends ListCardCatalogue {
	public static final RedissonProtobufCodec GET_CARDS_RESPONSE_CODEC = new RedissonProtobufCodec(Hiddenswitch.GetCardsResponse.parser());
	private static final String SPELLSOURCE_CARDS_CHANGES_CHANNEL_FROM_DDL = "spellsource_cards_changes_v0";
	private static final Counter.Builder REQUESTS = Counter.builder("cards.cached.request.count")
			.description("The number of requests for cards that hit the Redis cache.")
			.baseUnit(BaseUnits.OBJECTS);
	private static final Counter.Builder REQUESTS_BYTES = Counter.builder("cards.cached.request.bytes")
			.description("The number of bytes for card requests that were retrieved from the Redis cache.")
			.baseUnit(BaseUnits.BYTES);
	private static final Counter CARD_INVALIDATIONS = Counter.builder("cards.invalidations")
			.description("The number of invalidations of cards received from Postgres.")
			.baseUnit(BaseUnits.ROWS)
			.register(globalRegistry);
	private static final Counter CARD_LOADS = Counter.builder("cards.loads")
			.description("The number of card records loaded into the card catalogue.")
			.baseUnit(BaseUnits.ROWS)
			.register(globalRegistry);
	private static final String BUCKET_HAS_CONTENT_TAG = "bucket_has_content";
	private static final String CLIENT_CACHE_HIT_TAG = "client_cache_hit";
	private static final String DATA_EXPIRED_TAG = "data_expired";
	private final Subject<String> userInvalidations = PublishSubject.create();
	Deque<String> invalidated = new ConcurrentLinkedDeque<>();
	WeakVertxMap<PgSubscriber> subscribers = new WeakVertxMap<>(vertx -> PgSubscriber.subscriber(vertx, Environment.pgArgs().connectionOptions()));
	private PgSubscriber subscriber;
	private String gitUserId;
	private GameContext workingContext;

	public static void invalidateGitCardsFile() {
		var buckets = getBucketsForUser(Environment.getSpellsourceUserId());
		buckets.data().delete();
		buckets.head().delete();
	}

	private static Buckets getBucketsForUser(String userId) {
		var redisson = Environment.redisson();
		var head = redisson.<Hiddenswitch.GetCardsResponse>getBucket(String.format("%s:head", userId), GET_CARDS_RESPONSE_CODEC);
		var data = redisson.<Hiddenswitch.GetCardsResponse>getBucket(String.format("%s:data", userId), GET_CARDS_RESPONSE_CODEC);
		return new Buckets(head, data);
	}

	public void invalidateAllAndRefresh() {
		if (!Thread.currentThread().isVirtual()) {
			throw new IllegalStateException("must run in virtual");
		}

		Async.lock(lock.writeLock());
		gitUserId = Environment.getSpellsourceUserId();
		try {
			invalidated.clear();
			var cardsDao = new CardsDao(Environment.jooqAkaDaoConfiguration(), Environment.sqlClient());
			// this gets all the cards in SQL by all the users that are published
			// is this really what we want to do? probably, for now. It will take hundreds of thousands of cards for this to
			// make a significant impact on performance
			// really we should operate the SqlCachedCardCatalogue cards object as a TTL'd or LRU'd cache. or, replicate the
			// cards into a read-only sql database that runs side by side with the java process. would that be any faster?
			// we're not the only people on earth with this problem.
			var cardDbRecords = await(cardsDao.findManyByCondition(CARDS.IS_PUBLISHED.eq(true).and(CARDS.IS_ARCHIVED.eq(false))));
			clear();
			loadFromCardDbRecords(cardDbRecords);
			workingContext = new GameContext(this, this.spellsource());
		} finally {
			lock.writeLock().unlock();
		}
	}

	public Future<Void> subscribe() {
		if (subscriber != null) {
			return Future.succeededFuture();
		}

		// todo: pool these connections
		this.subscriber = subscribers.get();
		// todo: is this necessary? i think i am always on a proper context
		var context = (ContextInternal) Vertx.currentContext();
		subscriber.channel(SPELLSOURCE_CARDS_CHANGES_CHANNEL_FROM_DDL)
				.handler(payload -> context.runOnContext(v -> {
					try {
						CARD_INVALIDATIONS.increment();
						var decoded = DatabindCodec.mapper().readValue(payload, SpellsourceCardChangesChannelNotification.class);
						// todo: should we just send the whole card here? what are the downsides?
						invalidateCardForGameplayPurposes(decoded.id());
						// these are throttled per user
						userInvalidations.onNext(decoded.createdBy());
					} catch (JsonProcessingException e) {
						throw new RuntimeException(e);
					}
				}));


		var debounceInvalidations = Observable.merge(userInvalidations
						.groupBy(s -> s)
						.map(group -> group.debounce(8, TimeUnit.SECONDS)))
				.subscribe(this::invalidateUserGetCardsResponseForUnityCollection);

		context.addCloseHook(completion -> {
			debounceInvalidations.dispose();
			completion.tryComplete();
		});

		return subscriber.connect();
	}

	private void loadFromCardDbRecords(List<Cards> cardDbRecords) {
		var cardDescs = cardDbRecords.stream()
				.map(card -> {
					var cardJson = card.getCardScript().put("id", card.getId());
					var cardDesc = cardJson.mapTo(CardDesc.class);
					// todo: should there be a dedicated metadata field for this? it could be mutated by buggy code
					var attributes = cardDesc.getAttributes();
					attributes.put(Attribute.CREATED_BY_USER_ID, card.getCreatedBy());
					if (card.getIsPublished()) {
						attributes.put(Attribute.PUBLISHED, true);
					}
					if (card.getIsArchived()) {
						attributes.put(Attribute.ARCHIVED, true);
					}
					return cardDesc;
				})
				.toList();
		loadCards(cardDescs);
	}

	public void invalidateCardForGameplayPurposes(String cardId) {
		invalidated.add(cardId);
	}

	@Override
	public @NotNull CardList query(DeckFormat deckFormat, Spellsource.CardTypeMessage.CardType cardType, Spellsource.RarityMessage.Rarity rarity, String heroClass, Attribute tag, boolean clone) {
		refreshInvalidCardsFromSql();
		return super.query(deckFormat, cardType, rarity, heroClass, tag, clone);
	}

	@Override
	public @NotNull CardList getAll() {
		refreshInvalidCardsFromSql();
		return super.getAll();
	}

	@Override
	public @NotNull Card getCardById(@NotNull String id) {
		refreshInvalidCardsFromSql();
		return super.getCardById(id);
	}

	@Override
	public @Nullable Card getCardByName(String name) {
		refreshInvalidCardsFromSql();
		return super.getCardByName(name);
	}

	@Override
	public @NotNull Card getCardByName(String name, String heroClass) {
		refreshInvalidCardsFromSql();
		return super.getCardByName(name, heroClass);
	}

	@Override
	public @NotNull Map<String, Card> getCards() {
		refreshInvalidCardsFromSql();
		return super.getCards();
	}

	@Override
	public DeckFormat getFormat(String name) {
		refreshInvalidCardsFromSql();
		return super.getFormat(name);
	}

	@Override
	public Card getHeroCard(String heroClass) {
		refreshInvalidCardsFromSql();
		return super.getHeroCard(heroClass);
	}

	@Override
	public Card getFormatCard(String name) {
		refreshInvalidCardsFromSql();
		return super.getFormatCard(name);
	}

	@Override
	public CardList getClassCards(DeckFormat format) {
		refreshInvalidCardsFromSql();
		return super.getClassCards(format);
	}

	@Override
	public List<String> getBaseClasses(DeckFormat deckFormat) {
		refreshInvalidCardsFromSql();
		return super.getBaseClasses(deckFormat);
	}

	@Override
	public CardList queryClassCards(DeckFormat format, String hero, Set<String> bannedCards, Spellsource.RarityMessage.Rarity rarity, Set<Spellsource.CardTypeMessage.CardType> validCardTypes) {
		refreshInvalidCardsFromSql();
		return super.queryClassCards(format, hero, bannedCards, rarity, validCardTypes);
	}

	@Override
	public CardList queryNeutrals(DeckFormat format, Set<String> bannedCards, Spellsource.RarityMessage.Rarity rarity, Set<Spellsource.CardTypeMessage.CardType> validCardTypes) {
		refreshInvalidCardsFromSql();
		return super.queryNeutrals(format, bannedCards, rarity, validCardTypes);
	}

	@Override
	public CardList queryUncollectible(DeckFormat deckFormat) {
		refreshInvalidCardsFromSql();
		return super.queryUncollectible(deckFormat);
	}

	private void refreshInvalidCardsFromSql() {
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
			if (Thread.currentThread().isVirtual()) {
				Async.lock(lock.writeLock());
				var cardDbRecords = await(getCards);
				try {
					loadFromCardDbRecords(cardDbRecords);
				} finally {
					lock.writeLock().unlock();
				}
			} else {
				// not sure we're ever in a non-virtual context
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

	private Hiddenswitch.GetCardsResponse createUserCards(String userId) {
		Predicate<Card> cardPredicate = card -> {
			var attributes = card.getDesc().getAttributes();
			return Objects.equals(attributes.get(Attribute.CREATED_BY_USER_ID), userId)
					&& card.hasAttribute(Attribute.PUBLISHED)
					&& !card.hasAttribute(Attribute.ARCHIVED)
					&& spellsource().isInFormat(card.getDesc().getSet())
					&& card.getCardType() != Spellsource.CardTypeMessage.CardType.GROUP;
		};

		return createCardsResponse(cardPredicate);
	}

	@Override
	public DeckFormat spellsource() {
		return getFormat("Spellsource");
	}

	@Override
	public DeckFormat defaultFormat() {
		return spellsource();
	}

	@NotNull
	private Hiddenswitch.GetCardsResponse createCardsResponse(Predicate<Card> cardPredicate) {
		var cards = getCards().values()
				.stream()
				.filter(cardPredicate)
				.map(card -> ModelConversions.getEntity(workingContext, card, 0))
				.map(card -> Spellsource.CardRecord.newBuilder().setEntity(card))
				.toList();

		var builder = Hiddenswitch.GetCardsResponse.Content.newBuilder();

		for (var i = 0; i < cards.size(); i++) {
			cards.get(i).getEntityBuilder().setId(i);
			builder.addCards(cards.get(i).build());
		}

		var built = builder.build();
		var checksum = LongHashFunction.xx().hashBytes(built.toByteArray());

		var response = Hiddenswitch.GetCardsResponse.newBuilder()
				.setVersion(Long.toString(checksum));

		response.setContent(built);

		return response.build();

	}

	private void invalidateUserGetCardsResponseForUnityCollection(String userId) {
		var buckets = getBucketsForUser(userId);
		buckets.head().delete();
		buckets.data().delete();
	}

	public Hiddenswitch.GetCardsResponse cachedRequest(Hiddenswitch.GetCardsRequest request) {
		var userId = request.getUserId();
		if (userId.isBlank()) {
			userId = gitUserId;
		}

		var buckets = getBucketsForUser(userId);
		var ttl = Objects.equals(userId, gitUserId) ? Duration.ofDays(Integer.MAX_VALUE) : Duration.ofDays(1);
		boolean bucketHasContent;
		boolean clientCacheHit;
		boolean dataExpired = false;

		Hiddenswitch.GetCardsResponse response;
		var head = await(buckets.head().getAsync().toCompletableFuture());

		bucketHasContent = head != null;
		clientCacheHit = bucketHasContent && Objects.equals(head.getVersion(), request.getIfNoneMatch());
		if (bucketHasContent && clientCacheHit) {
			response = Hiddenswitch.GetCardsResponse.newBuilder()
					.setCachedOk(true)
					.build();
		} else {
			var data = await(buckets.data().getAndExpireAsync(ttl).toCompletableFuture());

			dataExpired = data == null;
			if (dataExpired) {
				// compute
				data = createUserCards(userId);
				head = Hiddenswitch.GetCardsResponse.newBuilder().setVersion(data.getVersion()).build();
				buckets.head().setAsync(head);
				buckets.data().setAsync(data, ttl);
			}

			response = data;
		}

		var tags = Tags.of(
				BUCKET_HAS_CONTENT_TAG, Boolean.toString(bucketHasContent),
				CLIENT_CACHE_HIT_TAG, Boolean.toString(clientCacheHit),
				DATA_EXPIRED_TAG, Boolean.toString(dataExpired));
		REQUESTS.tags(tags).register(globalRegistry).increment();
		REQUESTS_BYTES.tags(tags).register(globalRegistry).increment(response.getSerializedSize());

		return response;
	}

	@Override
	protected void updatedWith(Map<String, CardDesc> cardDescs) {
		CARD_LOADS.increment(cardDescs.size());
		super.updatedWith(cardDescs);
	}

	record SpellsourceCardChangesChannelNotification(String __table, String id, String createdBy) {
	}

	record Buckets(RBucket<Hiddenswitch.GetCardsResponse> head, RBucket<Hiddenswitch.GetCardsResponse> data) {
	}

}
