package net.demilich.metastone.game.spells.desc.filter;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardType;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.targeting.TargetSelection;

public class TargetingFilter extends EntityFilter {
    public TargetingFilter(EntityFilterDesc desc) {
        super(desc);
    }

    @Override
    protected boolean test(GameContext context, Player player, Entity entity, Entity host) {
        Card card = entity.getSourceCard();

        CardType cardType = (CardType) getDesc().get(EntityFilterArg.CARD_TYPE);
        if (cardType != null && !card.getCardType().isCardType(cardType)) {
            return false;
        }

        if (card.getTargetSelection().equals(TargetSelection.NONE)) {
            return false;
        }
        return true;
    }
}
