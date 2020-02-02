package net.demilich.metastone.game.spells.desc.condition;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.SpellUtils;
import net.demilich.metastone.game.spells.desc.filter.ComparisonOperation;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import net.demilich.metastone.game.spells.desc.valueprovider.ValueProviderArg;
import net.demilich.metastone.game.targeting.EntityReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NumberOfCopiesCondition extends Condition {

    public NumberOfCopiesCondition(ConditionDesc desc) {
        super(desc);
    }

    @Override
    protected boolean isFulfilled(GameContext context, Player player, ConditionDesc desc, Entity source, Entity target) {
        List<Entity> targets;
        if (desc.containsKey(ConditionArg.TARGET)) {
            targets = context.resolveTarget(player, source, (EntityReference) desc.get(ConditionArg.TARGET));
        } else {
            targets = Collections.singletonList(target);
        }

        if (desc.containsKey(ConditionArg.FILTER)) {
            targets = targets.stream()
                    .filter(((EntityFilter) desc.get(ConditionArg.FILTER)).matcher(context, player, source))
                    .collect(Collectors.toList());
        }

        int targetValue = desc.getInt(ConditionArg.VALUE);
        ComparisonOperation operation = (ComparisonOperation) desc.get(ConditionArg.OPERATION);

        List<String> ids = targets.stream().map(entity -> entity.getSourceCard().getCardId()).collect(Collectors.toList());

        boolean result = true;

        for (String id : ids.stream().distinct().collect(Collectors.toList())) {
            int copies = (int) ids.stream().filter(s -> s.equals(id)).count();
            if (!SpellUtils.evaluateOperation(operation, copies, targetValue)) {
                result = false;
            }
        }

        return result;
    }
}
