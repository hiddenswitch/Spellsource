package com.hiddenswitch.framework.impl;

import com.google.common.collect.LinkedHashMultiset;
import com.hiddenswitch.framework.Environment;
import com.hiddenswitch.framework.Legacy;
import com.hiddenswitch.framework.schema.spellsource.Routines;
import com.hiddenswitch.framework.schema.spellsource.enums.RogueChoiceType;
import com.hiddenswitch.framework.schema.spellsource.enums.RogueRunState;
import com.hiddenswitch.framework.schema.spellsource.tables.mappers.RowMappers;
import com.hiddenswitch.framework.schema.spellsource.tables.pojos.RogueChoice;
import com.hiddenswitch.framework.schema.spellsource.tables.pojos.RogueRun;
import com.hiddenswitch.framework.schema.spellsource.tables.records.RogueChoiceRecord;
import com.hiddenswitch.framework.schema.spellsource.tables.records.RogueRunRecord;
import com.hiddenswitch.spellsource.rpc.Spellsource;
import com.hiddenswitch.spellsource.rpc.Spellsource.CardTypeMessage.CardType;
import io.vertx.core.Future;
import io.vertx.sqlclient.Row;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardArrayList;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.events.RogueChoiceEvent;
import net.demilich.metastone.game.logic.XORShiftRandom;
import net.demilich.metastone.game.targeting.TargetSelection;
import org.jooq.DSLContext;
import org.jooq.ResultQuery;
import org.jooq.UpdateSetFirstStep;
import org.jooq.UpdateSetMoreStep;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import static com.hiddenswitch.framework.schema.spellsource.Tables.*;
import static io.vertx.await.Async.await;
import static net.demilich.metastone.game.GameContext.PLAYER_1;

public class RogueManager {

	public static final int WIN_COUNT = 8;

	public static final SqlCachedCardCatalogue cardCatalogue = new SqlCachedCardCatalogue();

	public static void initCardCatalogue() {
		cardCatalogue.subscribe();
		cardCatalogue.invalidateAllAndRefresh();
	}

	public static Future<Long> startRogueRun(String heroClass, Long seed, String userId) {
		var rogueRun =
			await(Environment.callRoutine(Routines.startRogueRun(heroClass, seed)).withUserId(userId).execute(RowMappers.getRogueRunMapper()));

		seed = rogueRun.getSeed();

		var random = new XORShiftRandom(seed);

		var deckCards = new ArrayList<>(getCardChoices(random, rogueRun.getHeroClass(), RogueChoiceType.STANDARD, 15,
			null));
		deckCards.add("level_1");
		var deck = new CardArrayList(deckCards.stream().map(cardCatalogue::getCardById).toList());

		await(Environment.callRoutine(Routines.setCardsInDeck(rogueRun.getDeck(), deckCards.toArray(String[]::new))).execute(RowMappers.getCardsInDeckMapper()));

		await(updateOpponentDeck(rogueRun, random));

		var equipment = getCardChoices(random, rogueRun.getHeroClass(), RogueChoiceType.EQUIPMENT, 3, deck);
		if (!equipment.isEmpty()) {
			await(addNewRogueChoice(r -> r.setType(RogueChoiceType.EQUIPMENT).setRogueRun(rogueRun.getId()).setIndex(0).setCards(equipment.toArray(String[]::new)).setCanPick(1)));
		}

		await(updateRogueRun(rogueRun.getId(), r -> r.set(ROGUE_RUN.STATE, RogueRunState.CHOICE).set(ROGUE_RUN.SEED_STATE,
			random.getState())));

		return Future.succeededFuture(rogueRun.getId());
	}

