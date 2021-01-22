package com.hiddenswitch.framework;

import com.google.common.collect.Iterators;
import com.google.common.collect.ObjectArrays;
import com.google.protobuf.Empty;
import com.google.protobuf.StringValue;
import com.hiddenswitch.framework.impl.ModelConversions;
import com.hiddenswitch.framework.impl.ServerGameContext;
import com.hiddenswitch.framework.impl.WeakVertxMap;
import com.hiddenswitch.framework.rpc.GetCardsRequest;
import com.hiddenswitch.framework.rpc.GetCardsResponse;
import com.hiddenswitch.framework.rpc.VertxUnauthenticatedCardsGrpc;
import com.hiddenswitch.framework.schema.spellsource.tables.daos.*;
import com.hiddenswitch.framework.schema.spellsource.tables.pojos.DeckPlayerAttributeTuples;
import com.hiddenswitch.framework.schema.spellsource.tables.pojos.Decks;
import com.hiddenswitch.framework.schema.spellsource.tables.records.DecksRecord;
import com.hiddenswitch.spellsource.rpc.*;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import io.grpc.BindableService;
import io.grpc.ServerServiceDefinition;
import io.grpc.Status;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.streams.ReadStream;
import io.vertx.core.streams.WriteStream;
import io.vertx.sqlclient.Row;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.desc.CardDesc;
import net.demilich.metastone.game.decks.DeckCreateRequest;
import net.demilich.metastone.game.decks.DeckFormat;
import net.demilich.metastone.game.decks.GameDeck;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.spells.desc.condition.Condition;
import net.demilich.metastone.game.spells.desc.condition.ConditionArg;
import net.openhft.hashing.LongHashFunction;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.redisson.api.RMapCacheAsync;

import java.io.IOException;
import java.util.Comparator;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import static com.hiddenswitch.framework.schema.spellsource.Tables.DECK_SHARES;
import static com.hiddenswitch.framework.schema.spellsource.tables.Cards.CARDS;
import static com.hiddenswitch.framework.schema.spellsource.tables.CardsInDeck.CARDS_IN_DECK;
import static com.hiddenswitch.framework.schema.spellsource.tables.DeckPlayerAttributeTuples.DECK_PLAYER_ATTRIBUTE_TUPLES;
import static com.hiddenswitch.framework.schema.spellsource.tables.Decks.DECKS;
import static io.vertx.ext.sync.Sync.await;
import static io.vertx.ext.sync.Sync.fiber;
import static java.util.stream.Collectors.*;

/**
 * The legacy services for Spellsource, to rapidly transition the game into a new backend.
 */
public class Legacy {
	private static final WeakVertxMap<List<DeckCreateRequest>> PREMADE_DECKS = new WeakVertxMap<>(Legacy::getPremadeDecksPrivate);
	private static final WeakVertxMap<RMapCacheAsync<String, DecksGetResponse>> DECKS_CACHE = new WeakVertxMap<>(Legacy::decksCacheConstructor);
	private static final Promise<GetCardsResponse> GET_CARDS_RESPONSE_PROMISE = Promise.promise();
	private static final AtomicBoolean CARDS_BUILT = new AtomicBoolean();

