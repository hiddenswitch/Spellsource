package net.demilich.metastone.game.spells;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.trigger.EnchantmentDesc;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;
import net.demilich.metastone.game.spells.trigger.WillEndSequenceTrigger;

public class CastAfterSequenceSpell extends Spell {
    @Override
    protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
        SpellDesc spell = desc.getSpell();

        EnchantmentDesc enchantmentDesc = new EnchantmentDesc();
        enchantmentDesc.spell = spell;
        enchantmentDesc.maxFires = 1;
        enchantmentDesc.eventTrigger = new EventTriggerDesc(WillEndSequenceTrigger.class);
        context.getLogic().addGameEventListener(player, enchantmentDesc.create(), player);
    }
}
