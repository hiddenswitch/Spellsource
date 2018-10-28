package net.demilich.metastone.game.spells.desc.valueprovider;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;

public class ManaCostValueProvider extends ValueProvider {
    public ManaCostValueProvider(ValueProviderDesc desc) {
        super(desc);
    }

    @Override
    protected int provideValue(GameContext context, Player player, Entity target, Entity host) {
        if (target instanceof Card) {
            return context.getLogic().getModifiedManaCost(player, (Card) target);
        } else {
            return context.getLogic().getModifiedManaCost(player, target.getSourceCard());
        }
    }
}