	public static Future<BindableService> unauthenticatedCards() {
		return Future.succeededFuture(new VertxUnauthenticatedCardsGrpc.UnauthenticatedCardsVertxImplBase() {
			@Override
			public Future<GetCardsResponse> getCards(GetCardsRequest request) {
				// retrieve the hashcode from the migration schema for the cards
				var configuration = Environment.jooqAkaDaoConfiguration();
				var delegate = Environment.sqlPoolAkaDaoDelegate();

				try {
					if (CARDS_BUILT.compareAndSet(false, true)) {
						var workingContext = new GameContext();
						(new CardsDao(configuration, delegate))
								.findAll()
								.map(cards -> cards.stream().map(card -> card.getCardScript().mapTo(CardDesc.class))
										.filter(cd -> DeckFormat.spellsource().isInFormat(cd.getSet())
												&& cd.getType() != com.hiddenswitch.spellsource.client.models.CardType.GROUP)
										.map(card -> {
											var entity = ModelConversions.getEntity(workingContext, card.create(), 0);
											if (card.getArt() != null) {
												var value = Environment.toProto(card.getArt(), Art.class);
												entity.setArt(value);
											}
											if (card.getTooltips() != null) {
												for (var t : card.getTooltips()) {
													entity.addTooltips(Environment.toProto(t, Tooltip.class));
												}
											}
											return entity;
										})
										.map(card -> CardRecord.newBuilder().setEntity(card))
										.collect(toList()))
								.map(cards -> {
									var builder = GetCardsResponse.Content.newBuilder();

									for (var i = 0; i < cards.size(); i++) {
										cards.get(i).getEntityBuilder()
												.setId(i);
										builder.addCards(cards.get(i).build());
									}

									var built = builder.build();
									var checksum = LongHashFunction.xx().hashBytes(built.toByteArray());

									var response = GetCardsResponse.newBuilder()
											.setVersion(Long.toString(checksum));

									response.setContent(built);

									return response.build();
								})
								.onComplete(GET_CARDS_RESPONSE_PROMISE);
					}

				} catch (Throwable t) {
					return Future.failedFuture(t);
				}

				return GET_CARDS_RESPONSE_PROMISE.future()
						.compose(res -> {
							if (Objects.equals(request.getIfNoneMatch(), res.getVersion())) {
								return Future.succeededFuture(GetCardsResponse.newBuilder().setCachedOk(true).build());
							}

							return Future.succeededFuture(res);
						})
						.recover(Environment.onGrpcFailure());
			}
		});
	}

