package net.demilich.metastone.game.spells.desc.condition;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.condition.Condition;
import net.demilich.metastone.game.spells.desc.condition.ConditionDesc;
import net.demilich.metastone.game.utils.Attribute;

public class AttributeExistsCondition extends Condition {

    public AttributeExistsCondition(ConditionDesc desc) {
        super(desc);
    }

    @Override
    protected boolean isFulfilled(GameContext context, Player player, ConditionDesc desc, Entity source, Entity target) {
        Attribute attribute = (Attribute) desc.get(ConditionArg.ATTRIBUTE);
        return context.getLogic().attributeExists(attribute);
    }
}
