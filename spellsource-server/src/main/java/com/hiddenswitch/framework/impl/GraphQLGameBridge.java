package com.hiddenswitch.framework.impl;

import com.hiddenswitch.framework.graphql.*;
import com.hiddenswitch.spellsource.rpc.Spellsource;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageConsumer;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * Bridges the game event bus to GraphQL subscriptions and mutations.
 * <p>
 * The game engine communicates via the Vert.x event bus:
 * <ul>
 *   <li>{@code games:reader:{userId}} — client→server messages</li>
 *   <li>{@code games:writer:{userId}} — server→client messages</li>
 * </ul>
 * <p>
 * This bridge converts proto messages to GraphQL types and provides a
 * {@link Publisher} for the {@code gameMessages} subscription.
 */
public class GraphQLGameBridge {
	private static final Logger LOGGER = LoggerFactory.getLogger(GraphQLGameBridge.class);

	/**
	 * Creates a reactive streams Publisher that emits ServerGameMessage for the given user.
	 * Subscribes to the event bus address {@code games:writer:{userId}}.
	 */
	public static Publisher<ServerGameMessage> gameMessagesPublisher(String userId) {
		return new GameMessagePublisher(userId);
	}

	/**
	 * Sends a ClientToServerMessage to the game engine via the event bus.
	 */
	public static void sendClientMessage(String userId, Spellsource.ClientToServerMessage message) {
		var eventBus = Vertx.currentContext().owner().eventBus();
		var publisher = eventBus.<Spellsource.ClientToServerMessage>publisher(ServerGameContext.getMessagesFromClientAddress(userId));
		var retryPublisher = new RetryMessageProducer<>(publisher, 10, 1000,
				body -> body.getMessageType() == Spellsource.MessageTypeMessage.MessageType.FIRST_MESSAGE);
		retryPublisher.write(message);
	}

	/**
	 * Converts a proto ServerToClientMessage to the GraphQL ServerGameMessage type.
	 */
	public static ServerGameMessage toGraphQL(Spellsource.ServerToClientMessage proto) {
		var builder = new ServerGameMessage.Builder()
				.setMessageType(MessageType.valueOf(proto.getMessageType().name()))
				.setId(proto.getId().isEmpty() ? null : proto.getId())
				.setLocalPlayerId(proto.getLocalPlayerId())
				.setIsReplayMessage(proto.getIsReplayMessage());

		if (proto.hasGameState()) {
			builder.setGameState(toGraphQLGameState(proto.getGameState()));
		}

		if (proto.hasActions()) {
			builder.setActions(toGraphQLActions(proto.getActions()));
		}

		if (proto.getStartingCardsCount() > 0) {
			builder.setStartingCards(proto.getStartingCardsList().stream()
					.map(GraphQLGameBridge::toGraphQLEntity)
					.collect(Collectors.toList()));
		}

		if (proto.hasEvent()) {
			builder.setEvent(toGraphQLEvent(proto.getEvent()));
		}

		if (proto.hasGameOver()) {
			builder.setGameOver(new GameOver.Builder()
					.setLocalPlayerWon(proto.getGameOver().getLocalPlayerWon())
					.setWinningPlayerId(proto.getGameOver().hasWinningPlayerId() ? proto.getGameOver().getWinningPlayerId() : null)
					.build());
		}

		if (proto.hasEmote()) {
			builder.setEmote(new Emote.Builder()
					.setEntityId(proto.getEmote().getEntityId())
					.setMessage(EmoteType.valueOf(proto.getEmote().getMessage().name()))
					.build());
		}

		if (proto.hasTimers()) {
			builder.setTimers(new Timers.Builder()
					.setMillisRemaining(proto.getTimers().getMillisRemaining())
					.build());
		}

		if (proto.hasChanges() && proto.getChanges().getIdsCount() > 0) {
			builder.setChangedEntityIds(proto.getChanges().getIdsList());
		}

		return builder.build();
	}