	public static Future<ServerServiceDefinition> services() {
		return Future.succeededFuture(new VertxHiddenSwitchSpellsourceAPIServiceGrpc.HiddenSwitchSpellsourceAPIServiceVertxImplBase() {

			@Override
			public void subscribeGame(ReadStream<ClientToServerMessage> request, WriteStream<ServerToClientMessage> response) {
				ServerGameContext.subscribeGame(request, response);
			}

			@Override
			public Future<Empty> decksDelete(DecksDeleteRequest request) {
				var userId = Accounts.userId();
				var deckId = request.getDeckId();
				// first, try to trash the share if it exists
				// otherwise, the if it's a premade deck and the trash record does not exist, insert a share record that is trashed
				// otherwise, if we own the deck, trash the deck.
				var queryExecutor = Environment.queryExecutor();
				return queryExecutor.execute(dsl -> dsl.update(DECK_SHARES)
						.set(DECK_SHARES.TRASHED_BY_RECIPIENT, true)
						.where(DECK_SHARES.DECK_ID.eq(deckId), DECK_SHARES.SHARE_RECIPIENT_ID.eq(userId)))
						.compose(updated -> {
							if (updated != 0) {
								return Future.succeededFuture();
							}

							return queryExecutor
									.findOneRow(dsl -> dsl.select(DECKS.IS_PREMADE, DECKS.CREATED_BY).from(DECKS).where(DECKS.ID.eq(deckId)))
									.compose(row -> {
										if (row == null) {
											return Future.failedFuture("deck not found");
										}

										var isPremade = row.getBoolean(DECKS.IS_PREMADE.getName());
										var isOwner = Objects.equals(row.getString(DECKS.CREATED_BY.getName()), userId);
										if (isPremade) {
											// insert a stop sharing premade deck row
											return queryExecutor.execute(dsl -> dsl.insertInto(DECK_SHARES)
													.set(DECK_SHARES.newRecord()
															.setDeckId(deckId)
															.setShareRecipientId(userId)
															.setTrashedByRecipient(true)));
										} else if (isOwner) {
											return queryExecutor.execute(dsl -> dsl.update(DECKS).set(DECKS.TRASHED, true).where(DECKS.ID.eq(deckId), canEditDeckSql(userId)));
										} else {
											return Future.failedFuture("deck not owned by user or shared with user");
										}
									});
						}).compose(v -> {
							// invalidate cache
							var cache = DECKS_CACHE.get();
							return Future.fromCompletionStage(cache.fastRemoveAsync(deckId), Vertx.currentContext());
						})
						.map(Empty.getDefaultInstance())
						.recover(Environment.onGrpcFailure());
			}

			@Override
			public Future<DecksGetResponse> decksGet(DecksGetRequest request) {
				var deckId = request.getDeckId();
				var userId = Accounts.userId();

				return getDeck(deckId, userId).recover(Environment.onGrpcFailure());
			}

			@Override
			public Future<DecksGetAllResponse> decksGetAll(Empty request) {
				var userId = Accounts.userId();
				return getAllDecks(userId).recover(Environment.onGrpcFailure());
			}

			@Override
			public Future<DecksPutResponse> decksPut(DecksPutRequest request) {
				var userId = Accounts.userId();

				DeckCreateRequest createRequest;
				if (!request.getDeckList().isEmpty()) {
					createRequest = DeckCreateRequest.fromDeckList(request.getDeckList());
				} else {
					createRequest = new DeckCreateRequest()
							.withName(request.getName())
							.withFormat(request.getFormat())
							.withCardIds(request.getCardIdsList())
							.withHeroClass(request.getHeroClass());
				}

				return createDeck(userId, createRequest).recover(Environment.onGrpcFailure());
			}

			@Override
			public Future<DecksGetResponse> decksUpdate(DecksUpdateRequest request) {
				var configuration = Environment.jooqAkaDaoConfiguration();
				var delegate = Environment.sqlPoolAkaDaoDelegate();
				var decks = new DecksDao(configuration, delegate);
				var deckId = request.getDeckId();
				var userId = Accounts.userId();
				var updateCommand = request.getUpdateCommand();
				var queryExecutor = Environment.queryExecutor();

				if (deckId.isEmpty()) {
					return Future.failedFuture(Status.INVALID_ARGUMENT
							.withCause(new NullPointerException("deckId"))
							.augmentDescription("You must specify a deckId")
							.asRuntimeException());
				}

				if (!request.hasUpdateCommand()) {
					return getDeck(deckId, userId);
				}

				var futs = new ArrayList<Future>();

				// Assert that we have permissions to edit this deck
				return decks.queryExecutor().execute(dsl -> dsl
						.select(DECKS.ID)
						.from(DECKS)
						.where(DECKS.ID.eq(deckId).and(canEditDeckSql(userId)))
						.limit(1)
				).compose(authedCount -> {
					if (authedCount < 1) {
						return Future.failedFuture(Status.PERMISSION_DENIED
								.augmentDescription("You are not authorized to edit this deck because you are not its owner")
								.asRuntimeException());
					}

					// Player entity attribute update
					if (updateCommand.hasSetPlayerEntityAttribute()) {
						var setAttributeCommand = updateCommand.getSetPlayerEntityAttribute();
						switch (setAttributeCommand.getAttribute()) {
							case SIGNATURE:
								futs.add(queryExecutor.execute(dsl -> dsl.insertInto(DECK_PLAYER_ATTRIBUTE_TUPLES,
										DECK_PLAYER_ATTRIBUTE_TUPLES.ATTRIBUTE,
										DECK_PLAYER_ATTRIBUTE_TUPLES.DECK_ID,
										DECK_PLAYER_ATTRIBUTE_TUPLES.STRING_VALUE).values(
										PlayerEntityAttributes.SIGNATURE_VALUE,
										deckId,
										setAttributeCommand.getStringValue())));
								break;
							default:
								break;
						}
					}

					var cardsInDeckDao = new CardsInDeckDao(configuration, delegate);
					// Card records update
					if (updateCommand.hasPushCardIds()) {
						futs.add(queryExecutor.execute(dsl -> {
							var insert = dsl.insertInto(CARDS_IN_DECK,
									CARDS_IN_DECK.DECK_ID,
									CARDS_IN_DECK.CARD_ID
							);
							for (var cardId : updateCommand.getPushCardIds().getEachList()) {
								insert.values(deckId, cardId);
							}
							return insert;
						}));
					}

					if (updateCommand.hasPushInventoryIds()) {
						futs.add(Future.failedFuture(Status.UNIMPLEMENTED
								.augmentDescription("Inventory IDs are now unique to each deck and cannot be shared among decks.")
								.asRuntimeException()));
					}

					if (updateCommand.getSetInventoryIdsCount() > 0) {
						futs.add(Future.failedFuture(Status.UNIMPLEMENTED
								.augmentDescription("Inventory IDs are now unique to each deck and cannot be shared among decks.")
								.asRuntimeException()));
					}

					if (updateCommand.getPullAllCardIdsCount() > 0) {
						futs.addAll(updateCommand.getPullAllCardIdsList().stream()
								.collect(groupingBy(Function.identity(), counting()))
								.entrySet()
								.stream()
								.map(entry ->
										queryExecutor.execute(dsl ->
												dsl.deleteFrom(CARDS_IN_DECK)
														.where(CARDS_IN_DECK.DECK_ID.eq(deckId).and(CARDS_IN_DECK.CARD_ID.eq(entry.getKey())))
														.limit(entry.getValue().intValue())))
								.collect(toList()));
					}

					if (updateCommand.getPullAllInventoryIdsCount() > 0) {
						futs.add(cardsInDeckDao.deleteByIds(new ArrayList<>(updateCommand.getPullAllInventoryIdsList())));
					}

					var setsHeroClass = !updateCommand.getSetHeroClass().isEmpty();
					var setsName = !updateCommand.getSetName().isEmpty();

					if (setsHeroClass || setsName) {
						// Deck record update
						futs.add(decks.queryExecutor()
								.execute(dsl -> {
									var update = (UpdateSetStep<DecksRecord>) dsl.update(DECKS);
									if (setsHeroClass) {
										update = update.set(DECKS.HERO_CLASS, updateCommand.getSetHeroClass());
									}

									if (setsName) {
										update = update.set(DECKS.NAME, updateCommand.getSetName());
									}

									update = update.set(DECKS.LAST_EDITED_BY, userId);

									return ((UpdateSetMoreStep<DecksRecord>) update)
											.where(DECKS.ID.eq(deckId));
								}));
					}

					if (futs.isEmpty()) {
						return Future.succeededFuture();
					}

					return CompositeFuture.all(futs);
				})
						.compose(v -> {
							// invalidate cache
							var cache = DECKS_CACHE.get();
							return Future.fromCompletionStage(cache.fastRemoveAsync(deckId), Vertx.currentContext());
						})
						.compose(v -> getDeck(deckId, userId))
						.recover(Environment.onGrpcFailure());
			}

			@Override
			public Future<DecksGetResponse> duplicateDeck(StringValue request) {
				var userId = Accounts.userId();
				var deckId = request.getValue();
				var newDeckId = UUID.randomUUID().toString();

				var queryExecutor = Environment.queryExecutor();

				return fiber(() -> {
					var decksInserted = await(queryExecutor
							.execute(dsl -> {
								// new deckId, deck fields...
								var newDeckIdAndOtherFields = ObjectArrays.concat(DSL.val(newDeckId), withoutFields(DECKS.fields(), DECKS.ID));
								// replace owner ID
								var ownerIdPos = Iterators.indexOf(Iterators.forArray(newDeckIdAndOtherFields), t -> t != null && Objects.equals(t.getName(), DECKS.CREATED_BY.getName()));
								newDeckIdAndOtherFields[ownerIdPos] = DSL.val(userId);
								// replace premade
								var premadePos = Iterators.indexOf(Iterators.forArray(newDeckIdAndOtherFields), f -> Objects.equals(f, DECKS.IS_PREMADE));
								newDeckIdAndOtherFields[premadePos] = DSL.val(false);
								return dsl.insertInto(DECKS, DECKS.fields())
										// retrieve fields from existing deck EXCEPT the ID, which we replaced
										.select(DSL.using(SQLDialect.POSTGRES).select(newDeckIdAndOtherFields)
												.from(DECKS)
												// We can duplicate our own decks, decks that are permitted to duplicate by anyone or premade decks
												.where(DECKS.ID.eq(deckId).and(canEditDeckSql(userId)
														.or(DECKS.PERMITTED_TO_DUPLICATE.eq(true))
														.or(DECKS.IS_PREMADE.eq(true)))));
							}));

					if (decksInserted != 1) {
						throw Status.NOT_FOUND.asRuntimeException();
					}

					await(queryExecutor.execute(dsl -> duplicateAllForeign(deckId, newDeckId, CARDS_IN_DECK.ID, CARDS_IN_DECK.DECK_ID)));
					await(queryExecutor.execute(dsl -> duplicateAllForeign(deckId, newDeckId, DECK_PLAYER_ATTRIBUTE_TUPLES.ID, DECK_PLAYER_ATTRIBUTE_TUPLES.DECK_ID)));
					return await(getDeck(newDeckId, userId));
				})
						.recover(Environment.onGrpcFailure());
			}
		})
				.compose(Accounts::requiresAuthorization);
	}

