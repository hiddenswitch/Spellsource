package com.hiddenswitch.spellsource;

import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.spellsource.client.models.CardRecord;
import com.hiddenswitch.spellsource.concurrent.SuspendableMap;
import com.hiddenswitch.spellsource.models.*;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.util.GlobalTracer;
import io.vertx.core.Vertx;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.CardCatalogueRecord;
import net.demilich.metastone.game.cards.CardSet;
import net.demilich.metastone.game.cards.CardType;
import net.demilich.metastone.game.cards.desc.CardDesc;
import net.demilich.metastone.game.decks.DeckFormat;
import net.demilich.metastone.game.entities.heroes.HeroClass;

import java.util.*;

import static com.hiddenswitch.spellsource.util.QuickJson.json;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

/**
 * The cards service. This manages the base card definitions.
 */
public interface Cards {
	Random RANDOM = new Random();

	/**
	 * Queries the card catalogue with the specified parameters and returns the corresponding card records. Useful for
	 * filtering through the card catalogue.
	 *
	 * @param request A variety of different filtering parameters for querying the card catalogue.
	 * @return Records which match the filters in the request.
	 */
	@Suspendable
	static QueryCardsResponse query(QueryCardsRequest request) {
		Tracer tracer = GlobalTracer.get();
		Span span = tracer.buildSpan("Cards/query")
				.withTag("request", json(request).toString())
				.start();
		Scope scope = tracer.activateSpan(span);
		try {
			// For now, just use the CardCatalogue
			CardCatalogue.loadCardsFromPackage();

			final QueryCardsResponse response;

			if (request.isBatchRequest()) {
				response = new QueryCardsResponse()
						.withRecords(new ArrayList<>());

				for (QueryCardsRequest request1 : request.getRequests()) {
					response.append(query(request1));
				}
			} else if (request.getCardIds() != null) {
				response = new QueryCardsResponse()
						.withRecords(request.getCardIds().stream().map(CardCatalogue.getRecords()::get).collect(toList()));
			} else {
				final Set<String> sets = new HashSet<>(Arrays.asList(request.getSets()));

				List<CardCatalogueRecord> results = CardCatalogue.getRecords().values().stream().filter(r -> {
					boolean passes = true;

					final CardDesc desc = r.getDesc();

					passes &= desc.isCollectible();
					passes &= sets.contains(desc.getSet());

					if (request.getRarity() != null) {
						passes &= desc.getRarity() != null && desc.getRarity().isRarity(request.getRarity());
					}

					return passes;
				}).collect(toList());

				int count = results.size();

				if (request.isRandomCountRequest()) {
					Collections.shuffle(results, getRandom());
					count = Math.min(request.getRandomCount(), count);
				}

				List<CardCatalogueRecord> cards = results;
				if (count != 0) {
					cards = new ArrayList<>(cards.subList(0, count));
				}

				response = new QueryCardsResponse()
						.withRecords(cards);
			}
			return response;
		} catch (RuntimeException runtimeException) {
			Tracing.error(runtimeException, span, true);
			throw runtimeException;
		} finally {
			span.finish();
			scope.close();
		}
	}

	static Random getRandom() {
		return RANDOM;
	}

	/**
	 * Retrieves a freshly computed list containing all the collectible cards as client entities. Represents the current
	 * master collection of the game.
	 *
	 * @return The cards
	 */
	static List<CardRecord> getCards() {
		Tracer tracer = GlobalTracer.get();
		Span span = tracer.buildSpan("Cards/getCards")
				.start();
		Scope scope = tracer.activateSpan(span);
		try {
			GameContext workingContext = new GameContext(HeroClass.ANY, HeroClass.ANY);
			return CardCatalogue.getRecords().values()
					.stream()
					.map(CardCatalogueRecord::getDesc)
					.filter(cd -> DeckFormat.getFormat("Greater Custom").isInFormat(cd.getSet())
							&& cd.type != CardType.GROUP
							&& cd.type != CardType.HERO_POWER
							&& cd.type != CardType.ENCHANTMENT)
					.map(CardDesc::create)
					.map(card -> Games.getEntity(workingContext, card, 0))
					.map(entity -> {
						// Don't waste space storing locations on these
						entity.l(null);
						return new CardRecord()
								.id(entity.getCardId())
								.entity(entity);
					})
					.collect(toList());
		} catch (RuntimeException runtimeException) {
			Tracing.error(runtimeException, span, true);
			throw runtimeException;
		} finally {
			span.finish();
			scope.close();
		}
	}

	/**
	 * Invalidates the card cache.
	 */
	@Suspendable
	static void invalidateCardCache() {
		Tracer tracer = GlobalTracer.get();
		Span span = tracer.buildSpan("Cards/invalidateCardCache")
				.start();
		Scope scope = tracer.activateSpan(span);
		try {
			SuspendableMap<String, Object> cache = SuspendableMap.getOrCreate("Cards/cards");
			// Invalidate the cache here
			cache.put("cards-version", Vertx.currentContext().deploymentID());
			cache.put("cards-last-modified", Gateway.DATE_TIME_FORMATTER.format(new Date()));
		} catch (RuntimeException runtimeException) {
			Tracing.error(runtimeException, span, true);
			throw runtimeException;
		} finally {
			span.finish();
			scope.close();
		}
	}
}
