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
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.internal.ContextInternal;
import io.vertx.core.internal.VertxInternal;
import io.vertx.core.json.jackson.DatabindCodec;
import io.vertx.pgclient.pubsub.PgSubscriber;
import io.vertx.rxjava3.RxHelper;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.AttributeMap;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.cards.catalogues.ClasspathCardCatalogue;
import net.demilich.metastone.game.cards.catalogues.ListCardCatalogue;
import net.demilich.metastone.game.cards.desc.CardDesc;
import net.demilich.metastone.game.decks.DeckFormat;
import net.openhft.hashing.LongHashFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.redisson.api.RBucket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import static com.hiddenswitch.framework.schema.spellsource.tables.Cards.CARDS;
import static io.micrometer.core.instrument.Metrics.globalRegistry;
import static io.vertx.await.Async.await;

public class SqlCachedCardCatalogue extends ListCardCatalogue {
	private static final Logger LOGGER = LoggerFactory.getLogger(SqlCachedCardCatalogue.class);
	private static final String CARD_CACHE_FORMAT_VERSION = "2";
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
	private static final ConcurrentHashMap<String, ListCardCatalogue> GIT_CATALOGUE_BY_USER = new ConcurrentHashMap<>();
	private static final WeakVertxMap<SqlCachedCardCatalogue> VERTX_CATALOGUES = new WeakVertxMap<>(vertx -> {
		var cat = new SqlCachedCardCatalogue();
		cat.sharedPerVertx = true;
		return cat;
	});
	private final Subject<String> userInvalidations = PublishSubject.create();
	Deque<String> invalidated = new ConcurrentLinkedDeque<>();
	WeakVertxMap<PgSubscriber> subscribers = new WeakVertxMap<>(vertx -> PgSubscriber.subscriber(vertx, Environment.pgArgs().connectionOptions()));
	private PgSubscriber subscriber;
	private String gitUserId;
	private volatile GameContext workingContext;
	private boolean sharedPerVertx;
	private Promise<Void> initializedPromise;
	private final Map<String, String> lastModifiedByCard = new ConcurrentHashMap<>();
	private Future<Void> pendingCardRefresh = Future.succeededFuture();

	public String getCardLastModified(String cardId) {
		return lastModifiedByCard.get(cardId);
	}

	public static SqlCachedCardCatalogue forVertx() {
		return VERTX_CATALOGUES.get();
	}

	public void ensureInitialized() {
		Promise<Void> promise;
		boolean first = false;
		synchronized (this) {
			if (initializedPromise == null) {
				initializedPromise = Promise.promise();
				first = true;
			}
			promise = initializedPromise;
		}
		if (first) {
			try {
				invalidateAllAndRefresh();
				await(subscribe());
				promise.complete();
			} catch (Throwable t) {
				synchronized (this) {
					initializedPromise = null;
				}
				promise.fail(t);
				throw t;
			}
		} else {
			await(promise.future());
		}
	}

	public static void invalidateGitCardsFile() {
		var buckets = getBucketsForUser(Environment.getSpellsourceUserId());
		buckets.data().delete();
		buckets.head().delete();
	}

	private static Buckets getBucketsForUser(String userId) {
		var redisson = Environment.redisson();
		var head = redisson.<Hiddenswitch.GetCardsResponse>getBucket(String.format("Spellsource:cards:%s:%s:head", CARD_CACHE_FORMAT_VERSION, userId), GET_CARDS_RESPONSE_CODEC);
		var data = redisson.<Hiddenswitch.GetCardsResponse>getBucket(String.format("Spellsource:cards:%s:%s:data", CARD_CACHE_FORMAT_VERSION, userId), GET_CARDS_RESPONSE_CODEC);
		return new Buckets(head, data);
	}

	private static ListCardCatalogue getBaseGitCatalogue(String userId) {
		return GIT_CATALOGUE_BY_USER.computeIfAbsent(userId, uid -> {
			var base = new ListCardCatalogue();
			var gitCardDescs = ClasspathCardCatalogue.INSTANCE.getCards().values().stream()
					.map(card -> {
						var desc = card.getDesc().clone();
						var attributes = desc.getAttributes();
						if (attributes == null) {
							attributes = new AttributeMap();
							desc.setAttributes(attributes);
						}
						attributes.put(Attribute.CREATED_BY_USER_ID, uid);
						attributes.put(Attribute.PUBLISHED, true);
						return desc;
					})
					.toList();
			base.loadCards(gitCardDescs);
			return base;
		});
	}