	public static Future<DecksGetAllResponse> getAllDecks(String userId) {
		return Environment.queryExecutor()
				.findManyRow(dsl ->
						// retrieves all decks that are premade and which the user has not explicitly deleted (the share of)
						// and all the user's decks they've created and not deleted
						// and all other decks shared with the user that they have not deleted (the share of)
						dsl.select(DECKS.ID)
								.from(DECKS)
								// check the share settings on the decks
								.join(DECK_SHARES, JoinType.LEFT_OUTER_JOIN)
								// if it has a deck share record...
								.on(DECKS.ID.eq(DECK_SHARES.DECK_ID)
										// the record must match this user
										.and(DECK_SHARES.SHARE_RECIPIENT_ID.eq(userId)))
								.where(
										// the deck is not trashed and, if there is a deck share record, the deck share's trashed
										// value is null or false
										DECKS.TRASHED.eq(false),
										// the user can edit the deck
										canEditDeckSql(userId)
												.or(
														// this is a premade deck - there's no share record or if there was a share record, it was trashed by the recipient
														DECKS.IS_PREMADE.eq(true).and(DECK_SHARES.SHARE_RECIPIENT_ID.isNull().or(DECK_SHARES.TRASHED_BY_RECIPIENT.eq(false))))
												.or(
														// this deck is shared with the user - it is not a premade deck, there is a recipient id, and it has not been trashed by the recipient
														DECKS.IS_PREMADE.eq(false).and(DECK_SHARES.SHARE_RECIPIENT_ID.isNotNull().and(DECK_SHARES.TRASHED_BY_RECIPIENT.eq(false)))
												)))
				.compose(rows -> CompositeFuture.all(rows.stream()
						.map(row -> {
							var deckId = row.getString(0);
							return getDeck(deckId, userId);
						}).collect(toList())))
				.compose(getDeckResponses -> {
					var reply = DecksGetAllResponse.newBuilder();
					for (var i = 0; i < getDeckResponses.size(); i++) {
						var getDeckResponse = getDeckResponses.<DecksGetResponse>resultAt(i);
						reply.addDecks(getDeckResponse);
					}
					return Future.succeededFuture(reply.build());
				});
	}

