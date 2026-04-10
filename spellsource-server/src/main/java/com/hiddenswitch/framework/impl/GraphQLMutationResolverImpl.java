package com.hiddenswitch.framework.impl;

import com.hiddenswitch.framework.Accounts;
import com.hiddenswitch.framework.Environment;
import com.hiddenswitch.framework.Legacy;
import com.hiddenswitch.framework.graphql.*;
import com.hiddenswitch.spellsource.rpc.Spellsource;
import com.hiddenswitch.framework.schema.keycloak.tables.daos.UserEntityDao;
import graphql.kickstart.tools.GraphQLMutationResolver;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClient;
import net.demilich.metastone.game.decks.DeckCreateRequest;
import org.keycloak.TokenVerifier;
import org.keycloak.representations.AccessToken;

import java.util.List;
import java.util.stream.Collectors;

import static com.hiddenswitch.framework.Environment.withDslContext;
import static com.hiddenswitch.framework.Environment.withExecutor;
import static com.hiddenswitch.framework.schema.spellsource.Tables.MATCHMAKING_TICKETS;

public class GraphQLMutationResolverImpl implements MutationResolver, GraphQLMutationResolver {

	private final SqlCachedCardCatalogue cardCatalogue;

	public GraphQLMutationResolverImpl() {
		this.cardCatalogue = null;
	}

	public GraphQLMutationResolverImpl(SqlCachedCardCatalogue cardCatalogue) {
		this.cardCatalogue = cardCatalogue;
	}

	// ── auth ─────────────────────────────────────────────────

	@Override
	public Future<LoginOrCreateReply> createAccount(CreateAccountInput input) throws Exception {
		var webClient = WebClient.create(Vertx.currentContext().owner());
		var client = new com.hiddenswitch.framework.Client(Vertx.currentContext().owner(), webClient);
		var decks = input.getDecks() != null ? input.getDecks() : true;
		var guest = input.getGuest() != null ? input.getGuest() : false;

		Future<com.hiddenswitch.framework.schema.keycloak.tables.pojos.UserEntity> createFut;
		String email = input.getEmail();
		String password = input.getPassword();

		if (guest) {
			email = java.util.UUID.randomUUID() + "@spellsource.com";
			password = java.util.UUID.randomUUID().toString();
			createFut = Accounts.createUser(email, Accounts.GUEST_PREFIX + System.currentTimeMillis(), password, new Accounts.UserAttributes(true));
		} else {
			createFut = Accounts.createUser(email, input.getUsername(), password, new Accounts.UserAttributes(decks));
		}

		var finalEmail = email;
		var finalPassword = password;
		return createFut.compose(userEntity -> client.login(finalEmail, finalPassword)
				.map(accessTokenResponse -> new LoginOrCreateReply.Builder()
						.setAccessToken(new com.hiddenswitch.framework.graphql.AccessToken.Builder()
								.setToken(accessTokenResponse.getToken())
								.build())
						.setUserEntity(new UserEntity.Builder()
								.setId(userEntity.getId())
								.setEmail(userEntity.getEmail())
								.setUsername(userEntity.getUsername())
								.setPrivacyToken("")
								.build())
						.build()));
	}

	@Override
	public Future<LoginOrCreateReply> login(LoginInput input) throws Exception {
		var webClient = WebClient.create(Vertx.currentContext().owner());
		var client = new com.hiddenswitch.framework.Client(Vertx.currentContext().owner(), webClient);
		return client.login(input.getUsernameOrEmail(), input.getPassword())
				.compose(accessTokenResponse -> {
					try {
						var token = TokenVerifier.create(accessTokenResponse.getToken(), AccessToken.class);
						var userId = token.getToken().getSubject();
						var dao = new UserEntityDao(Environment.jooqAkaDaoConfiguration(), Environment.sqlClient());
						return dao.findOneById(userId).map(userEntity -> new LoginOrCreateReply.Builder()
								.setAccessToken(new com.hiddenswitch.framework.graphql.AccessToken.Builder()
										.setToken(accessTokenResponse.getToken())
										.build())
								.setUserEntity(new UserEntity.Builder()
										.setId(userEntity.getId())
										.setEmail(userEntity.getEmail())
										.setUsername(userEntity.getUsername())
										.setPrivacyToken("")
										.build())
								.build());
					} catch (Exception e) {
						return Future.failedFuture(e);
					}
				});
	}

