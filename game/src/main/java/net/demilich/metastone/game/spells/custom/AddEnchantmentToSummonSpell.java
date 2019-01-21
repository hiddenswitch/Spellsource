package net.demilich.metastone.game.spells.custom;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.AddEnchantmentSpell;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.SummonSpell;
import net.demilich.metastone.game.spells.TargetPlayer;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.trigger.EnchantmentDesc;

import java.util.List;

public class AddEnchantmentToSummonSpell extends AddEnchantmentSpell {

    @Override
    protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
        EnchantmentDesc enchantmentDesc = (EnchantmentDesc) desc.get(SpellArg.TRIGGER);
        if (player.getId() == target.getOwner()) {
            enchantmentDesc.spell = SummonSpell.create(TargetPlayer.SELF, target.getSourceCard());
        } else {
            enchantmentDesc.spell = SummonSpell.create(TargetPlayer.OPPONENT, target.getSourceCard());
        }
        desc.put(SpellArg.TRIGGER, enchantmentDesc);
        super.onCast(context, player, desc, source, player);
    }
}
