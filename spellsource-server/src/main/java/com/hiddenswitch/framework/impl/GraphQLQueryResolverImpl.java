package com.hiddenswitch.framework.impl;

import com.hiddenswitch.framework.Accounts;
import com.hiddenswitch.framework.Environment;
import com.hiddenswitch.framework.Games;
import com.hiddenswitch.framework.Legacy;
import com.hiddenswitch.framework.graphql.*;
import com.hiddenswitch.framework.schema.keycloak.tables.daos.UserEntityDao;
import com.hiddenswitch.framework.schema.spellsource.tables.daos.MatchmakingQueuesDao;
import graphql.kickstart.tools.GraphQLQueryResolver;
import io.vertx.core.Future;
import net.demilich.metastone.game.entities.heroes.HeroClass;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class GraphQLQueryResolverImpl implements QueryResolver, GraphQLQueryResolver {

	private final SqlCachedCardCatalogue cardCatalogue;

	public GraphQLQueryResolverImpl() {
		this.cardCatalogue = null;
	}

	public GraphQLQueryResolverImpl(SqlCachedCardCatalogue cardCatalogue) {
		this.cardCatalogue = cardCatalogue;
	}

	// ── auth ─────────────────────────────────────────────────

	@Override
	public Future<String> currentUserId() throws Exception {
		return Future.succeededFuture(Accounts.userId());
	}

	@Override
	public Future<ClientConfiguration> configuration() throws Exception {
		var config = Environment.getConfiguration();
		var authUrl = config.getKeycloak().getPublicAuthUrl();
		return Future.succeededFuture(new ClientConfiguration.Builder()
			.setKeycloakResetPasswordUrl(URI.create(authUrl + "/realms/hiddenswitch/login-actions/reset-credentials").normalize().toString())
			.setKeycloakAccountManagementUrl(URI.create(authUrl + "/realms/hiddenswitch/protocol/openid-connect/token").normalize().toString())
			.setGraphQlUrl(config.getGraphql().getUrl())
			.build());
	}

	// ── cards ────────────────────────────────────────────────

	@Override
	public Future<GetCardsResponse> cards(String ifNoneMatch) throws Exception {
		if (cardCatalogue == null) {
			return Future.failedFuture("card catalogue not initialized");
		}
		var request = com.hiddenswitch.framework.rpc.Hiddenswitch.GetCardsRequest.newBuilder()
			.setIfNoneMatch(ifNoneMatch != null ? ifNoneMatch : "")
			.setUserId("")
			.build();
		try {
			var proto = cardCatalogue.cachedRequest(request);
			return Future.succeededFuture(protoToGraphqlCards(proto));
		} catch (Throwable t) {
			return Future.failedFuture(t);
		}
	}

	@Override
	public Future<GetCardsResponse> cardsByUser(String ifNoneMatch) throws Exception {
		if (cardCatalogue == null) {
			return Future.failedFuture("card catalogue not initialized");
		}
		var userId = Accounts.userId();
		if (userId == null) {
			return Future.failedFuture("must be authenticated");
		}
		var request = com.hiddenswitch.framework.rpc.Hiddenswitch.GetCardsRequest.newBuilder()
			.setIfNoneMatch(ifNoneMatch != null ? ifNoneMatch : "")
			.setUserId(userId)
			.build();
		try {
			var proto = cardCatalogue.cachedRequest(request);
			return Future.succeededFuture(protoToGraphqlCards(proto));
		} catch (Throwable t) {
			return Future.failedFuture(t);
		}
	}

	private GetCardsResponse protoToGraphqlCards(com.hiddenswitch.framework.rpc.Hiddenswitch.GetCardsResponse proto) {
		var cards = proto.hasContent()
			? proto.getContent().getCardsList().stream()
				.map(cr -> new CardRecord.Builder()
									 .setId(cr.getId())
									 .setCardId(cr.getEntity().getCardId())
									 .setUserId(cr.getUserId())
									 .setCount(cr.getCount())
									 .setCollectionIds(cr.getCollectionIdsList())
									 .setEntity(protoToGraphqlEntity(cr.getEntity()))
									 .build())
				.collect(Collectors.toList())
			: Collections.<CardRecord>emptyList();
		return new GetCardsResponse.Builder()
			.setCards(cards)
			.setVersion(proto.getVersion())
			.setCachedOk(proto.getCachedOk())
			.build();
	}

	private Entity protoToGraphqlEntity(com.hiddenswitch.spellsource.rpc.Spellsource.Entity proto) {
		return new Entity.Builder()
			.setId(proto.getId())
			.setName(proto.getName())
			.setDescription(proto.getDescription())
			.setCardId(proto.getCardId())
			.setCardType(CardType.valueOf(proto.getCardType().name()))
			.setEntityType(EntityType.valueOf(proto.getEntityType().name()))
			.setRarity(Rarity.valueOf(proto.getRarity().name()))
			.setOwner(proto.getOwner())
			.setBoardPosition(proto.getBoardPosition())
			.setAttack(proto.hasAttack() ? proto.getAttack() : null)
			.setBaseAttack(proto.hasBaseAttack() ? proto.getBaseAttack() : null)
			.setHp(proto.hasHp() ? proto.getHp() : null)
			.setBaseHp(proto.hasBaseHp() ? proto.getBaseHp() : null)
			.setMaxHp(proto.hasMaxHp() ? proto.getMaxHp() : null)
			.setArmor(proto.hasArmor() ? proto.getArmor() : null)
			.setManaCost(proto.hasManaCost() ? proto.getManaCost() : null)
			.setBaseManaCost(proto.hasBaseManaCost() ? proto.getBaseManaCost() : null)
			.setDurability(proto.hasDurability() ? proto.getDurability() : null)
			.setMana(proto.getMana())
			.setMaxMana(proto.getMaxMana())
			.setLockedMana(proto.getLockedMana())
			.setBattlecry(proto.getBattlecry())
			.setCannotAttack(proto.getCannotAttack())
			.setCharge(proto.getCharge())
			.setChooseOne(proto.getChooseOne())
			.setCollectible(proto.getCollectible())
			.setCombo(proto.getCombo())
			.setConditionMet(proto.getConditionMet())
			.setDeathrattles(proto.getDeathrattles())
			.setDeflect(proto.getDeflect())
			.setDestroyed(proto.getDestroyed())
			.setDiscarded(proto.getDiscarded())
			.setDivineShield(proto.getDivineShield())
			.setEnraged(proto.getEnraged())
			.setFrozen(proto.getFrozen())
			.setGameStarted(proto.getGameStarted())
			.setGold(proto.getGold())
			.setHostsTrigger(proto.getHostsTrigger())
			.setImmune(proto.getImmune())
			.setIsStartingTurn(proto.getIsStartingTurn())
			.setLifesteal(proto.getLifesteal())
			.setPermanent(proto.getPermanent())
			.setPlayable(proto.getPlayable())
			.setPoisonous(proto.getPoisonous())
			.setRoasted(proto.getRoasted())
			.setRush(proto.getRush())
			.setSilenced(proto.getSilenced())
			.setStealth(proto.getStealth())
			.setSummoningSickness(proto.getSummoningSickness())
			.setTaunt(proto.getTaunt())
			.setUncensored(proto.getUncensored())
			.setUnderAura(proto.getUnderAura())
			.setUntargetableBySpells(proto.getUntargetableBySpells())
			.setWindfury(proto.getWindfury())
			.setHost(proto.getHost())
			.setHeroClasses(proto.getHeroClassesList())
			.setCardSet(proto.getCardSet())
			.setCardSets(proto.getCardSetsList())
			.setEnchantmentType(proto.getEnchantmentType())
			.setTribes(proto.getTribesList())
			.setNote(proto.getNote())
			.setTooltips(proto.getTooltipsList().stream()
				.map(t -> new Tooltip.Builder().setText(t.getText()).setKeywords(t.getKeywordsList()).build())
				.collect(Collectors.toList()))
			.setArt(protoToGraphqlArt(proto.getArt()))
			.build();
	}

	private Art protoToGraphqlArt(com.hiddenswitch.spellsource.rpc.Spellsource.Art proto) {
		if (proto == null) {
			return null;
		}
		var builder = new Art.Builder();
		if (proto.hasPrimary()) {
			builder.setPrimary(protoToGraphqlColor(proto.getPrimary()));
		}
		if (proto.hasSecondary()) {
			builder.setSecondary(protoToGraphqlColor(proto.getSecondary()));
		}
		if (proto.hasShadow()) {
			builder.setShadow(protoToGraphqlColor(proto.getShadow()));
		}
		if (proto.hasHighlight()) {
			builder.setHighlight(protoToGraphqlColor(proto.getHighlight()));
		}
		if (proto.hasBody() && proto.getBody().hasVertex()) {
			builder.setBody(new ArtFont.Builder()
					.setVertex(protoToGraphqlColor(proto.getBody().getVertex()))
					.build());
		}
		if (proto.hasSprite() && !proto.getSprite().getNamed().isEmpty()) {
			builder.setSprite(new ArtSprite.Builder().setNamed(proto.getSprite().getNamed()).build());
		}
		if (proto.hasSpriteShadow() && !proto.getSpriteShadow().getNamed().isEmpty()) {
			builder.setSpriteShadow(new ArtSprite.Builder().setNamed(proto.getSpriteShadow().getNamed()).build());
		}
		if (proto.hasSpell() && !proto.getSpell().getNamed().isEmpty()) {
			builder.setSpell(new ArtPrefab.Builder().setNamed(proto.getSpell().getNamed()).build());
		}
		if (proto.hasMissile() && !proto.getMissile().getNamed().isEmpty()) {
			builder.setMissile(new ArtPrefab.Builder().setNamed(proto.getMissile().getNamed()).build());
		}
		if (proto.hasOnHit() && !proto.getOnHit().getNamed().isEmpty()) {
			builder.setOnHit(new ArtPrefab.Builder().setNamed(proto.getOnHit().getNamed()).build());
		}
		if (proto.hasOnCast() && !proto.getOnCast().getNamed().isEmpty()) {
			builder.setOnCast(new ArtPrefab.Builder().setNamed(proto.getOnCast().getNamed()).build());
		}
		if (proto.hasLoop() && !proto.getLoop().getNamed().isEmpty()) {
			builder.setLoop(new ArtPrefab.Builder().setNamed(proto.getLoop().getNamed()).build());
		}
		return builder.build();
	}

	private ArtColor protoToGraphqlColor(com.hiddenswitch.spellsource.rpc.Spellsource.Color proto) {
		return new ArtColor.Builder()
				.setR(proto.getR())
				.setG(proto.getG())
				.setB(proto.getB())
				.setA(proto.getA())
				.build();
	}

	// ── account ──────────────────────────────────────────────

	@Override
	public Future<UserEntity> account() throws Exception {
		var userId = Accounts.userId();
		if (userId == null) {
			return Future.failedFuture("must be authenticated");
		}
		return Accounts.user(userId)
			.map(ue -> new UserEntity.Builder()
				.setId(ue.getId())
				.setUsername(ue.getUsername())
				.setEmail(ue.getEmail())
				.setPrivacyToken("")
				.build());
	}

	@Override
	public Future<List<UserEntity>> accounts(List<String> userIds) throws Exception {
		var userId = Accounts.userId();
		if (userId == null) {
			return Future.failedFuture("must be authenticated");
		}
		var dao = new UserEntityDao(Environment.jooqAkaDaoConfiguration(), Environment.sqlClient());
		return dao.findManyByIds(userIds)
			.map(users -> users.stream().map(ue -> {
				var builder = new UserEntity.Builder()
					.setId(ue.getId())
					.setUsername(ue.getUsername())
					.setPrivacyToken("");
				// only show email for the requesting user's own account
				if (ue.getId().equals(userId)) {
					builder.setEmail(ue.getEmail());
				} else {
					builder.setEmail("");
				}
				return builder.build();
			}).collect(Collectors.toList()));
	}

	// ── decks ────────────────────────────────────────────────

	@Override
	public Future<DecksGetResponse> deck(String deckId) throws Exception {
		var userId = Accounts.userId();
		if (userId == null) {
			return Future.failedFuture("must be authenticated");
		}
		if (cardCatalogue == null) {
			return Future.failedFuture("card catalogue not initialized");
		}
		return Legacy.getDeck(cardCatalogue, deckId, userId)
			.map(this::protoToGraphqlDeck);
	}

	@Override
	public Future<List<DecksGetResponse>> decks() throws Exception {
		var userId = Accounts.userId();
		if (userId == null) {
			return Future.failedFuture("must be authenticated");
		}
		if (cardCatalogue == null) {
			return Future.failedFuture("card catalogue not initialized");
		}
		return Legacy.getAllDecks(cardCatalogue, userId)
			.map(proto -> proto.getDecksList().stream()
				.map(this::protoToGraphqlDeck)
				.collect(Collectors.toList()));
	}

	private DecksGetResponse protoToGraphqlDeck(com.hiddenswitch.spellsource.rpc.Spellsource.DecksGetResponse proto) {
		var coll = proto.getCollection();
		var inventoryCollection = new InventoryCollection.Builder()
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
					.setEntity(protoToGraphqlEntity(cr.getEntity()))
					.build())
				.collect(Collectors.toList()))
			.setPlayerEntityAttributes(coll.getPlayerEntityAttributesList().stream()
				.map(a -> new AttributeValueTuple.Builder()
					.setAttribute(PlayerEntityAttribute.valueOf(a.getAttribute().name()))
					.setStringValue(a.getStringValue())
					.build())
				.collect(Collectors.toList()))
			.setValidationReport(coll.hasValidationReport()
				? new ValidationReport.Builder()
					.setValid(coll.getValidationReport().getValid())
					.setErrors(coll.getValidationReport().getErrorsList())
					.build()
				: null)
			.build();
		return new DecksGetResponse.Builder()
			.setCollection(inventoryCollection)
			.setInventoryIdsSize(proto.getInventoryIdsSize())
			.build();
	}

	// ── drafts ───────────────────────────────────────────────

	@Override
	public Future<DraftState> draft() throws Exception {
		// Drafts are not yet implemented in the gRPC Legacy service either (stubs return unimplemented)
		return Future.failedFuture("not yet implemented");
	}

	// ── friends ──────────────────────────────────────────────

	@Override
	public Future<List<Friend>> friends() throws Exception {
		// Friends are managed via server-streaming in gRPC; query snapshot not available without DB schema
		return Future.failedFuture("not yet implemented");
	}

	// ── invites ──────────────────────────────────────────────

	@Override
	public Future<InviteResponse> invite(String inviteId) throws Exception {
		// Invites not yet migrated
		return Future.failedFuture("not yet implemented");
	}

	@Override
	public Future<List<Invite>> invites() throws Exception {
		// Invites not yet migrated
		return Future.failedFuture("not yet implemented");
	}

	// ── matchmaking ──────────────────────────────────────────

	@Override
	public Future<List<MatchmakingQueue>> matchmakingQueues() throws Exception {
		var dao = new MatchmakingQueuesDao(Environment.jooqAkaDaoConfiguration(), Environment.sqlClient());
		return dao.findAll()
			.map(queues -> queues.stream().map(q -> new MatchmakingQueue.Builder()
				.setQueueId(q.getId())
				.setName(q.getName())
				.setDescription(q.getName())
				.setTooltip("")
				.setRequires(new MatchmakingQueueRequires.Builder()
					.setDeck(!q.getBotOpponent())
					.setHeroClass(false)
					.setDeckIdChoices(Collections.emptyList())
					.build())
				.build()).collect(Collectors.toList()));
	}

	@Override
	public Future<String> isInMatch() throws Exception {
		var userId = Accounts.userId();
		if (userId == null) {
			return Future.succeededFuture(null);
		}
		return Games.getGameId(userId);
	}

	// ── game records ─────────────────────────────────────────

	@Override
	public Future<GameRecord> gameRecord(String gameId) throws Exception {
		// Game records not yet migrated — needs game_records table access
		return Future.failedFuture("not yet implemented");
	}

	@Override
	public Future<List<String>> gameRecordIds() throws Exception {
		// Game record IDs not yet migrated
		return Future.failedFuture("not yet implemented");
	}

	// ── rogue (existing) ─────────────────────────────────────

	@Override
	public Future<List<String>> currentRogueClasses() throws Exception {
		return Future.succeededFuture(List.of(HeroClass.COPPER, HeroClass.TOAST));
	}

	@Override
	public Future<Integer> rerollCost(Long rogueId) throws Exception {
		return RogueManager.rerollCost(rogueId);
	}

	@Override
	public Future<Integer> trashCardCost(Long rogueId, String cardId) throws Exception {
		return Future.succeededFuture(1);
	}

	@Override
	public Future<Integer> upgradeCardCost(Long rogueId, String cardId) throws Exception {
		return RogueManager.upgradeCardCost(rogueId, cardId);
	}
}