	private static GameState toGraphQLGameState(Spellsource.GameState proto) {
		return new GameState.Builder()
				.setEntities(proto.getEntitiesList().stream()
						.map(GraphQLGameBridge::toGraphQLEntity)
						.collect(Collectors.toList()))
				.setIsLocalPlayerTurn(proto.getIsLocalPlayerTurn())
				.setTurnNumber(proto.getTurnNumber())
				.setTurnState(proto.getTurnState())
				.setTimestamp(proto.getTimestamp())
				.setPowerHistory(proto.getPowerHistoryList().stream()
						.map(GraphQLGameBridge::toGraphQLEvent)
						.collect(Collectors.toList()))
				.setHasPowerHistory(proto.getHasPowerHistory())
				.build();
	}

	private static GameActions toGraphQLActions(Spellsource.GameActions proto) {
		return new GameActions.Builder()
				.setAll(proto.getAllList().stream()
						.map(GraphQLGameBridge::toGraphQLSpellAction)
						.collect(Collectors.toList()))
				.setCompatibility(proto.getCompatibilityList())
				.build();
	}

	private static SpellAction toGraphQLSpellAction(Spellsource.SpellAction proto) {
		return new SpellAction.Builder()
				.setAction(proto.getAction())
				.setActionType(ActionType.valueOf(proto.getActionType().name()))
				.setSourceId(proto.getSourceId())
				.setDescription(proto.getDescription())
				.setChoices(proto.getChoicesList().stream()
						.map(GraphQLGameBridge::toGraphQLSpellAction)
						.collect(Collectors.toList()))
				.setEntity(proto.hasEntity() ? toGraphQLEntity(proto.getEntity()) : null)
				.setTargetKeyToActions(proto.getTargetKeyToActionsList().stream()
						.map(t -> new TargetActionPair.Builder()
								.setAction(t.getAction())
								.setTarget(t.getTarget())
								.setFriendlyBattlefieldIndex(t.getFriendlyBattlefieldIndex())
								.build())
						.collect(Collectors.toList()))
				.build();
	}

	private static GameEvent toGraphQLEvent(Spellsource.GameEvent proto) {
		return new GameEvent.Builder()
				.setEventType(GameEventType.valueOf(proto.getEventType().name()))
				.setId(proto.getId())
				.setDescription(proto.getDescription())
				.setIsPowerHistory(proto.getIsPowerHistory())
				.setIsSourcePlayerLocal(proto.getIsSourcePlayerLocal())
				.setIsTargetPlayerLocal(proto.getIsTargetPlayerLocal())
				.setSource(proto.hasSource() ? toGraphQLEntity(proto.getSource()) : null)
				.setTarget(proto.hasTarget() ? toGraphQLEntity(proto.getTarget()) : null)
				.setTargets(proto.getTargetsList().stream()
						.map(GraphQLGameBridge::toGraphQLEntity)
						.collect(Collectors.toList()))
				.setValue(proto.hasValue() ? proto.getValue() : null)
				.setCardEvent(proto.hasCardEvent() ? new CardEvent.Builder()
						.setCard(proto.getCardEvent().hasCard() ? toGraphQLEntity(proto.getCardEvent().getCard()) : null)
						.setShowLocal(proto.getCardEvent().getShowLocal())
						.build() : null)
				.setPerformedGameAction(proto.hasPerformedGameAction() ? new PerformedGameActionEvent.Builder()
						.setActionType(ActionType.valueOf(proto.getPerformedGameAction().getActionType().name()))
						.build() : null)
				.setEntityTouched(proto.getEntityTouched() != 0 ? proto.getEntityTouched() : null)
				.setEntityUntouched(proto.getEntityUntouched() != 0 ? proto.getEntityUntouched() : null)
				.build();
	}

	static Entity toGraphQLEntity(Spellsource.Entity proto) {
		return new Entity.Builder()
				.setId(proto.getId())
				.setName(proto.getName())
				.setDescription(proto.getDescription())
				.setCardId(proto.getCardId())
				.setCardType(CardType.valueOf(proto.getCardType().name()))
				.setEntityType(EntityType.valueOf(proto.getEntityType().name()))
				.setRarity(Rarity.valueOf(proto.getRarity().name()))
				.setLocation(proto.hasLocation() ? new EntityLocation.Builder()
						.setIndex(proto.getLocation().getIndex())
						.setZone(Zone.valueOf(proto.getLocation().getZone().name()))
						.setPlayer(proto.getLocation().getPlayer())
						.build() : null)
				.setArt(proto.hasArt() ? toGraphQLArt(proto.getArt()) : null)
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
				.setSpellDamage(proto.hasSpellDamage() ? proto.getSpellDamage() : null)
				.setOverload(proto.hasOverload() ? proto.getOverload() : null)
				.setExtraAttack(proto.hasExtraAttack() ? proto.getExtraAttack() : null)
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
				.setCharges(proto.hasCharges() ? proto.getCharges() : null)
				.setCountUntilCast(proto.hasCountUntilCast() ? proto.getCountUntilCast() : null)
				.setFires(proto.hasFires() ? proto.getFires() : null)
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
				.build();
	}