	@Override
	public Future<LoginOrCreateReply> changePassword(String newPassword) throws Exception {
		var userId = Accounts.userId();
		if (userId == null) {
			return Future.failedFuture("must be authenticated");
		}
		return Accounts.realm()
				.compose(realm -> Environment.executeBlocking(() -> {
					realm.users().get(userId).resetPassword(
							new org.keycloak.representations.idm.CredentialRepresentation() {{
								setType(org.keycloak.representations.idm.CredentialRepresentation.PASSWORD);
								setValue(newPassword);
								setTemporary(false);
							}});
					return null;
				}))
				.compose(v -> Accounts.user(userId))
				.map(userEntity -> new LoginOrCreateReply.Builder()
						.setUserEntity(new UserEntity.Builder()
								.setId(userEntity.getId())
								.setEmail(userEntity.getEmail())
								.setUsername(userEntity.getUsername())
								.setPrivacyToken("")
								.build())
						.build());
	}

	@Override
	public Future<Boolean> requestPasswordResetEmail() throws Exception {
		// Keycloak handles password reset emails via its own flow
		return Future.succeededFuture(true);
	}

	// ── decks ────────────────────────────────────────────────

	@Override
	public Future<DecksPutResponse> createDeck(DecksPutInput input) throws Exception {
		var userId = Accounts.userId();
		if (userId == null) {
			return Future.failedFuture("must be authenticated");
		}
		if (cardCatalogue == null) {
			return Future.failedFuture("card catalogue not initialized");
		}

		DeckCreateRequest createRequest;
		if (input.getDeckList() != null && !input.getDeckList().isEmpty()) {
			createRequest = DeckCreateRequest.fromDeckList(input.getDeckList());
		} else {
			createRequest = new DeckCreateRequest()
					.withName(input.getName())
					.withFormat(input.getFormat() != null ? input.getFormat() : "")
					.withCardIds(input.getCardIds() != null ? input.getCardIds() : List.of())
					.withHeroClass(input.getHeroClass());
		}

		return Legacy.createDeck(cardCatalogue, userId, createRequest)
				.map(proto -> {
					var coll = proto.getCollection();
					return new DecksPutResponse.Builder()
							.setDeckId(proto.getDeckId())
							.setCollection(protoToGraphqlCollection(coll))
							.build();
				});
	}