	public static Future<Long> makeRogueChoice(long choiceId, List<Integer> choices) {
		if (choices.stream().anyMatch(i -> i < 0)) {
			return Future.failedFuture("Choice index invalid");
		}

		var choice = await(getRogueChoice(choiceId));

		if (choice == null) {
			return Future.failedFuture("Choice not found");
		}

		if (choices.size() > choice.getCanPick()) {
			return Future.failedFuture("Invalid number of choices");
		}

		if (choices.stream().anyMatch(i -> i >= choice.getCards().length)) {
			return Future.failedFuture("Choice index invalid");
		}

		var rogueRun = await(getRogueRun(choice.getRogueRun()));

		if (rogueRun.getState() != RogueRunState.CHOICE) {
			return Future.failedFuture("Cannot make choice in current state");
		}

		var seedState = rogueRun.getSeedState();

		var deck = await(getDeck(rogueRun.getDeck()));

		if (!choices.isEmpty()) {
			var userId = rogueRun.getPlayer();
			var deckId = rogueRun.getDeck();
			var context = new RogueChoiceGameContext(cardCatalogue, rogueRun.getHeroClass(), deck, userId, deckId,
				rogueRun.getSeed());
			context.init(); // TODO add info for rogue run id ?
			var player = context.getPlayer1();

			context.setActivePlayerId(player.getId());
			context.getLogic().getRandom().setState(seedState);

			for (var index : choices) {
				var cardId = choice.getCards()[index];

				var card = cardCatalogue.getCardById(cardId);
				card.setId(context.getLogic().generateId());
				card.setOwner(PLAYER_1);
				card.moveOrAddTo(context, player.getDiscoverZone().getZone());

				if (card.getCardType() != CardType.ROGUE_CHOICE) {
					context.getLogic().shuffleToDeck(player, card);
				}

				if (card.getDesc().getRogueInfo() != null && card.getDesc().getRogueInfo().getChosenSpell() != null) {
					context.getLogic().castSpell(PLAYER_1, card.getDesc().getRogueInfo().getChosenSpell(), card.getReference(),
						null, TargetSelection.NONE, false, null);
				}

				context.getLogic().fireGameEvent(new RogueChoiceEvent(context, player, card));
			}

			seedState = context.getLogic().getRandom().getState();

			var newDeck = context.getPlayer1().getDeck().stream().map(Card::getCardId).toArray(String[]::new);
			deck = new CardArrayList(Arrays.stream(newDeck).map(cardCatalogue::getCardById).toList());

			await(Environment.callRoutine(Routines.setCardsInDeck(deckId, newDeck)).execute(RowMappers.getCardsInDeckMapper()));
		}

		Legacy.invalidateDeck(rogueRun.getDeck());

		if (!choice.getRepopulate() || choices.size() >= choice.getCanPick() || choices.isEmpty()) {
			await(Environment.withDslContext(dsl -> dsl.deleteFrom(ROGUE_CHOICE).where(ROGUE_CHOICE.ID.eq(choiceId))));
		} else {
			var random = new XORShiftRandom(seedState);
			var newCards = getCardChoices(random, rogueRun.getHeroClass(), choice.getType(),
				choices.size(), deck);
			seedState = random.getState();

			var newChoices = choice.getCards();
			for (int i = 0; i < choices.size(); i++) {
				newChoices[choices.get(i)] = newCards.get(i);
			}

			await(updateRogueChoice(choiceId, c -> c.set(ROGUE_CHOICE.CARDS, newChoices).set(ROGUE_CHOICE.CAN_PICK,
				choice.getCanPick() - choices.size())));
		}

		var newChoice =
			await(Environment.callRoutine(Routines.currentRogueChoice(rogueRun.getId())).execute(RowMappers.getRogueChoiceMapper()));

		var finalSeedState = seedState;
		await(updateRogueRun(rogueRun.getId(), r -> r.set(ROGUE_RUN.STATE,
			(newChoice == null || newChoice.getId() == null) ? RogueRunState.PRE_MATCH : RogueRunState.CHOICE).set(ROGUE_RUN.SEED_STATE, finalSeedState)));

		return Future.succeededFuture(rogueRun.getId());
	}

	public static Future<RogueChoice> currentRogueChoice(long rogueId) {
		return Environment.callRoutine(Routines.currentRogueChoice(rogueId)).execute(RogueManager.rogueChoiceMapper());
	}

	public static Future<RogueRun> makeAnyChoices(long rogueId) {
		while (true) {
			var choice = await(currentRogueChoice(rogueId));

			if (choice == null || choice.getId() == null || choice.getId() == 0) {
				break;
			}

			await(RogueManager.makeRogueChoice(choice.getId(), List.of()));
		}

		return getRogueRun(rogueId);
	}

	public static Future<RogueChoice> addNewRogueChoice(long rogueId, String[] cards, int index, int canPick) {
		return returningRogueChoice(dsl -> dsl.insertInto(ROGUE_CHOICE).set(ROGUE_CHOICE.newRecord().setRogueRun(rogueId).setCards(cards).setIndex(index).setCanPick(canPick)).returning());
	}

