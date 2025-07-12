package com.hiddenswitch.framework.impl;

import com.hiddenswitch.framework.Environment;
import com.hiddenswitch.framework.schema.spellsource.Routines;
import com.hiddenswitch.framework.schema.spellsource.Tables;
import com.hiddenswitch.framework.schema.spellsource.enums.RogueRunState;
import com.hiddenswitch.framework.schema.spellsource.tables.mappers.RowMappers;
import com.hiddenswitch.framework.schema.spellsource.tables.pojos.RogueChoice;
import com.hiddenswitch.framework.schema.spellsource.tables.pojos.RogueRun;
import com.hiddenswitch.framework.schema.spellsource.tables.records.RogueChoiceRecord;
import com.hiddenswitch.framework.schema.spellsource.tables.records.RogueRunRecord;
import com.hiddenswitch.spellsource.rpc.Spellsource.CardTypeMessage.CardType;
import io.vertx.core.Future;
import io.vertx.sqlclient.Row;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardArrayList;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.events.RogueChoiceEvent;
import net.demilich.metastone.game.targeting.TargetSelection;
import org.jooq.DSLContext;
import org.jooq.ResultQuery;
import org.jooq.UpdateSetFirstStep;
import org.jooq.UpdateSetMoreStep;

import java.util.List;
import java.util.function.Function;

import static com.hiddenswitch.framework.schema.spellsource.Tables.ROGUE_CHOICE;
import static com.hiddenswitch.framework.schema.spellsource.Tables.ROGUE_RUN;
import static io.vertx.await.Async.await;
import static net.demilich.metastone.game.GameContext.PLAYER_1;

public class RogueManager {

	public static final SqlCachedCardCatalogue cardCatalogue = new SqlCachedCardCatalogue();

	public static void initCardCatalogue() {
		cardCatalogue.subscribe();
		cardCatalogue.invalidateAllAndRefresh();
	}

	public static Future<Long> startRogueRun(String heroClass, long seed, String userId) {
		var rogueRun = await(Environment.callRoutine(Routines.startRogueRun(heroClass, seed)).withUserId(userId).execute(RowMappers.getRogueRunMapper()));

		var format = cardCatalogue.getFormat("Rogue");
		var testCards = cardCatalogue.query(format).stream().filter(card -> card.hasHeroClass(HeroClass.ANY) && card.isCollectible()).limit(10);

		await(Future.all(testCards.map(card -> Environment.withDslContext(dsl -> dsl.insertInto(Tables.CARDS_IN_DECK).set(Tables.CARDS_IN_DECK.newRecord().setDeckId(rogueRun.getDeck()).setCardId(card.getCardId())))).toList()));

		// TODO set opponent deck cards

		// TODO change state to pre match OR give initial set of choices

		await(updateRogueRun(rogueRun.getId(), r -> r.set(ROGUE_RUN.STATE, RogueRunState.PRE_MATCH)));

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

		if (choices.size() != choice.getCanPick()) {
			return Future.failedFuture("Invalid number of choices");
		}

		if (choices.stream().anyMatch(i -> i >= choice.getCards().length)) {
			return Future.failedFuture("Choice index invalid");
		}

		var rogueRun = await(getRogueRun(choice.getRogueRun()));

		if (rogueRun.getState() != RogueRunState.CHOICE) {
			return Future.failedFuture("Cannot make choice in current state");
		}

		var userId = rogueRun.getPlayer();
		var deckId = rogueRun.getDeck();

		var deckCards = await(Environment.callRoutine(Routines.getCardsInDeck(rogueRun.getDeck())).execute(row -> row.getString(0)));
		var deck = new CardArrayList(deckCards.stream().map(cardCatalogue::getCardById).toList());

		var context = new RogueChoiceGameContext(cardCatalogue, rogueRun.getHeroClass(), deck, userId, deckId, rogueRun.getSeed());
		context.init(); // TODO add info for rogue run id ?
		var player = context.getPlayer1();

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
				context.getLogic().castSpell(PLAYER_1, card.getDesc().getRogueInfo().getChosenSpell(), card.getReference(), null, TargetSelection.NONE, false, null);
			}

			context.getLogic().fireGameEvent(new RogueChoiceEvent(context, player, card));
		}

		await(Environment.callRoutine(Routines.setCardsInDeck(deckId, context.getPlayer1().getDeck().stream().map(Card::getCardId).toArray(String[]::new))).execute(RowMappers.getCardsInDeckMapper()));

		await(Environment.withDslContext(dsl -> dsl.deleteFrom(ROGUE_CHOICE).where(ROGUE_CHOICE.ID.eq(choiceId))));

		var newChoice = await(Environment.callRoutine(Routines.currentRogueChoice(rogueRun.getId())).execute(RowMappers.getRogueChoiceMapper()));

		updateRogueRun(rogueRun.getId(), r -> r.set(ROGUE_RUN.STATE, newChoice == null ? RogueRunState.PRE_MATCH : RogueRunState.CHOICE).set(ROGUE_RUN.SEED, rogueRun.getSeed() + 1));

		return Future.succeededFuture(rogueRun.getId());
	}

	public static Future<RogueChoice> addNewRogueChoice(long rogueId, String[] cards, int index, int canPick) {
		return returningRogueChoice(dsl -> dsl.insertInto(ROGUE_CHOICE).set(ROGUE_CHOICE.newRecord().setRogueRun(rogueId).setCards(cards).setIndex(index).setCanPick(canPick)).returning());
	}

	public static Future<Boolean> handleGameStart(String deckId, long gameId) {
		return Environment.callRoutine(Routines.checkRogueGameStart(deckId, gameId)).execute();
	}

	public static Future<Boolean> handleGameEnd(long gameId, String winnerUserId) {
		// TODO handle populating rewards, next opponent and stuff
		return Environment.callRoutine(Routines.checkRogueGameEnd(gameId, winnerUserId)).execute();
	}

	public static Future<RogueRun> returningRogueRun(Function<DSLContext, ResultQuery<RogueRunRecord>> handler) {
		return Environment.withExecutor(executor -> executor.findOneRow(handler).map(row -> row == null ? null : RowMappers.getRogueRunMapper().apply(row)));
	}

	public static Future<RogueChoice> returningRogueChoice(Function<DSLContext, ResultQuery<RogueChoiceRecord>> handler) {
		return Environment.withExecutor(executor -> executor.findOneRow(handler).map(row -> row == null ? null : rogueChoiceMapper().apply(row)));
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

	public static Future<RogueRun> updateRogueRun(long rogueId, Function<UpdateSetFirstStep<RogueRunRecord>, UpdateSetMoreStep<RogueRunRecord>> handler) {
		return returningRogueRun(dsl -> handler.apply(dsl.update(ROGUE_RUN)).where(ROGUE_RUN.ID.eq(rogueId)).returning());
	}

	public static Function<Row, RogueChoice> rogueChoiceMapper() {
		return row -> RowMappers.getRogueChoiceMapper().apply(row).setCards(row.getArrayOfStrings("cards"));
	}

}