	private static boolean canEditDeck(DecksGetResponse deck, String userId) {
		return Objects.equals(deck.getCollection().getUserId(), userId);
	}

	private static org.jooq.Condition canEditDeckSql(String userId) {
		return DECKS.CREATED_BY.eq(userId);
	}

	private static <R extends Record, TForeignIdField> Insert<R>
	duplicateAllForeign(TForeignIdField oldForeignId,
	                    TForeignIdField newForeignId,
	                    TableField<R, ?> generatedAlwaysIdField,
	                    TableField<R, TForeignIdField> foreignReferenceField) {
		// column_a, ... , id generated always, column_b, column_c, ... , foreign_id_column, column_d, column_e ...
		var oneToManyTable = generatedAlwaysIdField.getTable();
		// remove the always generated id field in this one-to-many table
		// column_a, ... , column_b, column_c, ... , foreign_id_column, column_d, column_e ...
		var fieldsWithoutId = withoutFields(oneToManyTable.fields(), generatedAlwaysIdField);
		// find the position of the foreign id column in order to replace the foreign id column name with the literal value
		// of the new foreign ID
		var foreignIdPos = Iterators.indexOf(Iterators.forArray(fieldsWithoutId), t -> Objects.equals(t.getName(), foreignReferenceField.getName()));
		var newForeignIdAndFieldsWithoutAutogeneratedId = Arrays.copyOf(fieldsWithoutId, fieldsWithoutId.length);
		// replace with the new foreign ID
		// column_a, ... , column_b, column_c, ... , "new foreign ID", column_d, column_e ...
		newForeignIdAndFieldsWithoutAutogeneratedId[foreignIdPos] = DSL.val(newForeignId);
		// insert column_a, ... , column_b, column_c, ... , foreign_id_column, column_d, column_e ... into one_to_many_table
		//   select (column_a, ... , column_b, column_c, ... , "new foreign ID", column_d, column_e ...) from one_to_many_table
		//   where foreign_id_column = oldForeignId
		return DSL.using(SQLDialect.POSTGRES).insertInto(oneToManyTable, fieldsWithoutId)
				.select(DSL.using(SQLDialect.POSTGRES).select(newForeignIdAndFieldsWithoutAutogeneratedId)
						.from(oneToManyTable)
						.where(foreignReferenceField.eq(oldForeignId)));
	}