	public static Future<RogueChoice> addNewRogueChoice(Function<RogueChoiceRecord, RogueChoiceRecord> handler) {
		return returningRogueChoice(dsl -> dsl.insertInto(ROGUE_CHOICE).set(handler.apply(ROGUE_CHOICE.newRecord())).returning());
	}

	public static Future<CardArrayList> getDeck(String deckId) {
		var deckCards = await(Environment.callRoutine(Routines.getCardsInDeck(deckId)).execute(row -> row.getString(0)));

		return Future.succeededFuture(new CardArrayList(deckCards.stream().map(cardCatalogue::getCardById).toList()));
	}

	public static Future<Long> reroll(long choiceId) {
		var choice = await(getRogueChoice(choiceId));

		if (!choice.getCanReroll()) {
			return Future.failedFuture("Can't reroll this choice");
		}

		var rogueRun = await(getRogueRun(choice.getRogueRun()));

		var cost = await(rerollCost(rogueRun));

		if (rogueRun.getGold() < cost) {
			return Future.failedFuture("Not enough gold to reroll");
		}

		var random = new XORShiftRandom(rogueRun.getSeedState());

		var deck = await(getDeck(rogueRun.getDeck()));

		var newChoices =
			getCardChoices(random, rogueRun.getHeroClass(), choice.getType(), choice.getCards().length, deck).toArray(String[]::new);

		await(updateRogueChoice(choiceId, c -> c.set(ROGUE_CHOICE.CARDS, newChoices)));
		await(updateRogueRun(rogueRun.getId(),
			r -> r.set(ROGUE_RUN.GOLD, rogueRun.getGold() - cost).set(ROGUE_RUN.SEED_STATE, random.getState())));

		return Future.succeededFuture(rogueRun.getId());
	}

	public static Future<Integer> rerollCost(RogueRun rogueRun) {
		return Future.succeededFuture(1);
	}

	public static Future<Integer> rerollCost(long rogueId) {
		var rogueRun = await(getRogueRun(rogueId));

		return rerollCost(rogueRun);
	}

	public static Future<Long> trashCard(Long rogueId, String cardId) {
		if (cardId.startsWith("level_")) {
			return Future.failedFuture("Can't trash that card");
		}

		var rogueRun = await(getRogueRun(rogueId));

		var count =
			await(Environment.withDslContext(dsl -> dsl.deleteFrom(CARDS_IN_DECK).where(CARDS_IN_DECK.DECK_ID.eq(rogueRun.getDeck()).and(CARDS_IN_DECK.CARD_ID.eq(cardId))).limit(1)));

		if (count < 1) {
			return Future.failedFuture("Card did not exist within deck to trash");
		}

		return Future.succeededFuture(rogueRun.getId());
	}

	public static Future<RogueRun> handleGameStart(String deckId, long gameId) {
		return Environment.callRoutine(Routines.checkRogueGameStart(deckId, gameId)).execute(RowMappers.getRogueRunMapper());
	}

	public static Future<RogueRun> handleGameEnd(long gameId, String winnerUserId) {
		var result =
			await(Environment.callRoutine(Routines.checkRogueGameEnd(gameId, winnerUserId)).execute(RowMappers.getRogueRunMapper()));

		if (result == null || result.getId() == null) {
			return Future.succeededFuture(null);
		}

		return progressRun(result, result.getPlayer().equals(winnerUserId));
	}

