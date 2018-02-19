package net.demilich.metastone.game.spells.desc.source;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.CardList;

import java.util.Map;

public class HandSource extends CardSource {

	public HandSource(SourceDesc desc) {
		super(desc);
	}

	@Override
	protected CardList match(GameContext context, Player player) {
		return player.getHand();
	}

	public static HandSource create() {
		Map<SourceArg, Object> args = SourceDesc.build(HandSource.class);
		return new HandSource(new SourceDesc(args));
	}
}
