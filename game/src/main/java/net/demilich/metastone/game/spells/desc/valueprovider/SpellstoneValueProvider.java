package net.demilich.metastone.game.spells.desc.valueprovider;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.trigger.Enchantment;

public class SpellstoneValueProvider extends ValueProvider {
    public SpellstoneValueProvider(ValueProviderDesc desc) {
        super(desc);
    }

    @Override
    protected int provideValue(GameContext context, Player player, Entity target, Entity host) {
        Enchantment enchantment = (Enchantment) context.getTriggersAssociatedWith(target.getReference()).get(0);
        return enchantment.getCountUntilCast() - enchantment.getFires();

    }
}