	@SafeVarargs
	public static Field[] withoutFields(Field[] array, Field... withoutElements) {
		return without(Comparator.comparing(Field::getName), array, withoutElements);
	}

	@SafeVarargs
	public static <T extends Comparable<T>> T[] without(T[] array, T... withoutElements) {
		return without(Comparator.naturalOrder(), array, withoutElements);
	}

	@SafeVarargs
	public static <T> T[] without(Comparator<T> comparator, T[] array, T... withoutElements) {
		var returnArray = Arrays.copyOf(array, array.length);
		var k = 0;
		for (var i = 0; i < array.length; i++) {
			var skip = false;
			for (var j = 0; j < withoutElements.length; j++) {
				if (comparator.compare(array[i], withoutElements[j]) == 0) {
					skip = true;
					break;
				}
			}

			if (skip) {
				continue;
			}

			returnArray[k] = array[i];
			k++;
		}

		if (k != returnArray.length) {
			return Arrays.copyOf(returnArray, k);
		}

		return returnArray;
	}

	private static RMapCacheAsync<String, DecksGetResponse> decksCacheConstructor(Vertx vertx) {
		return Environment.cache("Spellsource:decks", DecksGetResponse.parser());
	}

	public static Future<DecksGetResponse> getDeck(String deckId, String userId) {
		var serverConfiguration = Environment.cachedConfigurationOrGet();
		var cache = DECKS_CACHE.get();
		var configuration = Environment.jooqAkaDaoConfiguration();
		var delegate = Environment.sqlPoolAkaDaoDelegate();
		return Future.fromCompletionStage(cache.getAsync(deckId), Vertx.currentContext())
				.compose(existing -> {
					if (existing != null) {
						return Future.succeededFuture(existing);
					}

					return getDeck(deckId)
							.compose(deck -> Future.fromCompletionStage(cache.fastPutAsync(deckId, deck, serverConfiguration.getDecks().getCachedDeckTimeToLiveMinutes(), TimeUnit.MINUTES), Vertx.currentContext()).map(deck));
				}).compose(deck -> {
					// we must always look up the shares record, though this should be fast
					var deckSharesDao = new DeckSharesDao(configuration, delegate);
					var sharesFut = deckSharesDao.queryExecutor()
							.findOne(dsl -> dsl.selectFrom(DECK_SHARES)
									.where(DECK_SHARES.SHARE_RECIPIENT_ID.eq(userId).and(DECK_SHARES.DECK_ID.eq(deckId))));

					return sharesFut.compose(shares -> {
						// Check the deck is premade, owned by the current user, or shared with the user
						if (!(deck.getCollection().getIsStandardDeck()
								|| Objects.equals(deck.getCollection().getUserId(), userId)
								|| shares != null)) {
							return Future.failedFuture("unauthorized");
						}
						return Future.succeededFuture(deck);
					});
				}).map(deck -> {
					// transmit the right editability
					var editableDeck = DecksGetResponse.newBuilder(deck);
					var collection = InventoryCollection.newBuilder(editableDeck.getCollection());
					collection.setCanEdit(canEditDeck(deck, userId));
					editableDeck.setCollection(collection);
					return editableDeck.build();
				});
	}