	private static Art toGraphQLArt(Spellsource.Art proto) {
		return new Art.Builder()
				.setBody(proto.hasBody() ? new ArtFont.Builder()
						.setVertex(proto.getBody().hasVertex() ? toGraphQLColor(proto.getBody().getVertex()) : null)
						.build() : null)
				.setHighlight(proto.hasHighlight() ? toGraphQLColor(proto.getHighlight()) : null)
				.setLoop(proto.hasLoop() ? new ArtPrefab.Builder().setNamed(proto.getLoop().getNamed()).build() : null)
				.setMissile(proto.hasMissile() ? new ArtPrefab.Builder().setNamed(proto.getMissile().getNamed()).build() : null)
				.setOnCast(proto.hasOnCast() ? new ArtPrefab.Builder().setNamed(proto.getOnCast().getNamed()).build() : null)
				.setOnHit(proto.hasOnHit() ? new ArtPrefab.Builder().setNamed(proto.getOnHit().getNamed()).build() : null)
				.setPrimary(proto.hasPrimary() ? toGraphQLColor(proto.getPrimary()) : null)
				.setSecondary(proto.hasSecondary() ? toGraphQLColor(proto.getSecondary()) : null)
				.setShadow(proto.hasShadow() ? toGraphQLColor(proto.getShadow()) : null)
				.setSpell(proto.hasSpell() ? new ArtPrefab.Builder().setNamed(proto.getSpell().getNamed()).build() : null)
				.setSprite(proto.hasSprite() ? new ArtSprite.Builder().setNamed(proto.getSprite().getNamed()).build() : null)
				.setSpriteShadow(proto.hasSpriteShadow() ? new ArtSprite.Builder().setNamed(proto.getSpriteShadow().getNamed()).build() : null)
				.build();
	}

	private static ArtColor toGraphQLColor(Spellsource.Color proto) {
		return new ArtColor.Builder()
				.setR(proto.getR())
				.setG(proto.getG())
				.setB(proto.getB())
				.setA(proto.getA())
				.build();
	}

	/**
	 * A reactive streams Publisher that bridges the Vert.x event bus consumer
	 * to the graphql-java subscription mechanism.
	 */
	private static class GameMessagePublisher implements Publisher<ServerGameMessage> {
		private final String userId;

		GameMessagePublisher(String userId) {
			this.userId = userId;
		}

		@Override
		public void subscribe(Subscriber<? super ServerGameMessage> subscriber) {
			var vertx = Vertx.currentContext().owner();
			var eventBus = vertx.eventBus();
			var address = ServerGameContext.getMessagesFromServerAddress(userId);
			var consumer = eventBus.<Spellsource.ServerToClientMessage>consumer(address);
			var cancelled = new AtomicBoolean(false);

			subscriber.onSubscribe(new Subscription() {
				@Override
				public void request(long n) {
					// Event bus is push-based; we start flowing as soon as subscribed.
					// For backpressure, we'd need buffering, but game messages are
					// low-frequency enough that this isn't a concern.
				}

				@Override
				public void cancel() {
					if (cancelled.compareAndSet(false, true)) {
						consumer.unregister();
					}
				}
			});

			consumer.handler(message -> {
				if (cancelled.get()) return;
				try {
					var graphqlMsg = toGraphQL(message.body());
					subscriber.onNext(graphqlMsg);
				} catch (Exception e) {
					LOGGER.error("failed to convert game message to graphql", e);
				}
			});

			consumer.completion().onFailure(t -> {
				if (!cancelled.get()) {
					subscriber.onError(t);
				}
			});
		}
	}
}
