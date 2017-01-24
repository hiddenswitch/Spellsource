package net.demilich.metastone.game.spells.desc.source;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.CardCollection;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.EntityType;
import net.demilich.metastone.game.entities.minions.Minion;
import java.io.Serializable;
import java.util.ArrayList;

public class GraveyardSource extends CardSource implements Serializable {

    public GraveyardSource(SourceDesc desc) {
        super(desc);
    }

    @Override
    protected CardCollection match(GameContext context, Player player) {
        CardCollection deadMinions = new CardCollection();
        ArrayList<Entity> graveyard = new ArrayList<>();
        graveyard.addAll(player.getGraveyard());
        for (Entity deadEntity : graveyard) {
            if (deadEntity.getEntityType() == EntityType.MINION) {
                Minion graveMinion = (Minion) deadEntity;
                deadMinions.add(graveMinion.getSourceCard());
            }
        }
        return deadMinions.clone();
    }

}
