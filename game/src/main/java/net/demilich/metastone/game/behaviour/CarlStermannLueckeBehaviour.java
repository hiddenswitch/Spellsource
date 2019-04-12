package net.demilich.metastone.game.behaviour;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.*;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.targeting.EntityReference;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * https://dockhorn.antares.uberspace.de/wordpress/bot-downloads/
 */

public class CarlStermannLueckeBehaviour extends IntelligentBehaviour {

    private String name;

    public CarlStermannLueckeBehaviour(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public List<Card> mulligan(GameContext context, Player player, List<Card> cards) {
        List<Card> discardedCards = new ArrayList<Card>();
        for (Card card : cards) {
            if (card.getBaseManaCost() > 3) {
                discardedCards.add(card);
            }
        }
        return discardedCards;
    }

    @Override
    public GameAction requestAction(GameContext context, Player player, List<GameAction> validActions) {
        List<GameAction> options = new ArrayList<>();
        for (GameAction gameAction : validActions) {
            if (!(gameAction.getTargetReference() == EntityReference.FRIENDLY_HERO)) options.add(gameAction);

        }
        return getBestMove(options);
    }

    private GameAction getBestMove(List<GameAction> options) {
        PlayMinionCardAction summonMinion = null;

        for (GameAction option : options) {
            if (option instanceof PlayMinionCardAction) summonMinion = (PlayMinionCardAction) option;

        }
        if (summonMinion != null) return summonMinion;

        LinkedList<GameAction> minionAttacks = new LinkedList<>();
        for (GameAction option : options) {
            if (option.getActionType() == ActionType.PHYSICAL_ATTACK && option.getTargetReference() == EntityReference.ENEMY_HERO)
                minionAttacks.add(option);
        }
        if (minionAttacks.size() > 0) return minionAttacks.remove(0);
        if (options.size() > 1 && options.get(0).getActionType() == ActionType.END_TURN) return options.remove(1);
        else return options.remove(0);
    }
}
