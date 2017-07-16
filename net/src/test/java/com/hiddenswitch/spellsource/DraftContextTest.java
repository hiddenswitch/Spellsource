package com.hiddenswitch.spellsource;

import ch.qos.logback.classic.Level;
import com.hiddenswitch.proto3.draft.DraftBehaviour;
import com.hiddenswitch.proto3.draft.DraftContext;
import com.hiddenswitch.proto3.draft.PublicDraftState;
import com.hiddenswitch.spellsource.client.models.MatchmakingDeck;
import com.hiddenswitch.spellsource.util.AbstractMatchmakingTest;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.ext.unit.TestContext;
import net.demilich.metastone.game.decks.Deck;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.utils.Tuple;
import org.junit.Test;

import java.util.List;

/**
 * Created by bberman on 12/16/16.
 */
public class DraftContextTest extends AbstractMatchmakingTest {
    @Test(timeout = 80000L)
    public void testDraftAndJoin(TestContext context) {
        setLoggingLevel(Level.DEBUG);
        wrapSync(context, this::createTwoPlayersAndMatchmake);
    }

    @Override
    protected Tuple<MatchmakingDeck, Deck> createDeckForMatchmaking(int playerId) {
        setLoggingLevel(Level.ERROR);
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
        context.accept(done -> {
        });
        return getTuple(context.getPublicState().createDeck());
    }
}