	public void invalidateAllAndRefresh() {
		if (!Thread.currentThread().isVirtual()) {
			throw new IllegalStateException("must run in virtual");
		}
		ClasspathCardCatalogue.INSTANCE.loadCardsFromPackage();
		var resolvedGitUserId = Environment.getSpellsourceUserId();
		var baseCatalogue = getBaseGitCatalogue(resolvedGitUserId);
		lock.writeLock().lock();
		gitUserId = resolvedGitUserId;
		try {
			invalidated.clear();
			lastModifiedByCard.clear();
			copyFrom(baseCatalogue);
			CARD_LOADS.increment(baseCatalogue.getCards().size());

			var cardsDao = new CardsDao(Environment.jooqAkaDaoConfiguration(), Environment.sqlClient());
			var cardDbRecords = await(cardsDao.findManyByCondition(
					CARDS.IS_PUBLISHED.eq(true)
							.and(CARDS.IS_ARCHIVED.eq(false))
							.and(CARDS.CREATED_BY.ne(gitUserId))));
			loadFromCardDbRecords(cardDbRecords);
			workingContext = null;
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
		var context = sharedPerVertx
				? ((VertxInternal) Vertx.currentContext().owner()).createEventLoopContext()
				: ContextInternal.current();
		subscriber.channel(SPELLSOURCE_CARDS_CHANGES_CHANNEL_FROM_DDL)
				.handler(payload -> context.runOnContext(v -> {
					try {
						CARD_INVALIDATIONS.increment();
						var decoded = DatabindCodec.mapper().readValue(payload, SpellsourceCardChangesChannelNotification.class);
						if (decoded.id() != null) {
							invalidateCardForGameplayPurposes(decoded.id());
						}
						if (decoded.createdBy() != null) {
							// these are throttled per user
							userInvalidations.onNext(decoded.createdBy());
						}
					} catch (JsonProcessingException e) {
						throw new RuntimeException(e);
					}
				}));


		var debounceInvalidations = Observable.merge(userInvalidations
						.groupBy(s -> s)
						.map(group -> group.debounce(8, TimeUnit.SECONDS)))
				.observeOn(RxHelper.scheduler(context))
				.subscribe(this::invalidateUserGetCardsResponseForUnityCollection);

		context.addCloseHook(completion -> {
			debounceInvalidations.dispose();
			completion.succeed();
		});

		return subscriber.connect();
	}

	private void loadFromCardDbRecords(List<Cards> cardDbRecords) {
		if (cardDbRecords == null || cardDbRecords.isEmpty()) {
			return;
		}
		var cardDescs = cardDbRecords.stream()
				.map(card -> {
					try {
						if (card.getLastModified() != null) {
							lastModifiedByCard.put(card.getId(), card.getLastModified().toString());
						}
						var cardJson = card.getCardScript().put("id", card.getId());
						var cardDesc = cardJson.mapTo(CardDesc.class);
						// todo: should there be a dedicated metadata field for this? it could be mutated by buggy code
						var attributes = cardDesc.getAttributes();
						if (attributes == null) {
							attributes = new AttributeMap();
							cardDesc.setAttributes(attributes);
						}
						attributes.put(Attribute.CREATED_BY_USER_ID, card.getCreatedBy());
						attributes.put(Attribute.PUBLISHED, Boolean.TRUE.equals(card.getIsPublished()));
						attributes.put(Attribute.ARCHIVED, Boolean.TRUE.equals(card.getIsArchived()));
						return cardDesc;
					} catch (Exception e) {
						LOGGER.warn("Skipping malformed custom card {} in SQL: {}", card.getId(), e.getMessage());
						return null;
					}
				})
				.filter(Objects::nonNull)
				.toList();
		if (!cardDescs.isEmpty()) {
			loadCards(cardDescs);
		}
	}

	public void invalidateCardForGameplayPurposes(String cardId) {
		if (cardId == null) {
			return;
		}
		invalidated.add(cardId);
		refreshInvalidCardsFromSql();
	}

	private synchronized void refreshInvalidCardsFromSql() {
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
			var requestedIds = Set.copyOf(toFetch);
			pendingCardRefresh = pendingCardRefresh.recover(t -> Future.succeededFuture())
				.compose(v -> cardsDao.findManyByCondition(CARDS.ID.in(requestedIds)
					.and(CARDS.IS_PUBLISHED.eq(true)).and(CARDS.IS_ARCHIVED.eq(false))))
				.map(cardDbRecords -> {
					lock.writeLock().lock();
					try {
						var withdrawn = new HashSet<>(requestedIds);
						cardDbRecords.forEach(card -> withdrawn.remove(card.getId()));
						var gitCards = getBaseGitCatalogue(gitUserId).getCards();
						for (var id : withdrawn) {
							removeCard(id);
							lastModifiedByCard.remove(id);
							var gitCard = gitCards.get(id);
							if (gitCard != null) {
								loadCards(List.of(gitCard.getDesc()));
							}
						}
						loadFromCardDbRecords(cardDbRecords);
						return (Void) null;
					} finally {
						lock.writeLock().unlock();
					}
				}).onFailure(t -> LOGGER.warn("Failed to refresh invalidated cards from SQL", t));
		}
	}

	private Hiddenswitch.GetCardsResponse createUserCards(String userId) {
		Predicate<Card> cardPredicate = card -> {
			var attributes = card.getDesc().getAttributes();
			return Objects.equals(attributes.get(Attribute.CREATED_BY_USER_ID), userId)
					&& card.hasAttribute(Attribute.PUBLISHED)
					&& !card.hasAttribute(Attribute.ARCHIVED)
					&& (spellsource().isInFormat(card.getDesc().getSet()) || rogue().isInFormat(card.getDesc().getSet()))
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
	
	public DeckFormat rogue() {
		return getFormat("Rogue");
	}

	@NotNull
	private Hiddenswitch.GetCardsResponse createCardsResponse(Predicate<Card> cardPredicate) {
		var ctx = workingContext;
		if (ctx == null) {
			ctx = new GameContext(this, this.spellsource());
			workingContext = ctx;
		}
		final var activeCtx = ctx;
		var cards = getCards().values()
				.stream()
				.filter(cardPredicate)
				.map(card -> ModelConversions.getEntity(activeCtx, card, 0))
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
