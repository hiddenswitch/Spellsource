package net.demilich.metastone.game.spells.desc.source;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.cards.CardArrayList;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.TargetPlayer;

import java.io.Serializable;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

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

	@Suspendable
	public CardList getCards(GameContext context, Entity source, Player player) {
		TargetPlayer targetPlayer = (TargetPlayer) desc.get(SourceArg.TARGET_PLAYER);
		if (targetPlayer == null) {
			targetPlayer = TargetPlayer.SELF;
		}

		CardList cards = new CardArrayList();
		if (targetPlayer == TargetPlayer.BOTH) {
			for (Player selectedPlayer : context.getPlayers()) {
				cards.addAll(this.match(context, source, selectedPlayer));
			}
		} else {
			Player providingPlayer;
			switch (targetPlayer) {
				case ACTIVE:
					providingPlayer = context.getActivePlayer();
					break;
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
			cards.addAll(this.match(context, source, providingPlayer));
		}

		if (desc.getBool(SourceArg.DISTINCT)) {
			cards = new CardArrayList(cards
					.stream()
					.collect(toMap(Card::getCardId, Function.identity(), (p, q) -> p))
					.values());
		}

		return cards;
	}

	@Suspendable
	protected abstract CardList match(GameContext context, Entity source, Player player);

	public TargetPlayer getTargetPlayer() {
		return (TargetPlayer) desc.getOrDefault(SourceArg.TARGET_PLAYER, TargetPlayer.SELF);
	}
}

