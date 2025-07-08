package com.hiddenswitch.framework.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.hiddenswitch.framework.Environment;
import com.hiddenswitch.framework.schema.spellsource.Tables;
import com.hiddenswitch.framework.schema.spellsource.enums.RogueRunState;
import com.hiddenswitch.framework.schema.spellsource.tables.mappers.RowMappers;
import com.hiddenswitch.framework.schema.spellsource.tables.pojos.RogueRun;
import com.hiddenswitch.framework.schema.spellsource.tables.records.RogueRunRecord;
import com.hiddenswitch.spellsource.rpc.Spellsource;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.impl.ContextInternal;
import io.vertx.core.json.jackson.DatabindCodec;
import io.vertx.pgclient.pubsub.PgSubscriber;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import org.jooq.DSLContext;
import org.jooq.ResultQuery;
import org.jooq.UpdateSetFirstStep;
import org.jooq.UpdateSetMoreStep;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.hiddenswitch.framework.schema.spellsource.Tables.ROGUE_RUN;
import static io.vertx.await.Async.await;

public class RogueManager {

	public static final SqlCachedCardCatalogue cardCatalogue = new SqlCachedCardCatalogue();

	private static final String SPELLSOURCE_ROGUE_UPDATES_CHANNEL_FROM_DDL = "spellsource_rogue_updates_v0";
	private static WeakVertxMap<PgSubscriber> subscribers = new WeakVertxMap<>(vertx -> PgSubscriber.subscriber(vertx, Environment.pgArgs().connectionOptions()));
	private static PgSubscriber subscriber;

	private static final AtomicBoolean initialized = new AtomicBoolean(false);

	/**
	 * @see SqlCachedCardCatalogue#subscribe()
	 */
	public static Future<Void> initialize() {
		if (initialized.getAndSet(true)) {
			return Future.succeededFuture();
		}

		cardCatalogue.invalidateAllAndRefresh();

		subscriber = subscribers.get();
		var context = (ContextInternal) Vertx.currentContext();
		subscriber.channel(SPELLSOURCE_ROGUE_UPDATES_CHANNEL_FROM_DDL).handler(payload -> context.runOnContext(_ -> {
			try {
				var update = DatabindCodec.mapper().readValue(payload, SpellsourceRogueUpdate.class);
				var rogueId = update.id;
				System.out.println("handling a rogue update for " + rogueId);

				var rogueRun = await(getRogueRun(rogueId));

				await(update.payload().handle(rogueRun));
			} catch (JsonProcessingException e) {
				throw new RuntimeException(e);
			}
		}));

		return subscriber.connect();
	}

	public static Future<RogueRun> returningRogueRun(Function<DSLContext, ResultQuery<RogueRunRecord>> handler) {
		return Environment.withExecutor(executor -> executor.findOneRow(handler)
				.compose(row -> row == null ? Future.failedFuture("No rogue run found") : Future.succeededFuture(RowMappers.getRogueRunMapper().apply(row))));
	}

	public static Future<RogueRun> getRogueRun(long rogueId) {
		return returningRogueRun(dsl -> dsl.selectFrom(ROGUE_RUN).where(ROGUE_RUN.ID.eq(rogueId)));
	}

	public static Future<RogueRun> updateRogueRun(long rogueId, Function<UpdateSetFirstStep<RogueRunRecord>, UpdateSetMoreStep<RogueRunRecord>> handler) {
		return returningRogueRun(dsl -> handler.apply(dsl.update(ROGUE_RUN)).where(ROGUE_RUN.ID.eq(rogueId)).returning());
	}

	// the field names are the values within spellsource.rogue_payload_type in sql
	public record SpellsourceRogueUpdate(long id, RogueRunStarted start, RogueChoice choice, RogueMatchStart matchStart, RogueMatchEnd matchEnd) {
		private Stream<RoguePayload> payloads() {
			return Stream.of(start, choice, matchStart, matchEnd);
		}

		public RoguePayload payload() {
			return payloads().filter(Objects::nonNull).findFirst().orElseThrow();
		}
	}

	public interface RoguePayload {
		Future<?> handle(RogueRun rogueRun);
	}

	public record RogueRunStarted() implements RoguePayload {
		@Override
		public Future<?> handle(RogueRun rogueRun) {
			var format = cardCatalogue.getFormat("Rogue");
			var testCards = cardCatalogue.query(format).stream().filter(card -> card.hasHeroClass(HeroClass.ANY) && card.isCollectible()).limit(10);

			await(Future.all(testCards.map(card -> Environment.withDslContext(dsl ->
					dsl.insertInto(Tables.CARDS_IN_DECK).set(Tables.CARDS_IN_DECK.newRecord().setDeckId(rogueRun.getDeck()).setCardId(card.getCardId()))
			)).toList()));

			// TODO set opponent deck

			// TODO change state to pre match OR give initial set of choices

			await(updateRogueRun(rogueRun.getId(), r -> r.set(ROGUE_RUN.STATE, RogueRunState.PRE_MATCH)));

			return Future.succeededFuture();
		}
	}

	public record RogueChoice(int index) implements RoguePayload {
		@Override
		public Future<?> handle(RogueRun rogueRun) {
			if (index < 0 || index > rogueRun.getChoices().length) {
				return Future.failedFuture("Choice index invalid");
			}

			var choice = rogueRun.getChoices()[index];

			var newChoices = new String[0];

			try {
				var card = cardCatalogue.getCardById(choice);

				if (card.getCardType() == Spellsource.CardTypeMessage.CardType.ROGUE_CHOICE) {
					// TODO do other rogue choice stuff
				} else {
					await(Environment.withDslContext(dsl -> dsl.insertInto(Tables.CARDS_IN_DECK).set(Tables.CARDS_IN_DECK.newRecord().setDeckId(rogueRun.getDeck()).setCardId(choice))));
				}
			} catch (Exception e) {
				return Future.failedFuture(e);
			}

			return updateRogueRun(rogueRun.getId(), r -> r
					.set(ROGUE_RUN.CHOICES, newChoices)
					.set(ROGUE_RUN.STATE, newChoices.length > 0 ? RogueRunState.CHOICE : RogueRunState.PRE_MATCH)
			);
		}
	}

	public record RogueMatchStart(long gameId) implements RoguePayload {
		@Override
		public Future<?> handle(RogueRun rogueRun) {
			return Future.succeededFuture();
		}
	}

	public record RogueMatchEnd(boolean won) implements RoguePayload {
		@Override
		public Future<?> handle(RogueRun rogueRun) {
			// TODO determine new choices / next opponent deck

			return Future.succeededFuture();
		}
	}

}
