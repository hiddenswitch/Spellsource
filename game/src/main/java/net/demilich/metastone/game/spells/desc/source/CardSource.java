package net.demilich.metastone.game.spells.desc.source;

import com.github.fromage.quasi.fibers.Suspendable;
import com.google.common.collect.Lists;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardArrayList;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.cards.desc.Desc;
import net.demilich.metastone.game.cards.desc.HasDesc;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.TargetPlayer;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;

public abstract class CardSource implements Serializable, HasDesc<CardSourceDesc> {
	private CardSourceDesc desc;

	public CardSource(CardSourceDesc desc) {
		this.setDesc(desc);
	}

	public Object getArg(CardSourceArg arg) {
		return getDesc().get(arg);
	}

	public boolean hasArg(CardSourceArg arg) {
		return getDesc().containsKey(arg);
	}

	@Suspendable
	public CardList getCards(GameContext context, Entity source, Player player) {
		TargetPlayer targetPlayer = (TargetPlayer) getDesc().get(CardSourceArg.TARGET_PLAYER);
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
			cards = this.match(context, source, providingPlayer);
		}

		if (getDesc().getBool(CardSourceArg.DISTINCT)) {
			cards = new CardArrayList(cards
					.stream()
					.collect(toMap(Card::getCardId, Function.identity(), (p, q) -> p))
					.values());
		}

		if (getDesc().getBool(CardSourceArg.INVERT)) {
			cards = new CardArrayList(Lists.reverse(cards.toList()));
		}

		return cards;
	}

	@Suspendable
	protected abstract CardList match(GameContext context, Entity source, Player player);

	public TargetPlayer getTargetPlayer() {
		return (TargetPlayer) getDesc().getOrDefault(CardSourceArg.TARGET_PLAYER, TargetPlayer.SELF);
	}

	@Override
	public CardSourceDesc getDesc() {
		return desc;
	}

	@Override
	public void setDesc(Desc<?, ?> desc) {
		this.desc = (CardSourceDesc) desc;
	}
}

