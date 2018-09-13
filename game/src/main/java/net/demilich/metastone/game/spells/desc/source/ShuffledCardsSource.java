package net.demilich.metastone.game.spells.desc.source;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.CardArrayList;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.utils.Attribute;

public class ShuffledCardsSource extends CardSource {
    public ShuffledCardsSource(CardSourceDesc desc) {
        super(desc);
    }

    @Override
    protected CardList match(GameContext context, Entity source, Player player) {
        if (!player.hasAttribute(Attribute.LAST_SHUFFLED)) {
            return new CardArrayList();
        } else {
            return (CardList) player.getAttribute(Attribute.LAST_SHUFFLED);
        }
    }
}
