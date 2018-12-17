package net.demilich.metastone.game.spells.desc.source;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.Entity;

import java.util.Map;

public class HandSource extends CardSource {

	private static final long serialVersionUID = -3099619069960657609L;

	public HandSource(CardSourceDesc desc) {
		super(desc);
	}

	@Override
	protected CardList match(GameContext context, Entity source, Player player) {
		return player.getHand();
	}

	public static HandSource create() {
		Map<CardSourceArg, Object> args = new CardSourceDesc(HandSource.class);
		return new HandSource(new CardSourceDesc(args));
	}
}
