package net.demilich.metastone.game.spells.custom;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.ChangeHeroPowerSpell;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;

import java.util.List;

public class UpgradeHeroPowerSpell extends ChangeHeroPowerSpell {

    @Override
    public void cast(GameContext context, Player player, SpellDesc desc, Entity source, List<Entity> targets) {
        Card heroPower = player.getHeroPowerZone().get(0);
        if (heroPower.getDesc().heroPower != null) {
            desc = desc.addArg(SpellArg.CARD, heroPower.getDesc().heroPower);
            super.cast(context, player, desc, source, targets);
        }

    }
}
