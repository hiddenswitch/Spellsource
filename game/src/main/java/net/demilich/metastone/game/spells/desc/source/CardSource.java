package net.demilich.metastone.game.spells.desc.source;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.cards.CardArrayList;
import net.demilich.metastone.game.spells.TargetPlayer;

import java.io.Serializable;
import java.util.Map;

public abstract class CardSource implements Serializable {
	protected final SourceDesc desc;

	public CardSource(SourceDesc desc) {
		this.desc = desc;
	}

	public Object getArg(SourceArg arg) {
		return desc.get(arg);
	}

	public boolean hasArg(SourceArg arg) {
		return desc.containsKey(arg);
	}

	public CardList getCards(GameContext context, Player player) {
		TargetPlayer targetPlayer = (TargetPlayer) desc.get(SourceArg.TARGET_PLAYER);
		if (targetPlayer == null) {
			targetPlayer = TargetPlayer.SELF;
		}
		Player providingPlayer = null;
		switch (targetPlayer) {
			case ACTIVE:
				providingPlayer = context.getActivePlayer();
				break;
			case BOTH:
				CardList cards = new CardArrayList();
				for (Player selectedPlayer : context.getPlayers()) {
					cards.addAll(this.match(context, selectedPlayer));
				}
				return cards;
			case INACTIVE:
				providingPlayer = context.getOpponent(context.getActivePlayer());
				break;
			case OPPONENT:
				providingPlayer = context.getOpponent(player);
				break;
			case OWNER:
			case SELF:
			default:
				providingPlayer = player;
				break;
		}
		return this.match(context, providingPlayer);
	}

	protected abstract CardList match(GameContext context, Player player);

	public TargetPlayer getTargetPlayer() {
		return (TargetPlayer) desc.getOrDefault(SourceArg.TARGET_PLAYER, TargetPlayer.SELF);
	}
}
