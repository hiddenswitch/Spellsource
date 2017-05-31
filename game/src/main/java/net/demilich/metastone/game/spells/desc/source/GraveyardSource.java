package net.demilich.metastone.game.spells.desc.source;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.cards.CardArrayList;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.EntityType;
import net.demilich.metastone.game.entities.minions.Minion;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by finaris on 1/24/17.
 */

public class GraveyardSource extends CardSource implements Serializable {

    public GraveyardSource(SourceDesc desc) {
        super(desc);
    }

    @Override
    protected CardList match(GameContext context, Player player) {
        CardList deadMinions = new CardArrayList();
        ArrayList<Entity> graveyard = new ArrayList<>();
        graveyard.addAll(player.getGraveyard());
        for (Entity deadEntity : graveyard) {
            if (deadEntity.getEntityType() == EntityType.MINION) {
                Minion graveMinion = (Minion) deadEntity;
                deadMinions.addCard(graveMinion.getSourceCard());
            }
        }
        return deadMinions.clone();
    }

}
