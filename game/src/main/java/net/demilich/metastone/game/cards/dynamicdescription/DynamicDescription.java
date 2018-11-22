package net.demilich.metastone.game.cards.dynamicdescription;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.desc.Desc;
import net.demilich.metastone.game.cards.desc.HasDesc;
import net.demilich.metastone.game.logic.CustomCloneable;

public abstract class DynamicDescription extends CustomCloneable implements HasDesc<DynamicDescriptionDesc> {

    private DynamicDescriptionDesc desc;

    public DynamicDescription(DynamicDescriptionDesc desc) {
        this.desc = desc;
    }

    public abstract String resolveFinalString(GameContext context, Player player, Card card);

    @Override
    public DynamicDescriptionDesc getDesc() {
        return desc;
    }

    @Override
    public void setDesc(Desc<?, ?> desc) {
        this.desc = (DynamicDescriptionDesc) desc;
    }
}
