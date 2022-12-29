package net.demilich.metastone.game.spells.desc.source;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.Lists;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardArrayList;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.cards.desc.Desc;
import net.demilich.metastone.game.cards.desc.HasDesc;
import net.demilich.metastone.game.cards.desc.HasDescSerializer;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.TargetPlayer;

import java.io.Serializable;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;

@JsonSerialize(using = HasDescSerializer.class)
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
				case PLAYER_1:
					providingPlayer = context.getPlayer1();
					break;
				case PLAYER_2:
					providingPlayer = context.getPlayer2();
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

	/**
	 * Overridden by card source implementations to return a list of cards that usually get filtered in an {@link
	 * net.demilich.metastone.game.spells.desc.filter.EntityFilter}.
	 * <p>
	 * See the implementations for examples of how, e.g. the graveyard is turned into a {@link CardList} instance by
	 * iterating through all the actors in the graveyard and retrieving their {@link Entity#getSourceCard()}.
	 *
	 * @param context The game context
	 * @param source  The entity that is the origin of this matching operation
	 * @param player  The casting player
	 * @return A list of cards pre-filter.
	 */
	protected abstract CardList match(GameContext context, Entity source, Player player);

	/**
	 * Provides cards performantly, on demand, without having to iterate through the entire card catalogue for many kinds
	 * of sources.
	 * <p>
	 * The implementations are responsible for providing an efficient answer.
	 *
	 * @param context
	 * @param source
	 * @param player
	 * @param limit
	 * @param shuffled
	 * @return
	 */
	protected Stream<Card> match(GameContext context, Entity source, Player player, Predicate<Card> filter, int limit, boolean shuffled) {
		var match = match(context, source, player);
		if (shuffled) {
			match.shuffle(context.getLogic().getRandom());
		}
		return match.stream().filter(filter).limit(limit);
	}

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

