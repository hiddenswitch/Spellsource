package net.demilich.metastone.game.behaviour.salimcts;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.ActionType;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.behaviour.IntelligentBehaviour;

import java.util.Date;
import java.util.List;

public class SaliMCTSBehaviour extends IntelligentBehaviour {
    private double remainingSeconds = 15;
    private int actionCount = 0;
    private String name;

    public SaliMCTSBehaviour(String name) {
        this.name = name;
        remainingSeconds = 15;
        actionCount = 0;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public GameAction requestAction(GameContext context, Player player, List<GameAction> validActions) {
        Date start = new Date();

        GameAction t;
        if (validActions.size() == 1) t = validActions.get(0);
        else if (validActions.size() == 2) t = validActions.get(1);
        else
            t = MCTS.getBestAction(context, (int) (remainingSeconds / (Math.max(validActions.size() - actionCount, 1))));

        double seconds = (new Date().getTime() - start.getTime()) / 1000;
        if (t != null && t.getActionType() == ActionType.END_TURN) {
            remainingSeconds = 15;
            actionCount = 0;
        } else {
            remainingSeconds = -seconds;
            actionCount++;
        }
        if (t == null) return validActions.get(0);
        return t;
    }
}
