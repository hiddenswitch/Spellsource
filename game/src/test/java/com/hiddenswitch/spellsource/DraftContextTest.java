package com.hiddenswitch.spellsource;

import co.paralleluniverse.fibers.SuspendExecution;
import com.hiddenswitch.spellsource.draft.*;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.CardParseException;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

/**
 * Created by bberman on 12/14/16.
 */
public class DraftContextTest {
	@Test
	public void testDraftComplete() throws SuspendExecution, CardParseException, IOException, URISyntaxException {
		CardCatalogue.loadCardsFromPackage();
		DraftContext context = new DraftContext()
				.withBehaviour(new DraftBehaviour() {
					@Override
					public void chooseHeroAsync(List<HeroClass> classes, Handler<AsyncResult<HeroClass>> result) {
						result.handle(Future.succeededFuture(classes.get(0)));
					}

					@Override
					public void chooseCardAsync(List<String> cards, Handler<AsyncResult<Integer>> selectedCardIndex) {
						selectedCardIndex.handle(Future.succeededFuture(0));
					}

					@Override
					public void notifyDraftState(PublicDraftState state) {
						return;
					}

					@Override
					public void notifyDraftStateAsync(PublicDraftState state, Handler<AsyncResult<Void>> acknowledged) {
						acknowledged.handle(Future.succeededFuture());
					}
				});
		context.accept(then -> {
		});
		Assert.assertEquals(context.getPublicState().getSelectedCards().size(), DraftLogic.DRAFTS);
		Assert.assertEquals(context.getPublicState().getStatus(), DraftStatus.COMPLETE);
	}
}