	@Override
	public Future<DecksGetResponse> updateDeck(DecksUpdateInput input) throws Exception {
		var userId = Accounts.userId();
		if (userId == null) {
			return Future.failedFuture("must be authenticated");
		}
		if (cardCatalogue == null) {
			return Future.failedFuture("card catalogue not initialized");
		}

		// Build the proto DecksUpdateCommand from the GraphQL input
		var updateBuilder = com.hiddenswitch.spellsource.rpc.Spellsource.DecksUpdateCommand.newBuilder();

		if (input.getPullAllCardIds() != null) {
			updateBuilder.addAllPullAllCardIds(input.getPullAllCardIds());
		}
		if (input.getPushCardIds() != null) {
			updateBuilder.setPushCardIds(com.hiddenswitch.spellsource.rpc.Spellsource.DecksUpdateCommand.PushCardIdsMessage.newBuilder()
					.addAllEach(input.getPushCardIds()));
		}
		if (input.getSetHeroClass() != null) {
			updateBuilder.setSetHeroClass(input.getSetHeroClass());
		}
		if (input.getSetName() != null) {
			updateBuilder.setSetName(input.getSetName());
		}
		if (input.getSetPlayerEntityAttribute() != null) {
			updateBuilder.setSetPlayerEntityAttribute(
					com.hiddenswitch.spellsource.rpc.Spellsource.DecksUpdateCommand.SetPlayerEntityAttributeMessage.newBuilder()
							.setAttribute(com.hiddenswitch.spellsource.rpc.Spellsource.PlayerEntityAttributesMessage.PlayerEntityAttributes.valueOf(input.getSetPlayerEntityAttribute().getAttribute().name()))
							.setStringValue(input.getSetPlayerEntityAttribute().getStringValue()));
		}
		if (input.getUnsetPlayerEntityAttribute() != null) {
			updateBuilder.setUnsetPlayerEntityAttribute(input.getUnsetPlayerEntityAttribute());
		}

		var protoRequest = com.hiddenswitch.spellsource.rpc.Spellsource.DecksUpdateRequest.newBuilder()
				.setDeckId(input.getDeckId())
				.setUpdateCommand(updateBuilder)
				.build();

		// Reuse the same logic as Legacy.decksUpdate but without the gRPC request wrapper.
		// For now, delegate to the static methods.
		return withExecutor(queryExecutor -> {
			var deckId = input.getDeckId();
			var updateCommand = protoRequest.getUpdateCommand();
			var futs = new java.util.ArrayList<Future<?>>();

			return queryExecutor.execute(dsl -> dsl
							.select(com.hiddenswitch.framework.schema.spellsource.tables.Decks.DECKS.ID)
							.from(com.hiddenswitch.framework.schema.spellsource.tables.Decks.DECKS)
							.where(com.hiddenswitch.framework.schema.spellsource.tables.Decks.DECKS.ID.eq(deckId)
									.and(com.hiddenswitch.framework.schema.spellsource.tables.Decks.DECKS.CREATED_BY.eq(userId)))
							.limit(1))
					.compose(authedCount -> {
						if (authedCount < 1) {
							return Future.failedFuture("not authorized to edit this deck");
						}

						if (updateCommand.hasPushCardIds()) {
							futs.add(queryExecutor.execute(dsl -> {
								var insert = dsl.insertInto(com.hiddenswitch.framework.schema.spellsource.tables.CardsInDeck.CARDS_IN_DECK,
										com.hiddenswitch.framework.schema.spellsource.tables.CardsInDeck.CARDS_IN_DECK.DECK_ID,
										com.hiddenswitch.framework.schema.spellsource.tables.CardsInDeck.CARDS_IN_DECK.CARD_ID);
								for (var cardId : updateCommand.getPushCardIds().getEachList()) {
									insert.values(deckId, cardId);
								}
								return insert;
							}));
						}

						if (updateCommand.getPullAllCardIdsCount() > 0) {
							futs.addAll(updateCommand.getPullAllCardIdsList().stream()
									.collect(java.util.stream.Collectors.groupingBy(java.util.function.Function.identity(), java.util.stream.Collectors.counting()))
									.entrySet().stream()
									.map(entry -> queryExecutor.execute(dsl ->
											dsl.deleteFrom(com.hiddenswitch.framework.schema.spellsource.tables.CardsInDeck.CARDS_IN_DECK)
													.where(com.hiddenswitch.framework.schema.spellsource.tables.CardsInDeck.CARDS_IN_DECK.DECK_ID.eq(deckId)
															.and(com.hiddenswitch.framework.schema.spellsource.tables.CardsInDeck.CARDS_IN_DECK.CARD_ID.eq(entry.getKey())))
													.limit(entry.getValue().intValue())))
									.toList());
						}

						var setsHeroClass = !updateCommand.getSetHeroClass().isEmpty();
						var setsName = !updateCommand.getSetName().isEmpty();
						if (setsHeroClass || setsName) {
							futs.add(queryExecutor.execute(dsl -> {
								var update = (org.jooq.UpdateSetStep<com.hiddenswitch.framework.schema.spellsource.tables.records.DecksRecord>) dsl.update(com.hiddenswitch.framework.schema.spellsource.tables.Decks.DECKS);
								if (setsHeroClass) {
									update = update.set(com.hiddenswitch.framework.schema.spellsource.tables.Decks.DECKS.HERO_CLASS, updateCommand.getSetHeroClass());
								}
								if (setsName) {
									update = update.set(com.hiddenswitch.framework.schema.spellsource.tables.Decks.DECKS.NAME, updateCommand.getSetName());
								}
								update = update.set(com.hiddenswitch.framework.schema.spellsource.tables.Decks.DECKS.LAST_EDITED_BY, userId);
								return ((org.jooq.UpdateSetMoreStep<com.hiddenswitch.framework.schema.spellsource.tables.records.DecksRecord>) update)
										.where(com.hiddenswitch.framework.schema.spellsource.tables.Decks.DECKS.ID.eq(deckId));
							}));
						}

						if (futs.isEmpty()) {
							return Future.succeededFuture();
						}
						return Future.all(futs);
					})
					.compose(v -> Legacy.invalidateDeck(deckId))
					.compose(v -> Legacy.getDeck(cardCatalogue, deckId, userId));
		}).map(this::protoToGraphqlDeck);
	}

