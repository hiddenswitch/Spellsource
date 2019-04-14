package net.demilich.metastone.game.behaviour;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.ActionType;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.actions.PlayMinionCardAction;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.EntityZone;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.spells.trigger.Enchantment;
import net.demilich.metastone.game.targeting.EntityReference;

import java.util.LinkedList;
import java.util.List;

/**
 * HearthstoneBot-Rautenstengel-Camastro-Greedy
 */
public class TataTeBehaviour extends IntelligentBehaviour {

    private String name;
    private int mana;

    public TataTeBehaviour(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public GameAction requestAction(GameContext context, Player player, List<GameAction> validActions) {

        EntityZone<Minion> board = player.getMinions();
        if (board != null && !board.isEmpty()) {
            List<GameAction> smartTrades = doSmartTrades(context, validActions);
            if (smartTrades.size() > 0) {
                return smartTrades.get(0);      // Maybe better to change on "smartTrades.remove(0);"
            }
        }

        List<GameAction> minionAttacksOppHero = getMinionAttackList(validActions);
        if (minionAttacksOppHero.size() > 0) {
            return minionAttacksOppHero.get(0);
        }

        GameAction minionSummon = summonBestMinion(context, validActions);
        if (minionSummon != null) {
            return minionSummon;
        }

        GameAction spell = playSpell(validActions);
        if (spell != null) {
            return spell;
        }

        GameAction heroPower = useHeroPower(context, validActions);
        if (heroPower != null) {
            return heroPower;
        }

        GameAction attackingHero = getAttackingHeroPowerTask(context, validActions);
        if (attackingHero != null) {
            return attackingHero;
        }

        //after all other moves, end turn
        for (GameAction action : validActions) {
            if (action.getActionType() == ActionType.END_TURN) {
                return action;
            }
        }
        return validActions.get(0); // backup if nothing works
    }

    private GameAction summonBestMinion(GameContext context, List<GameAction> validActions) {
        int availableMana = context.getActivePlayer().getMana();
        GameAction minionWithHighestPlayableCost = null;
        int highestPlayableCost = -1;

        for (GameAction action : validActions) {
            if (action.getActionType() == ActionType.SUMMON && action instanceof PlayMinionCardAction) {
                int actionManaCost = action.getSource(context).getSourceCard().getManaCost(context, context.getActivePlayer());
                if (actionManaCost > highestPlayableCost && actionManaCost <= availableMana) {
                    minionWithHighestPlayableCost = action;
                    highestPlayableCost = actionManaCost;
                }
            }
        }
        return minionWithHighestPlayableCost;
    }


    private GameAction playSpell(List<GameAction> validActions) {
        for (GameAction action : validActions) {
            if (action.getActionType() == ActionType.SPELL) {
                return action;
            }
        }
        return null;
    }

    private List<GameAction> doSmartTrades(GameContext context, List<GameAction> validActions) {
        LinkedList<GameAction> minionAttacks = new LinkedList<GameAction>();
        for (GameAction action : validActions) {
            //get all attacks, which are not against the opponent hero
            if (action.getActionType() == ActionType.PHYSICAL_ATTACK && action.getTargetReference() != EntityReference.ENEMY_HERO) {
                int[] ownCardStats = getOwnBoardMinionStats(context, action.getSource(context).getSourceCard());
                int ownCardAttack = ownCardStats[0];
                int ownCardHealth = ownCardStats[1];

                int[] opponentCardStats = getOpponentBoardMinionStats(context, context.resolveSingleTarget(action.getTargetReference()).getSourceCard());
                int opponentCardAttack = opponentCardStats[0];
                int opponentCardHealth = opponentCardStats[1];

                //check if no taunt minion is in way
                if (!opponentHasTaunt(context)) {
                    //check if own minions survives AND opponent minion dies
                    if (ownCardHealth > opponentCardAttack && ownCardAttack >= opponentCardHealth) {
                        minionAttacks.addLast(action);
                    }
                }
                //or opponent minion has taunt
                else if (hasOpponentBoardMinionTaunt(context, context.resolveSingleTarget(action.getTargetReference()).getSourceCard())) {
                    minionAttacks.addLast(action);
                } else {
                    //do not add this action
                }
            }
        }
        return minionAttacks;
    }

    private GameAction useHeroPower(GameContext context, List<GameAction> validActions) {
        for (GameAction action : validActions) {
            if (action.getActionType() == ActionType.HERO_POWER) {
                if (isHeroPowerNeedingTarget(context) && isHeroPowerAggressive(context)) {
                    if (action.getTargetReference() == EntityReference.ENEMY_HERO) {
                        return action;
                    }
                } else if (isHeroPowerNeedingTarget(context) && !isHeroPowerAggressive(context)) {
                    if (action.getTargetReference() == EntityReference.FRIENDLY_HERO) {
                        return action;
                    }
                } else {
                    return action;
                }
            }

        }
        return null;
    }

    private GameAction getAttackingHeroPowerTask(GameContext context, List<GameAction> validActions) {
        for (GameAction action : validActions) {
            if (action.getActionType() == ActionType.PHYSICAL_ATTACK &&
                    action.getSourceReference() == EntityReference.FRIENDLY_HERO &&
                    action.getTargetReference() == EntityReference.ENEMY_HERO) {
                return action;
            }
        }
        return null;
    }


    private List<GameAction> getMinionAttackList(List<GameAction> validActions) {
        LinkedList<GameAction> minionAttacks = new LinkedList<GameAction>();
        for (GameAction action : validActions) {
            if (action.getActionType() == ActionType.PHYSICAL_ATTACK && action.getTargetReference() == EntityReference.ENEMY_HERO) {
                minionAttacks.addLast(action);
            }
        }
        return minionAttacks;
    }

    private boolean opponentHasTaunt(GameContext context) {
        Player opponent = context.getOpponent(context.getActivePlayer());
        for (Minion minion : opponent.getMinions()) {
            if (minion.getAttributes().containsKey(Attribute.TAUNT)) {
                return true;
            }
        }
        return false;
    }

    private int[] getOwnBoardMinionStats(GameContext context, Card card) {
        int ownCardAttack = 0;
        int ownCardHealth = 0;

        for (int i = 0; i < context.getActivePlayer().getMinions().size(); i++) {
            if (card == context.getActivePlayer().getMinions().get(i).getSourceCard()) {
                ownCardAttack = context.getActivePlayer().getMinions().get(i).getAttack();
                ownCardHealth = context.getActivePlayer().getMinions().get(i).getHp();
                break;
            }
        }
        return new int[]{ownCardAttack, ownCardHealth};
    }

    private int[] getOpponentBoardMinionStats(GameContext context, Card card) {
        int ownCardAttack = 0;
        int ownCardHealth = 0;

        for (int i = 0; i < context.getOpponent(context.getActivePlayer()).getMinions().size(); i++) {
            if (card == context.getOpponent(context.getActivePlayer()).getMinions().get(i).getSourceCard()) {
                ownCardAttack = context.getOpponent(context.getActivePlayer()).getMinions().get(i).getAttack();
                ownCardHealth = context.getOpponent(context.getActivePlayer()).getMinions().get(i).getHp();
                break;
            }
        }
        return new int[]{ownCardAttack, ownCardHealth};
    }

    private boolean hasOpponentBoardMinionTaunt(GameContext context, Card card) {
        boolean minionHasTaunt = false;

        for (int i = 0; i < context.getActivePlayer().getMinions().size(); i++) {
            if (card == context.getActivePlayer().getMinions().get(i).getSourceCard()) {
                minionHasTaunt = context.getActivePlayer().getMinions().get(i).getAttributes().containsKey(Attribute.TAUNT);
                break;
            }
        }
        return minionHasTaunt;
    }

    private boolean isHeroPowerAggressive(GameContext context) {
        switch (context.getActivePlayer().getHero().getHeroClass()) {
            case BLUE:
            case GREEN:
            case GOLD:
            case SILVER:
            case BLACK:
            case BROWN:
                return true;
        }
        return false;
    }

    private boolean isHeroPowerNeedingTarget(GameContext context) {
        switch (context.getActivePlayer().getHero().getHeroClass()) {
            case BLUE:
            case WHITE:
                return true;
        }
        return false;
    }


}
