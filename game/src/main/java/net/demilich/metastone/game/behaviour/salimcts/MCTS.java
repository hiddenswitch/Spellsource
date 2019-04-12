package net.demilich.metastone.game.behaviour.salimcts;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.actions.GameAction;

import java.util.*;

import static net.demilich.metastone.game.logic.GameStatus.RUNNING;

/**
 * https://dockhorn.antares.uberspace.de/wordpress/bot-downloads/
 * <p>
 * based on https://daim.idi.ntnu.no/masteroppgaver/014/14750/masteroppgave.pdf
 */
public class MCTS {
    public static GameAction getBestAction(GameContext context, int iteerations) {
        TaskNode root = new TaskNode(null, null, context);
        for (int i = 0; i < iteerations; i++) {
            try {
                TaskNode node = root.selectNode();
                node = node.Expand();
                int r = node.simulateGames(5);
                node.backpropagate(r);
            } catch (Exception e) {
                // Don't know what to do...
            }
        }
        TaskNode best = null;
        for (TaskNode child : root.children) {
            if (best == null || child.totNumVisits > best.totNumVisits) {
                best = child;
            }
        }
        return best != null ? best.action : null;
    }


}

class TaskNode {
    static Random rand = new Random();
    static double biasParameter = 0.5;

    GameContext context;
    TaskNode parent;
    List<GameAction> possibleActions;
    public GameAction action;
    public List<TaskNode> children;

    public int totNumVisits = 0;
    public int wins = 0;

    public TaskNode(TaskNode parent, GameAction action, GameContext context) {
        this.context = context;
        this.parent = parent;
        this.action = action;
        this.possibleActions = context.getValidActions();
        this.children = new LinkedList<>();
    }

    public TaskNode selectNode() {
        if (possibleActions.size() == 0 && children.size() > 0) {
            double candidateScore = Double.MIN_VALUE;
            TaskNode candidate = null;
            for (TaskNode child : children) {
                double childScore = child.UCB1Score();
                if (childScore > candidateScore) {
                    candidateScore = childScore;
                    candidate = child;
                }
            }
            return candidate != null ? candidate.selectNode() : null;
        }

        return this;
    }

    private double UCB1Score() {
        double exploitScore = (double) wins / (double) totNumVisits;
        double explorationScore = Math.sqrt(Math.log(parent.totNumVisits) / totNumVisits);

        explorationScore *= biasParameter;

        return exploitScore + explorationScore;
    }

    public TaskNode Expand() {
        if (possibleActions.size() == 0) {
            return null;
        }
        GameAction action = possibleActions.get(rand.nextInt(possibleActions.size()));
        try {
            return addChild(action);
        } catch (Exception e) {
            return null;
        }
    }

    private TaskNode addChild(GameAction action) {
        possibleActions.remove(action);
        Map<GameAction, GameContext> map = new HashMap<>();
        GameContext childGame = map.get(action);
        TaskNode child = new TaskNode(this, action, childGame);
        this.children.add(child);
        return child;
    }

    public int simulateGames(int numGames) {
        int wins = 0;
        for (int i = 0; i < numGames; ++i) {
            try {
                wins += simulate();
            } catch (Exception e) {
                // Don't know what to do...
            }
        }
        return wins;
    }

    private int simulate() {
        GameContext gameClone = context.clone();
        int initialPlayer = gameClone.getActivePlayerId();
        while (true) {
            //if game ended
            if (!gameClone.getStatus().equals(RUNNING)) {
                if (gameClone.getWinner() != null && gameClone.getWinner().getId() == initialPlayer) {
                    return 1;
                } else return 0;
            }

            List<GameAction> options = gameClone.getValidActions();
            int randomNumber = rand.nextInt(options.size());
            GameAction action = options.get(randomNumber);
            gameClone.performAction(gameClone.getActivePlayerId(), action);
        }
    }

    public void backpropagate(int score) {
        int currentPlayerID = context.getActivePlayerId();
        TaskNode node = this;
        while (node.parent != null) {
            if (node.parent.context.getActivePlayerId() == currentPlayerID) {
                node.updateScore(score);
            } else {
                if (score == 0) {
                    node.updateScore(1);
                } else {
                    node.updateScore(0);
                }
            }
            node = node.parent;
        }
        node.totNumVisits++;
    }

    private void updateScore(int score) {
        totNumVisits++;
        wins += score;

    }
}