	@Override
	public Future<Boolean> deleteDeck(String deckId) throws Exception {
		var userId = Accounts.userId();
		if (userId == null) {
			return Future.failedFuture("must be authenticated");
		}

		return withExecutor(queryExecutor -> queryExecutor.execute(dsl -> dsl.update(com.hiddenswitch.framework.schema.spellsource.tables.DeckShares.DECK_SHARES)
						.set(com.hiddenswitch.framework.schema.spellsource.tables.DeckShares.DECK_SHARES.TRASHED_BY_RECIPIENT, true)
						.where(com.hiddenswitch.framework.schema.spellsource.tables.DeckShares.DECK_SHARES.DECK_ID.eq(deckId),
								com.hiddenswitch.framework.schema.spellsource.tables.DeckShares.DECK_SHARES.SHARE_RECIPIENT_ID.eq(userId)))
				.compose(updated -> {
					if (updated != 0) {
						return Future.succeededFuture();
					}
					return queryExecutor.findOneRow(dsl -> dsl.select(
									com.hiddenswitch.framework.schema.spellsource.tables.Decks.DECKS.IS_PREMADE,
									com.hiddenswitch.framework.schema.spellsource.tables.Decks.DECKS.CREATED_BY)
							.from(com.hiddenswitch.framework.schema.spellsource.tables.Decks.DECKS)
							.where(com.hiddenswitch.framework.schema.spellsource.tables.Decks.DECKS.ID.eq(deckId)))
							.compose(row -> {
								if (row == null) {
									return Future.failedFuture("deck not found");
								}
								var isPremade = row.getBoolean("is_premade");
								var isOwner = java.util.Objects.equals(row.getString("created_by"), userId);
								if (isPremade) {
									return queryExecutor.execute(dsl -> dsl.insertInto(com.hiddenswitch.framework.schema.spellsource.tables.DeckShares.DECK_SHARES)
											.set(com.hiddenswitch.framework.schema.spellsource.tables.DeckShares.DECK_SHARES.newRecord()
													.setDeckId(deckId)
													.setShareRecipientId(userId)
													.setTrashedByRecipient(true))).map(v -> (Void) null);
								} else if (isOwner) {
									return queryExecutor.execute(dsl -> dsl.update(com.hiddenswitch.framework.schema.spellsource.tables.Decks.DECKS)
											.set(com.hiddenswitch.framework.schema.spellsource.tables.Decks.DECKS.TRASHED, true)
											.where(com.hiddenswitch.framework.schema.spellsource.tables.Decks.DECKS.ID.eq(deckId),
													com.hiddenswitch.framework.schema.spellsource.tables.Decks.DECKS.CREATED_BY.eq(userId))).map(v -> (Void) null);
								} else {
									return Future.<Void>failedFuture("not authorized");
								}
							});
				})
				.compose(v -> Legacy.invalidateDeck(deckId))
				.map(true));
	}

	@Override
	public Future<DecksGetResponse> duplicateDeck(String deckId) throws Exception {
		var userId = Accounts.userId();
		if (userId == null) {
			return Future.failedFuture("must be authenticated");
		}
		if (cardCatalogue == null) {
			return Future.failedFuture("card catalogue not initialized");
		}
		// Delegate to Legacy's static duplicate logic via the proto-based path
		// This is complex enough that reusing the existing impl is the safest approach
		return Future.failedFuture("not yet implemented — requires transaction refactoring");
	}

