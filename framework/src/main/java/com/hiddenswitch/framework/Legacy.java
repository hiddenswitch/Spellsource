package com.hiddenswitch.framework;

import com.google.protobuf.Empty;
import com.hiddenswitch.framework.schema.spellsource.tables.daos.DecksDao;
import com.hiddenswitch.framework.schema.spellsource.tables.pojos.Decks;
import com.hiddenswitch.spellsource.rpc.*;
import io.grpc.ServerServiceDefinition;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.sqlclient.Row;
import org.jooq.JSONB;
import org.jooq.JoinType;

import java.util.List;

import static com.hiddenswitch.framework.schema.spellsource.tables.Cards.CARDS;
import static com.hiddenswitch.framework.schema.spellsource.tables.CardsInDeck.CARDS_IN_DECK;
import static java.util.stream.Collectors.toList;

/**
 * The legacy services for Spellsource, to rapidly transition the game into a new backend.
 */
public class Legacy {

	public static Entity.Builder toCardEntity(String cardId, JSONB cardScript) {
		var entity = Entity.newBuilder();
		return entity;
	}

	public static Entity.Builder toCardEntity(Row cardRow) {
		return toCardEntity(cardRow.getString(CARDS.URI.getName()), (JSONB) cardRow.getValue(CARDS.CARD_SCRIPT.getName()));
	}


	public static InventoryCollection.Builder fromDeckRecord(Decks deck) {
		return null;
	}

	public Future<ServerServiceDefinition> services() {
		var decks = new DecksDao(Environment.jooq(), Environment.pool());
		return Future.succeededFuture(new HiddenSwitchSpellsourceAPIServiceGrpc.HiddenSwitchSpellsourceAPIServiceVertxImplBase() {
			@Override
			public void decksDelete(DecksDeleteRequest request, Promise<Empty> response) {
				decks.deleteById(request.getDeckId())
						.map(Empty.getDefaultInstance())
						.onComplete(response);
			}

			@Override
			public void decksGet(DecksGetRequest request, Promise<DecksGetResponse> response) {
				var decks = new DecksDao(Environment.jooq(), Environment.pool());
				var queryExecutor = Environment.queryExecutor();

				var cards = queryExecutor.findManyRow(dsl -> dsl.select(CARDS_IN_DECK.ID, CARDS_IN_DECK.DECK_ID, CARDS_IN_DECK.CARD_ID, CARDS.URI, CARDS.CARD_SCRIPT)
						.from(CARDS_IN_DECK)
						.join(CARDS, JoinType.JOIN)
						.where(CARDS_IN_DECK.DECK_ID.eq(request.getDeckId())))
						.map(rows -> rows.stream().map(Legacy::toCardEntity).collect(toList()));

				var inventoryRecord = decks.findOneById(request.getDeckId())
						.map(Legacy::fromDeckRecord);

				CompositeFuture.join(cards, inventoryRecord)
						.compose(res -> {
							var entities = res.<List<Entity.Builder>>resultAt(0);
							var inventory = res.<InventoryCollection.Builder>resultAt(1);
							for (var entity : entities) {
								inventory.addInventory(CardRecord.newBuilder()
										.setEntity(entity)
										.build());
							}
							return Future.succeededFuture(DecksGetResponse.newBuilder()
									.build());
						})
						.onComplete(response);
			}

			@Override
			public void decksGetAll(Empty request, Promise<DecksGetAllResponse> response) {
				super.decksGetAll(request, response);
			}

			@Override
			public void decksPut(DecksPutRequest request, Promise<DecksPutResponse> response) {
				super.decksPut(request, response);
			}

			@Override
			public void decksUpdate(DecksUpdateRequest request, Promise<DecksGetResponse> response) {
				super.decksUpdate(request, response);
			}

			@Override
			public void getCards(GetCardsRequest request, Promise<GetCardsResponse> response) {
				super.getCards(request, response);
			}
		})
				.compose(Accounts::requiresAuthorization);
	}

}
