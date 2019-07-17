package com.hiddenswitch.spellsource.draft;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import net.demilich.metastone.game.entities.heroes.HeroClass;

import java.util.List;

/**
 * Created by bberman on 12/14/16.
 */
public class NullDraftBehaviour implements DraftBehaviour {
	@Override
	public void chooseHeroAsync(List<String> classes, Handler<AsyncResult<String>> result) {
	}

	@Override
	public void chooseCardAsync(List<String> cards, Handler<AsyncResult<Integer>> selectedCardIndex) {
	}

	@Override
	public void notifyDraftState(PublicDraftState state) {
	}

	@Override
	public void notifyDraftStateAsync(PublicDraftState state, Handler<AsyncResult<Void>> acknowledged) {
	}
}