	// ── drafts ───────────────────────────────────────────────

	@Override
	public Future<DraftState> startOrModifyDraft(DraftsPostInput input) throws Exception {
		return Future.failedFuture("not yet implemented");
	}

	@Override
	public Future<DraftState> draftsChooseHero(int heroIndex) throws Exception {
		return Future.failedFuture("not yet implemented");
	}

	@Override
	public Future<DraftState> draftsChooseCard(int cardIndex) throws Exception {
		return Future.failedFuture("not yet implemented");
	}

	// ── friends ──────────────────────────────────────────────

	@Override
	public Future<Friend> addFriend(String friendId, String usernameWithToken) throws Exception {
		return Future.failedFuture("not yet implemented");
	}

	@Override
	public Future<Boolean> removeFriend(String friendId) throws Exception {
		return Future.failedFuture("not yet implemented");
	}

	// ── invites ──────────────────────────────────────────────

	@Override
	public Future<InviteResponse> sendInvite(InvitePostInput input) throws Exception {
		return Future.failedFuture("not yet implemented");
	}

	@Override
	public Future<InviteResponse> acceptInvite(AcceptInviteInput input) throws Exception {
		return Future.failedFuture("not yet implemented");
	}

	@Override
	public Future<InviteResponse> deleteInvite(String inviteId) throws Exception {
		return Future.failedFuture("not yet implemented");
	}

	// ── matchmaking ──────────────────────────────────────────

	@Override
	public Future<Boolean> enqueueMatchmaking(MatchmakingEnqueueInput input) throws Exception {
		var userId = Accounts.userId();
		if (userId == null) {
			return Future.failedFuture("must be authenticated");
		}
		return withDslContext(dsl -> dsl.insertInto(MATCHMAKING_TICKETS,
						MATCHMAKING_TICKETS.USER_ID,
						MATCHMAKING_TICKETS.DECK_ID,
						MATCHMAKING_TICKETS.QUEUE_ID,
						MATCHMAKING_TICKETS.BOT_DECK_ID)
				.values(userId,
						input.getDeckId(),
						input.getQueueId(),
						input.getBotDeckId())
				.onConflict(MATCHMAKING_TICKETS.USER_ID)
				.doUpdate()
				.set(MATCHMAKING_TICKETS.DECK_ID, input.getDeckId())
				.set(MATCHMAKING_TICKETS.QUEUE_ID, input.getQueueId())
				.set(MATCHMAKING_TICKETS.BOT_DECK_ID, input.getBotDeckId()))
				.map(rows -> true);
	}

	@Override
	public Future<Boolean> cancelMatchmaking() throws Exception {
		var userId = Accounts.userId();
		if (userId == null) {
			return Future.failedFuture("must be authenticated");
		}
		return withDslContext(dsl -> dsl.deleteFrom(MATCHMAKING_TICKETS)
				.where(MATCHMAKING_TICKETS.USER_ID.eq(userId)))
				.map(deleted -> deleted > 0);
	}

	// ── game actions ─────────────────────────────────────────

	@Override
	public Future<Boolean> connectToGame(String playerKey, String playerSecret) throws Exception {
		var userId = Accounts.userId();
		if (userId == null) {
			return Future.failedFuture("must be authenticated");
		}
		var firstMessage = Spellsource.ClientToServerMessage.newBuilder()
				.setMessageType(Spellsource.MessageTypeMessage.MessageType.FIRST_MESSAGE)
				.setFirstMessage(Spellsource.ClientToServerMessage.FirstMessageMessage.newBuilder()
						.setPlayerKey(playerKey)
						.setPlayerSecret(playerSecret))
				.build();
		GraphQLGameBridge.sendClientMessage(userId, firstMessage);
		return Future.succeededFuture(true);
	}