	public static Future<RogueRun> progressRun(RogueRun rogueRun, boolean bossDefeated) {
		var random = new XORShiftRandom(rogueRun.getSeedState());
		var deck = await(getDeck(rogueRun.getDeck()));

		var newBossesDefeated = bossDefeated ? rogueRun.getBossesDefeated() + 1 : rogueRun.getBossesDefeated();
		if (newBossesDefeated >= WIN_COUNT) {
			await(updateRogueRun(rogueRun.getId(), r -> r.set(ROGUE_RUN.BOSSES_DEFEATED, newBossesDefeated)));

			var result =
				await(Environment.callRoutine(Routines.endRogueRun(rogueRun.getId())).execute(RowMappers.getRogueRunMapper()));

			return Future.succeededFuture(result);
		}

		var newGold = rogueRun.getGold() + (bossDefeated ? newBossesDefeated : 0);
		var newState = bossDefeated ? RogueRunState.CHOICE : RogueRunState.PRE_MATCH;

		if (bossDefeated) {
			var equipment = getCardChoices(random, rogueRun.getHeroClass(), RogueChoiceType.EQUIPMENT, 3, deck);

			if (!equipment.isEmpty()) {
				await(addNewRogueChoice(r -> r.setType(RogueChoiceType.EQUIPMENT).setRogueRun(rogueRun.getId()).setIndex(0).setCards(equipment.toArray(String[]::new)).setCanPick(1)));
			}

			var cards = getCardChoices(random, rogueRun.getHeroClass(), RogueChoiceType.STANDARD, 3, deck);
			if (!cards.isEmpty()) {
				await(addNewRogueChoice(r -> r.setType(RogueChoiceType.STANDARD).setRogueRun(rogueRun.getId()).setIndex(1).setCards(cards.toArray(String[]::new)).setCanPick(3).setRepopulate(true).setCanReroll(true)));
			}

			await(Environment.withDslContext(dsl -> dsl.update(CARDS_IN_DECK).set(CARDS_IN_DECK.CARD_ID,
				"level_" + (newBossesDefeated + 1)).where(CARDS_IN_DECK.CARD_ID.startsWith("level_"))));
			Legacy.invalidateDeck(rogueRun.getDeck());
		}

		await(updateOpponentDeck(rogueRun, random));

		var result = await(updateRogueRun(rogueRun.getId(), r -> r.set(ROGUE_RUN.GOLD, newGold).set(ROGUE_RUN.SEED_STATE,
			random.getState()).set(ROGUE_RUN.STATE, newState).set(ROGUE_RUN.BOSSES_DEFEATED, newBossesDefeated)));

		return Future.succeededFuture(result);
	}

	public static Future<RogueRun> skipBoss(Long rogueId) {
		var rogueRun = await(getRogueRun(rogueId));

		return RogueManager.progressRun(rogueRun, true);
	}

	public static Future<Void> updateOpponentDeck(RogueRun rogueRun, XORShiftRandom random) {

		var premadeDecks = Legacy.getPremadeDecks();
		var deck = premadeDecks.get(random.nextInt(premadeDecks.size()));

		await(Environment.withDslContext(dsl -> dsl.update(DECKS).set(DECKS.HERO_CLASS, deck.getHeroClass()).where(DECKS.ID.eq(rogueRun.getOpponentDeck()))));
		await(Environment.callRoutine(Routines.setCardsInDeck(rogueRun.getOpponentDeck(),
			deck.getCardIds().toArray(String[]::new))).execute(RowMappers.getCardsInDeckMapper()));

		Legacy.invalidateDeck(rogueRun.getOpponentDeck());

		await(Environment.withDslContext(dsl -> dsl.insertInto(CARDS_IN_DECK, CARDS_IN_DECK.DECK_ID,
			CARDS_IN_DECK.CARD_ID).values(rogueRun.getOpponentDeck(), "level_" + (rogueRun.getBossesDefeated() + 1))));

		return Future.succeededFuture();
	}

	public static Future<Integer> upgradeCardCost(RogueRun rogueRun, String cardId) {
		var card = cardCatalogue.getCardById(cardId);

		return Future.succeededFuture(switch (card.getRarity()) {
			case LEGENDARY -> 8;
			case EPIC -> 4;
			case RARE -> 2;
			case COMMON -> 1;
			default -> 0;
		});
	}

	public static Future<Integer> upgradeCardCost(long rogueId, String cardId) {
		var rogueRun = await(getRogueRun(rogueId));
		return upgradeCardCost(rogueRun, cardId);
	}

	public static Future<Long> upgradeCard(Long rogueId, String cardId) {
		var rogueRun = await(getRogueRun(rogueId));

		var card = cardCatalogue.getCardById(cardId);

		if (!card.hasAttribute(Attribute.UPGRADE)) {
			return Future.failedFuture("That card can't be upgraded");
		}
		var upgradeCard = (String) card.getAttribute(Attribute.UPGRADE);

		var cost = await(upgradeCardCost(rogueRun, cardId));

		if (rogueRun.getGold() < cost) {
			return Future.failedFuture("Not enough gold to upgrade card");
		}

		await(Environment.withDslContext(dsl -> dsl.update(CARDS_IN_DECK).set(CARDS_IN_DECK.CARD_ID,
			upgradeCard).where(CARDS_IN_DECK.CARD_ID.eq("cardId")).limit(1)));

		await(updateRogueRun(rogueRun.getId(), r -> r.set(ROGUE_RUN.GOLD, rogueRun.getGold() - cost)));

		return Future.succeededFuture(rogueId);
	}

