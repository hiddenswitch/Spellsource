package com.hiddenswitch.spellsource.draft;

import com.hiddenswitch.spellsource.util.Result;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import net.demilich.metastone.game.behaviour.human.DraftSelectionOptions;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.heroes.HeroClass;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by bberman on 12/14/16.
 */
public class HumanDraftBehaviour implements DraftBehaviour {
	private List<Card> cards;
	private Handler<AsyncResult<Integer>> chooseCardHandler;
	private Handler<AsyncResult<HeroClass>> chooseHeroHandler;

	@Override
	public void chooseHeroAsync(List<HeroClass> classes, Handler<AsyncResult<HeroClass>> result) {
		// Convert to hero cards
		this.cards = classes.stream().map(HeroClass::getHeroCard).collect(Collectors.toList());
		this.chooseHeroHandler = result;
		DraftSelectionOptions options = new DraftSelectionOptions(this, this.cards);
	}

	@Override
	public void chooseCardAsync(List<String> cards, Handler<AsyncResult<Integer>> selectedCardIndex) {
		this.cards = cards.stream().map(CardCatalogue::getCardById).collect(Collectors.toList());
		this.chooseCardHandler = selectedCardIndex;
		DraftSelectionOptions options = new DraftSelectionOptions(this, this.cards);
	}

	@Override
	public void notifyDraftState(PublicDraftState state) {
	}

	@Override
	public void notifyDraftStateAsync(PublicDraftState state, Handler<AsyncResult<Void>> acknowledged) {
	}

}