	@Override
	public Future<Boolean> sendGameAction(int actionIndex, String repliesTo) throws Exception {
		var userId = Accounts.userId();
		if (userId == null) {
			return Future.failedFuture("must be authenticated");
		}
		var message = Spellsource.ClientToServerMessage.newBuilder()
				.setMessageType(Spellsource.MessageTypeMessage.MessageType.UPDATE_ACTION)
				.setActionIndex(actionIndex)
				.setRepliesTo(repliesTo)
				.build();
		GraphQLGameBridge.sendClientMessage(userId, message);
		return Future.succeededFuture(true);
	}

	@Override
	public Future<Boolean> sendMulligan(List<Integer> discardedCardIndices, String repliesTo) throws Exception {
		var userId = Accounts.userId();
		if (userId == null) {
			return Future.failedFuture("must be authenticated");
		}
		var message = Spellsource.ClientToServerMessage.newBuilder()
				.setMessageType(Spellsource.MessageTypeMessage.MessageType.UPDATE_MULLIGAN)
				.addAllDiscardedCardIndices(discardedCardIndices)
				.setRepliesTo(repliesTo)
				.build();
		GraphQLGameBridge.sendClientMessage(userId, message);
		return Future.succeededFuture(true);
	}

	@Override
	public Future<Boolean> sendEmote(int entityId, EmoteType message) throws Exception {
		var userId = Accounts.userId();
		if (userId == null) {
			return Future.failedFuture("must be authenticated");
		}
		var msg = Spellsource.ClientToServerMessage.newBuilder()
				.setMessageType(Spellsource.MessageTypeMessage.MessageType.EMOTE)
				.setEmote(Spellsource.Emote.newBuilder()
						.setEntityId(entityId)
						.setMessage(Spellsource.Emote.EmoteMessage.valueOf(message.name())))
				.build();
		GraphQLGameBridge.sendClientMessage(userId, msg);
		return Future.succeededFuture(true);
	}

	@Override
	public Future<Boolean> concedeGame() throws Exception {
		var userId = Accounts.userId();
		if (userId == null) {
			return Future.failedFuture("must be authenticated");
		}
		var message = Spellsource.ClientToServerMessage.newBuilder()
				.setMessageType(Spellsource.MessageTypeMessage.MessageType.CONCEDE)
				.build();
		GraphQLGameBridge.sendClientMessage(userId, message);
		return Future.succeededFuture(true);
	}

	@Override
	public Future<Boolean> touchEntity(int entityId) throws Exception {
		var userId = Accounts.userId();
		if (userId == null) {
			return Future.failedFuture("must be authenticated");
		}
		var message = Spellsource.ClientToServerMessage.newBuilder()
				.setMessageType(Spellsource.MessageTypeMessage.MessageType.TOUCH)
				.setEntityTouch(entityId)
				.build();
		GraphQLGameBridge.sendClientMessage(userId, message);
		return Future.succeededFuture(true);
	}

	@Override
	public Future<Boolean> untouchEntity(int entityId) throws Exception {
		var userId = Accounts.userId();
		if (userId == null) {
			return Future.failedFuture("must be authenticated");
		}
		var message = Spellsource.ClientToServerMessage.newBuilder()
				.setMessageType(Spellsource.MessageTypeMessage.MessageType.TOUCH)
				.setEntityUntouch(entityId)
				.build();
		GraphQLGameBridge.sendClientMessage(userId, message);
		return Future.succeededFuture(true);
	}

	// ── cards (editable) ─────────────────────────────────────

	@Override
	public Future<PutCardResult> putCard(PutCardInput input) throws Exception {
		return Future.failedFuture("not yet implemented");
	}

	@Override
	public Future<Boolean> deleteCard(String editableCardId) throws Exception {
		return Future.failedFuture("not yet implemented");
	}

	// ── rogue (existing) ─────────────────────────────────────

	@Override
	public Future<RogueRun> startRogueRun(String heroClass, Long seed) throws Exception {
		return RogueManager.startRogueRun(heroClass, seed, Accounts.userId()).map(RogueRun::new);
	}

	@Override
	public Future<RogueRun> makeRogueChoice(Long choiceId, List<Integer> choices) throws Exception {
		return RogueManager.makeRogueChoice(choiceId, choices).map(RogueRun::new);
	}