	private static Future<DecksGetResponse> getDeck(String deckId) {
		var configuration = Environment.jooqAkaDaoConfiguration();
		var delegate = Environment.sqlPoolAkaDaoDelegate();
		var decks = new DecksDao(configuration, delegate);
		var playerEntityAttributesDao = new DeckPlayerAttributeTuplesDao(configuration, delegate);
		var queryExecutor = Environment.queryExecutor();
		var cardsFut = queryExecutor.findManyRow(dsl -> dsl.select(
				CARDS_IN_DECK.ID,
				CARDS_IN_DECK.DECK_ID,
				CARDS_IN_DECK.CARD_ID,
				CARDS.CARD_SCRIPT)
				.from(CARDS_IN_DECK)
				.join(CARDS, JoinType.JOIN)
				.on(CARDS_IN_DECK.CARD_ID.eq(CARDS.ID))
				.where(CARDS_IN_DECK.DECK_ID.eq(deckId)))
				.onFailure(Environment.onFailure());
		var decksFut = decks.findOneById(deckId);
		var playerEntityAttributesFut = playerEntityAttributesDao.findManyByDeckId(Collections.singletonList(deckId));

		return CompositeFuture.join(cardsFut, decksFut, playerEntityAttributesFut)
				.compose(res -> {
					var cards = res.<List<Row>>resultAt(0);
					var deck = res.<Decks>resultAt(1);
					var playerEntityAttributes = res.<List<DeckPlayerAttributeTuples>>resultAt(2);
					var reply = DecksGetResponse.newBuilder();
					var inventoryCollection = InventoryCollection.newBuilder();

					inventoryCollection.setId(deck.getId())
							.setDeckType(InventoryCollection.InventoryCollectionDeckType.forNumber(deck.getDeckType()))
							.setFormat(deck.getFormat())
							.setHeroClass(deck.getHeroClass())
							.setName(deck.getName())
							.setType(InventoryCollection.InventoryCollectionType.INVENTORY_COLLECTION_TYPE_DECK)
							.setIsStandardDeck(deck.getIsPremade())
							.setUserId(deck.getCreatedBy())
							.setValidationReport((ValidationReport.Builder) validateDeck(cards.stream().map(row -> row.getString(CARDS_IN_DECK.CARD_ID.getName())).collect(toList()), deck.getHeroClass(), deck.getFormat()));
					var i = 0;
					for (var cardRecordRow : cards) {
						inventoryCollection.addInventory(CardRecord
								.newBuilder()
								.setId(cardRecordRow.getLong(CARDS_IN_DECK.ID.getName()))
								// TODO: Do we really need to transmit the complete entity here?
								.setEntity(Entity.newBuilder()
										.setId(i)
										.setName(cardRecordRow.getJsonObject(CARDS.CARD_SCRIPT.getName()).getString("name"))
										.setCardId(cardRecordRow.getString(CARDS_IN_DECK.CARD_ID.getName())))
								.addCollectionIds(deck.getId())
								.build());
						i++;
					}

					for (var playerEntityAttribute : playerEntityAttributes) {
						inventoryCollection.addPlayerEntityAttributes(AttributeValueTuple.newBuilder()
								.setAttribute(PlayerEntityAttributes.forNumber(playerEntityAttribute.getAttribute()))
								.setStringValue(playerEntityAttribute.getStringValue())
								.build());
					}

					reply.setCollection(inventoryCollection);
					reply.setInventoryIdsSize(inventoryCollection.getInventoryCount());
					return Future.succeededFuture(reply.build());
				});
	}

