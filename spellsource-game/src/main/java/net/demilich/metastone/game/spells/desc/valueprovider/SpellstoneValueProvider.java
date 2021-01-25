package net.demilich.metastone.game.spells.desc.valueprovider;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.trigger.Enchantment;
import net.demilich.metastone.game.spells.trigger.Trigger;

import java.util.List;

public class SpellstoneValueProvider extends ValueProvider {
	public SpellstoneValueProvider(ValueProviderDesc desc) {
        super(desc);
    }

    @Override
    protected int provideValue(GameContext context, Player player, Entity target, Entity host) {
        if (target == null) {
            return 0;
        }
	    List<Trigger> triggers = context.getLogic().getActiveTriggers(target.getReference());
        if (triggers.isEmpty()) {
            return 0;
        }
        if (!(triggers.get(0) instanceof Enchantment)) {
            return 0;
        }
        Enchantment enchantment = (Enchantment) triggers.get(0);
        return enchantment.getCountUntilCast() - enchantment.getFires();

    }
}
