package net.demilich.metastone.game.spells.desc.source;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.Entity;

import java.io.Serializable;

/**
 * Returns cards from the player's deck. Does <b>not</b> return copies or base cards, but references to the actual cards
 * themselves.
 */
public class DeckSource extends CardSource implements Serializable {

	public DeckSource(CardSourceDesc desc) {
		super(desc);
	}

	public static CardSource create() {
		return new CardSourceDesc(DeckSource.class).create();
	}

	@Override
	protected CardList match(GameContext context, Entity source, Player player) {
		return player.getDeck();
	}
}