	private static ValidationReportOrBuilder validateDeck(List<String> cardIds, String heroClass, String deckFormat) {
		var deck = new GameDeck(heroClass, cardIds);
		var validationReport = ValidationReport.newBuilder();
		validationReport.setValid(true);

		var context = new GameContext();
		var player1 = new Player(deck);
		context.setPlayer1(player1);
		context.setPlayer2(new Player(HeroClass.TEST));
		context.setDeckFormat(DeckFormat.getFormat(deckFormat));
		if (context.getDeckFormat() == null) {
			validationReport.setValid(false);
			validationReport.addErrors("Invalid Format");
			return validationReport;
		}
		var formatCard = CardCatalogue.getFormatCard(context.getDeckFormat().getName());
		if (formatCard == null) {
			validationReport.setValid(true);
			return validationReport;
		}

		var conditions = (Condition[]) formatCard.getCondition().get(ConditionArg.CONDITIONS);
		for (var condition : conditions) {
			var pass = condition.isFulfilled(context, player1, player1, player1);
			if (!pass) {
				validationReport.setValid(false);
				var error = condition.getDesc().getString(ConditionArg.DESCRIPTION);
				validationReport.addErrors(error);
			}
		}

		return validationReport;
	}

	public static Future<DecksPutResponse> createDeck(String userId, DeckCreateRequest request) {
		if (request.getInventoryIds() != null && !request.getInventoryIds().isEmpty()) {
			return Future.failedFuture("cannot specify inventory ids");
		}

		var configuration = Environment.jooqAkaDaoConfiguration();
		var delegate = Environment.sqlPoolAkaDaoDelegate();
		var decksDao = new DecksDao(configuration, delegate);
		var deckId = UUID.randomUUID().toString();
		return decksDao.insert(new Decks()
				.setId(deckId)
				.setCreatedBy(userId)
				.setName(request.getName())
				.setLastEditedBy(userId)
				.setFormat(request.getFormat())
				.setIsPremade(request.isStandardDeck())
				.setHeroClass(request.getHeroClass())
				.setDeckType(InventoryCollection.InventoryCollectionDeckType.INVENTORY_COLLECTION_DECK_TYPE_CONSTRUCTED_VALUE))
				.compose(rowsInserted -> {
					if (rowsInserted < 1) {
						return Future.failedFuture("insert failed");
					}

					if (request.getCardIds().isEmpty()) {
						return Future.succeededFuture();
					}

					return Environment.queryExecutor().execute(dsl -> {
						var insert = dsl.insertInto(CARDS_IN_DECK, CARDS_IN_DECK.CARD_ID, CARDS_IN_DECK.DECK_ID);
						request.getCardIds().forEach(cardId -> insert.values(cardId, deckId));
						return insert;
					});
				})
				.compose(ignored -> getDeck(deckId, userId))
				.compose(decksGetResponse -> Future.succeededFuture(DecksPutResponse.newBuilder()
						.setCollection(decksGetResponse.getCollection())
						.setDeckId(deckId)
						.build()));
	}

	public static List<DeckCreateRequest> getPremadeDecks() {
		return PREMADE_DECKS.get();
	}

	private static List<DeckCreateRequest> getPremadeDecksPrivate(Vertx ignored) {
		CardCatalogue.loadCardsFromPackage();
		List<String> deckLists;
		try (ScanResult scanResult = new ClassGraph()
				.acceptPaths("spellsource/decklists/current").scan()) {
			deckLists = scanResult
					.getResourcesWithExtension(".txt")
					.stream()
					.map(resource -> {
						try {
							return resource.getContentAsString();
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
					})
					.collect(toList());
			return deckLists.stream()
					.map((deckList) -> DeckCreateRequest.fromDeckList(deckList).setStandardDeck(true))
					.filter(Objects::nonNull)
					.collect(toList());
		} catch (Throwable t) {
			return Collections.emptyList();
		}
	}
}
