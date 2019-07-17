package com.hiddenswitch.spellsource.draft;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import net.demilich.metastone.game.entities.heroes.HeroClass;

import java.util.List;

/**
 * Created by bberman on 12/14/16.
 */
public interface DraftBehaviour {
	void chooseHeroAsync(List<String> classes, Handler<AsyncResult<String>> result);

	void chooseCardAsync(List<String> cards, Handler<AsyncResult<Integer>> selectedCardIndex);

	void notifyDraftState(PublicDraftState state);

	void notifyDraftStateAsync(PublicDraftState state, Handler<AsyncResult<Void>> acknowledged);
}
