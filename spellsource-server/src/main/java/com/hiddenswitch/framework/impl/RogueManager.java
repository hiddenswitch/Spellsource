package com.hiddenswitch.framework.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.hiddenswitch.framework.Environment;
import com.hiddenswitch.framework.schema.spellsource.Tables;
import com.hiddenswitch.framework.schema.spellsource.enums.RogueRunState;
import com.hiddenswitch.framework.schema.spellsource.tables.mappers.RowMappers;
import com.hiddenswitch.framework.schema.spellsource.tables.pojos.RogueRun;
import com.hiddenswitch.framework.schema.spellsource.tables.records.RogueRunRecord;
import com.hiddenswitch.spellsource.rpc.Spellsource;
import io.github.jklingsporn.vertx.jooq.shared.internal.QueryResult;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.impl.ContextInternal;
import io.vertx.core.json.jackson.DatabindCodec;
import io.vertx.pgclient.pubsub.PgSubscriber;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import org.jooq.UpdateReturningStep;
import org.jooq.UpdateSetFirstStep;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.hiddenswitch.framework.schema.spellsource.Tables.ROGUE_RUN;
import static io.vertx.await.Async.await;

public class RogueManager {

	private final CardCatalogue cardCatalogue;

	private static final String SPELLSOURCE_ROGUE_UPDATES_CHANNEL_FROM_DDL = "spellsource_rogue_updates_v0";
	private WeakVertxMap<PgSubscriber> subscribers = new WeakVertxMap<>(vertx -> PgSubscriber.subscriber(vertx, Environment.pgArgs().connectionOptions()));
	private PgSubscriber subscriber;

	public RogueManager(CardCatalogue cardCatalogue) {
		this.cardCatalogue = cardCatalogue;
	}

	/**
	 * @see SqlCachedCardCatalogue#subscribe()
	 */
	public Future<Void> subscribe() {
		if (subscriber != null) {
			return Future.succeededFuture();
		}

		this.subscriber = subscribers.get();
		var context = (ContextInternal) Vertx.currentContext();
		subscriber.channel(SPELLSOURCE_ROGUE_UPDATES_CHANNEL_FROM_DDL).handler(payload -> context.runOnContext(_ -> {
			try {
				var update = DatabindCodec.mapper().readValue(payload, SpellsourceRogueUpdate.class);
				var rogueId = update.id;

				var rogueRun = await(getRogueRun(rogueId));

				await(update.payload().handle(rogueRun, this));
			} catch (JsonProcessingException e) {
				throw new RuntimeException(e);
			}
		}));

		return subscriber.connect();
	}

	public Future<RogueRun> getRogueRun(long rogueId) {
		return Environment.withExecutor(executor -> executor.findOneRow(dsl -> dsl.selectFrom(ROGUE_RUN).where(ROGUE_RUN.ID.eq(rogueId))).compose(row -> row == null ?
				Future.failedFuture("No rogue run found") : Future.succeededFuture(RowMappers.getRogueRunMapper().apply(row))));
	}

	public Future<RogueRun> updateRogueRun(long rogueId, Function<UpdateSetFirstStep<RogueRunRecord>, UpdateReturningStep<RogueRunRecord>> handler) {
		return Environment.withExecutor(executor -> executor.query(dsl -> handler.apply(dsl.update(ROGUE_RUN.where(ROGUE_RUN.ID.eq(rogueId)))).returning()).compose(QueryResult::unwrap));
	}

	// the field names are the values within spellsource.rogue_payload_type in sql
	public record SpellsourceRogueUpdate(long id, RogueRunStarted start, RogueChoice choice, RogueMatchStart matchStart, RogueMatchEnd matchEnd, RogueResign resign) {
		private Stream<RoguePayload> payloads() {
			return Stream.of(start, choice, matchStart, matchEnd, resign);
		}

		public RoguePayload payload() {
			return payloads().filter(Objects::nonNull).findFirst().orElseThrow();
		}
	}

	public interface RoguePayload {
		Future<?> handle(RogueRun rogueRun, RogueManager rogueManager);
	}

	public record RogueRunStarted() implements RoguePayload {
		@Override
		public Future<?> handle(RogueRun rogueRun, RogueManager rogueManager) {
			// TODO real populate rogue deck

			var format = rogueManager.cardCatalogue.getFormat("Rogue");
			var testCards = rogueManager.cardCatalogue.query(format).filtered(card -> card.hasHeroClass(HeroClass.TEST) && card.isCollectible());

			await(Future.all(testCards.stream().map(card -> Environment.withDslContext(dsl ->
					dsl.insertInto(Tables.CARDS_IN_DECK).set(Tables.CARDS_IN_DECK.newRecord().setDeckId(rogueRun.getDeck()).setCardId(card.getCardId()))
			)).toList()));

			// TODO set opponent deck

			// TODO change state to pre match OR give initial set of choices

			return Future.succeededFuture();
		}
	}

	public record RogueChoice(int index) implements RoguePayload {
		@Override
		public Future<?> handle(RogueRun rogueRun, RogueManager rogueManager) {
			if (index < 0 || index > rogueRun.getChoices().length) {
				return Future.failedFuture("Choice index invalid");
			}

			var choice = rogueRun.getChoices()[index];

			var newChoices = new String[0];

			try {
				var card = rogueManager.cardCatalogue.getCardById(choice);

				if (card.getCardType() == Spellsource.CardTypeMessage.CardType.ROGUE_CHOICE) {
					// TODO do other rogue choice stuff
				} else {
					await(Environment.withDslContext(dsl -> dsl.insertInto(Tables.CARDS_IN_DECK).set(Tables.CARDS_IN_DECK.newRecord().setDeckId(rogueRun.getDeck()).setCardId(choice))));
				}
			} catch (Exception e) {
				return Future.failedFuture(e);
			}

			return rogueManager.updateRogueRun(rogueRun.getId(), r -> r
					.set(ROGUE_RUN.CHOICES, newChoices)
					.set(ROGUE_RUN.STATE, newChoices.length > 0 ? RogueRunState.CHOICE : RogueRunState.PRE_MATCH)
			);
		}
	}

	public record RogueMatchStart(long gameId) implements RoguePayload {
		@Override
		public Future<?> handle(RogueRun rogueRun, RogueManager rogueManager) {
			return Future.succeededFuture();
		}
	}

	public record RogueMatchEnd(boolean won) implements RoguePayload {
		@Override
		public Future<?> handle(RogueRun rogueRun, RogueManager rogueManager) {
			if (!won) {
				// TODO implement a lives mechanic

				return rogueManager.updateRogueRun(rogueRun.getId(), r -> r
						.set(ROGUE_RUN.STATE, RogueRunState.FINISHED)
						.set(ROGUE_RUN.ENDED_AT, OffsetDateTime.now())
				);
			}

			// TODO determine new choices / next opponent deck

			return rogueManager.updateRogueRun(rogueRun.getId(), r -> r
					.set(ROGUE_RUN.STATE, RogueRunState.CHOICE)
			);
		}
	}

	public record RogueResign() implements RoguePayload {
		@Override
		public Future<?> handle(RogueRun rogueRun, RogueManager rogueManager) {
			return rogueManager.updateRogueRun(rogueRun.getId(), r -> r
					.set(ROGUE_RUN.STATE, RogueRunState.FINISHED)
					.set(ROGUE_RUN.ENDED_AT, OffsetDateTime.now())
			);
		}
	}

}