	public static Future<RogueRun> returningRogueRun(Function<DSLContext, ResultQuery<RogueRunRecord>> handler) {
		return Environment.withExecutor(executor -> executor.findOneRow(handler).map(row -> row == null ? null :
			RowMappers.getRogueRunMapper().apply(row)));
	}

	public static Future<RogueChoice> returningRogueChoice(Function<DSLContext, ResultQuery<RogueChoiceRecord>> handler) {
		return Environment.withExecutor(executor -> executor.findOneRow(handler).map(row -> row == null ? null :
			rogueChoiceMapper().apply(row)));
	}

	public static Future<RogueRun> getRogueRun(long rogueId) {
		return returningRogueRun(dsl -> dsl.selectFrom(ROGUE_RUN).where(ROGUE_RUN.ID.eq(rogueId)));
	}

	public static Future<RogueRun> getRogueRun(String deckId) {
		return returningRogueRun(dsl -> dsl.selectFrom(ROGUE_RUN).where(ROGUE_RUN.DECK.eq(deckId)));
	}

	public static Future<RogueChoice> getRogueChoice(long choiceId) {
		return returningRogueChoice(dsl -> dsl.selectFrom(ROGUE_CHOICE).where(ROGUE_CHOICE.ID.eq(choiceId)));
	}

	public static Future<RogueRun> updateRogueRun(long rogueId, Function<UpdateSetFirstStep<RogueRunRecord>,
		UpdateSetMoreStep<RogueRunRecord>> handler) {
		return returningRogueRun(dsl -> handler.apply(dsl.update(ROGUE_RUN)).where(ROGUE_RUN.ID.eq(rogueId)).returning());
	}

	public static Future<RogueChoice> updateRogueChoice(long choiceId, Function<UpdateSetFirstStep<RogueChoiceRecord>,
		UpdateSetMoreStep<RogueChoiceRecord>> handler) {
		return returningRogueChoice(dsl -> handler.apply(dsl.update(ROGUE_CHOICE)).where(ROGUE_CHOICE.ID.eq(choiceId)).returning());
	}

	public static Function<Row, RogueChoice> rogueChoiceMapper() {
		return row -> row == null ? null : RowMappers.getRogueChoiceMapper().apply(row).setCards(row.getArrayOfStrings(
			"cards"));
	}

	public static boolean filterCard(RogueChoiceType choiceType, Card card, @Nullable CardList deck) {
		if (deck != null && (card.getRarity() == Spellsource.RarityMessage.Rarity.LEGENDARY || card.hasAttribute(Attribute.EQUIPMENT)) && deck.stream().anyMatch(c -> c.getCardId().equals(card.getCardId()))) {
			return false;
		}

		return switch (choiceType) {
			case STANDARD -> card.isCollectible();
			case EQUIPMENT -> card.hasAttribute(Attribute.EQUIPMENT);
		};
	}

	public static List<String> getCardChoices(XORShiftRandom random, String heroClass, RogueChoiceType choiceType,
																						int howMany, @Nullable CardList deck) {
		var format = cardCatalogue.getFormat("Rogue");
		var cards = cardCatalogue.query(format,
			card -> (card.hasHeroClass(heroClass) || card.hasHeroClass(HeroClass.ANY)) && filterCard(choiceType, card,
				deck));

		var weightedOptions = LinkedHashMultiset.<Card>create();
		cards.forEach(card -> {
			var weight = 1;
			if (Arrays.stream(card.getHeroClasses()).anyMatch(heroClass::equals)) {
				weight *= 2;
			}
			if (card.getCardType() != CardType.MINION) {
				weight *= 2;
			}

			if (choiceType == RogueChoiceType.EQUIPMENT) {
				weight *= switch (card.getRarity()) {
					case LEGENDARY -> 8;
					case EPIC -> 4;
					case RARE -> 2;
					default -> 1;
				};
			}

			weightedOptions.add(card, weight);
		});

		var results = new ArrayList<String>();
		var weightedList = new ArrayList<>(weightedOptions.stream().toList());

		while (results.size() < howMany && !weightedList.isEmpty()) {
			var index = random.nextInt(weightedList.size());
			var card = weightedList.get(index);

			results.add(card.getCardId());
			weightedList.removeIf(c -> c.getCardId().equals(card.getCardId()));
		}

		return results;
	}

}
