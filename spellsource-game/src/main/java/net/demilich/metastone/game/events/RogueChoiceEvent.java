package net.demilich.metastone.game.events;

import com.hiddenswitch.spellsource.rpc.Spellsource.GameEventTypeMessage.GameEventType;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import org.jetbrains.annotations.NotNull;

public class RogueChoiceEvent extends CardEvent {

	public RogueChoiceEvent(@NotNull GameContext context, Player player, Card card) {
		super(GameEventType.ROGUE_CHOICE, false, context, player, player.getHero(), player.getHero(), card);
	}
}