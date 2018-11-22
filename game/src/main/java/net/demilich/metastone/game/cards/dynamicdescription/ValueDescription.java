package net.demilich.metastone.game.cards.dynamicdescription;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;

public class ValueDescription extends DynamicDescription {
    public ValueDescription(DynamicDescriptionDesc desc) {
        super(desc);
    }

    @Override
    public String resolveFinalString(GameContext context, Player player, Card card) {
        return "" + getDesc().getValue(DynamicDescriptionArg.VALUE, context, player, card, card, 0);
    }
}