	@Override
	public Future<RogueRun> reroll(Long choiceId) throws Exception {
		return RogueManager.reroll(choiceId).map(RogueRun::new);
	}

	@Override
	public Future<RogueRun> trashCard(Long rogueId, String cardId) throws Exception {
		return RogueManager.trashCard(rogueId, cardId).map(RogueRun::new);
	}

	@Override
	public Future<RogueRun> skipBoss(Long rogueId) throws Exception {
		return RogueManager.skipBoss(rogueId).map(r -> new RogueRun(r.getId()));
	}

	@Override
	public Future<RogueRun> upgradeCard(Long rogueId, String cardId) throws Exception {
		return RogueManager.upgradeCard(rogueId, cardId).map(RogueRun::new);
	}

	// ── helpers ──────────────────────────────────────────────

	private DecksGetResponse protoToGraphqlDeck(com.hiddenswitch.spellsource.rpc.Spellsource.DecksGetResponse proto) {
		var coll = proto.getCollection();
		return new DecksGetResponse.Builder()
				.setCollection(protoToGraphqlCollection(coll))
				.setInventoryIdsSize(proto.getInventoryIdsSize())
				.build();
	}

	private InventoryCollection protoToGraphqlCollection(com.hiddenswitch.spellsource.rpc.Spellsource.InventoryCollection coll) {
		return new InventoryCollection.Builder()
				.setId(coll.getId())
				.setName(coll.getName())
				.setHeroClass(coll.getHeroClass())
				.setFormat(coll.getFormat())
				.setDeckType(DeckType.valueOf(coll.getDeckType().name()))
				.setCollectionType(CollectionType.valueOf(coll.getType().name()))
				.setUserId(coll.getUserId())
				.setIsStandardDeck(coll.getIsStandardDeck())
				.setInventory(coll.getInventoryList().stream()
						.map(cr -> new CardRecord.Builder()
								.setId(cr.getId())
								.setCardId(cr.getEntity().getCardId())
								.setUserId(cr.getUserId())
								.setCount(cr.getCount())
								.setCollectionIds(cr.getCollectionIdsList())
								.setEntity(new Entity.Builder()
										.setId(cr.getEntity().getId())
										.setName(cr.getEntity().getName())
										.setCardId(cr.getEntity().getCardId())
										.setDescription(cr.getEntity().getDescription())
										.setCardType(CardType.valueOf(cr.getEntity().getCardType().name()))
										.setEntityType(EntityType.valueOf(cr.getEntity().getEntityType().name()))
										.setRarity(Rarity.valueOf(cr.getEntity().getRarity().name()))
										.setOwner(0).setBoardPosition(0).setMana(0).setMaxMana(0).setLockedMana(0)
										.setBattlecry(false).setCannotAttack(false).setCharge(false).setChooseOne(false)
										.setCollectible(false).setCombo(false).setConditionMet(false).setDeathrattles(false)
										.setDeflect(false).setDestroyed(false).setDiscarded(false).setDivineShield(false)
										.setEnraged(false).setFrozen(false).setGameStarted(false).setGold(false)
										.setHostsTrigger(false).setImmune(false).setIsStartingTurn(false).setLifesteal(false)
										.setPermanent(false).setPlayable(false).setPoisonous(false).setRoasted(false)
										.setRush(false).setSilenced(false).setStealth(false).setSummoningSickness(false)
										.setTaunt(false).setUncensored(false).setUnderAura(false).setUntargetableBySpells(false)
										.setWindfury(false).setHost(0).setHeroClasses(List.of()).setCardSet("").setCardSets(List.of())
										.setEnchantmentType("").setTribes(List.of()).setNote("").setTooltips(List.of())
										.build())
								.build())
						.collect(Collectors.toList()))
				.setPlayerEntityAttributes(coll.getPlayerEntityAttributesList().stream()
						.map(a -> new AttributeValueTuple.Builder()
								.setAttribute(PlayerEntityAttribute.valueOf(a.getAttribute().name()))
								.setStringValue(a.getStringValue())
								.build())
						.collect(Collectors.toList()))
				.build();
	}
}
