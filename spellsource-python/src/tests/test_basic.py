import unittest

from spellsource.behaviour import *
from spellsource.context import Context
from spellsource.gamestatevaluebehaviour import GameStateValueBehaviour
from spellsource.playrandombehaviour import PlayRandomBehaviour
from .decks import *


class BasicTest(unittest.TestCase):
    def test_start_jvm(self):
        context_entered = False
        with Context() as ctx:
            self.assertEqual(ctx.status, Context.STATUS_READY)
            context_entered = True
        self.assertTrue(context_entered)

    def test_start_jvm_with_port(self):
        context_entered = False
        with Context(port=9090) as ctx:
            self.assertEqual(ctx.status, Context.STATUS_READY)
            context_entered = True
        self.assertTrue(context_entered)

    def test_create_game_context(self):
        with Context() as ctx:
            self.assertEqual(ctx.status, Context.STATUS_READY)
            gc = ctx.game.GameContext.fromTwoRandomDecks()
            self.assertEqual(gc.getPlayer1().getId(), 0)

    def test_behaviour(self):
        with Context() as ctx:
            gc = ctx.game.GameContext.fromTwoRandomDecks()
            behaviour1 = PlayRandomBehaviour()
            behaviour2 = PlayRandomBehaviour()
            gc.setBehaviour(0, behaviour1.wrap(ctx))
            gc.setBehaviour(1, behaviour2.wrap(ctx))
            gc.play()
            self.assertTrue(gc.updateAndGetGameOver())

    def test_advanced_behaviour(self):
        with Context() as ctx:
            gc = ctx.game.GameContext.fromTwoRandomDecks()
            behaviour1 = GameStateValueBehaviour(ctx)
            behaviour2 = PlayRandomBehaviour()
            gc.setBehaviour(0, behaviour1.wrap(ctx))
            gc.setBehaviour(1, behaviour2.wrap(ctx))
            # We'll just play one turn turns
            gc.init()
            gc.setActivePlayerId(0)
            gc.startTurn(0)
            while gc.takeActionInTurn():
                pass
            self.assertTrue(gc.getActionsThisTurn() > 0)

    def test_simulation_two_different_bots(self):
        _behaviours_called = [False, False]

        class PythonBehaviour1(Behaviour):
            def clone(self) -> Behaviour:
                return PythonBehaviour1()

            def get_name(self) -> str:
                return 'Name 1'

            def mulligan(self, context: GameContext, player: Player, cards: List[Card]) -> List[Card]:
                return []

            def request_action(self, context: GameContext, player: Player,
                               valid_actions: List[GameAction]) -> GameAction:
                _behaviours_called[0] = True
                return valid_actions[0]

        class PythonBehaviour2(Behaviour):
            def clone(self) -> Behaviour:
                return PythonBehaviour2()

            def get_name(self) -> str:
                return 'Name 2'

            def mulligan(self, context: GameContext, player: Player, cards: List[Card]) -> List[Card]:
                return []

            def request_action(self, context: GameContext, player: Player,
                               valid_actions: List[GameAction]) -> GameAction:
                _behaviours_called[1] = True
                return valid_actions[0]

        with Context() as ctx:
            from spellsource.utils import simulate
            results = list(
                simulate(context=ctx, decks=(DECK_1, DECK_2), behaviours=[PythonBehaviour1, PythonBehaviour2],
                         number=1))
            self.assertTrue(all(_behaviours_called))

    def test_simulation(self):
        with Context() as ctx:
            # Simulation function
            from spellsource.utils import simulate

            per_game_stats = list(simulate(context=ctx, decks=(DECK_1, DECK_2), number=10,
                                           behaviours=('PlayRandomBehaviour', 'PlayRandomBehaviour')))
            self.assertTrue(len(per_game_stats) > 0)

    def test_notebook(self):
        from tqdm import tqdm
        from spellsource.context import Context
        from spellsource.utils import simulate

        with Context() as ctx:
            results = list(tqdm(simulate(
                behaviours=('PlayRandomBehaviour', 'PlayRandomBehaviour'),
                decks=(DECK_3, DECK_4, DECK_5, DECK_6),
                number=100,
                context=ctx)))
            self.assertIsNotNone(results)
            self.assertEqual(len(results), 6)
            results = list(tqdm(simulate(
                behaviours=('GameStateValueBehaviour', 'GameStateValueBehaviour'),
                decks=(DECK_3, DECK_4, DECK_5, DECK_6),
                number=1,
                context=ctx)))
            self.assertIsNotNone(results)
            self.assertEqual(len(results), 6)

    @unittest.skip("FiberBehaviour is deprecated")
    def test_faq(self):
        from spellsource.context import Context
        with Context() as ctx:
            game_context = ctx.GameContext.fromDeckLists([TOKEN_DRUID, EVEN_WARLOCK])
            agent_1 = ctx.behaviour.FiberBehaviour()
            agent_2 = ctx.behaviour.FiberBehaviour()

            game_context.setBehaviour(ctx.GameContext.PLAYER_1, agent_1)
            game_context.setBehaviour(ctx.GameContext.PLAYER_2, agent_2)
            SEED = 10101
            game_context.setLogic(ctx.GameLogic(SEED))
            game_context.play(True)

            def pending_mulligans():
                for i, agent in enumerate([agent_1, agent_2]):
                    print('Agent', i + 1, [card.toString() for card in agent.getMulliganCards()])

            from time import sleep
            sleep(0.01)
            pending_mulligans()
            malfurion_the_pestilent = agent_1.getMulliganCards()[2]
            self.assertEqual(malfurion_the_pestilent.getName(), 'Malfurion the Pestilent')
            agent_1.setMulligan([malfurion_the_pestilent])
            sleep(0.01)
            pending_mulligans()
            sleep(0.01)
            agent_2.setMulligan([])
            _ = game_context.getActivePlayer().getName()
            _ = [action.toString() for action in agent_1.getValidActions()]
            _ = [card.toString() for card in game_context.getActivePlayer().getHand()]
            _ = game_context.getActivePlayer().getMana()
            _ = game_context.getActivePlayer().getHand()[0].getDescription()
            card_bloodfen_raptor = ctx.CardCatalogue.getCardByName("Bloodfen Raptor")
            self.assertIsNotNone(card_bloodfen_raptor)
            bloodfen_raptor = card_bloodfen_raptor.summon()
            opponent = game_context.getOpponent(game_context.getActivePlayer())
            self.assertTrue(game_context.getLogic().summon(opponent.getId(), bloodfen_raptor, card_bloodfen_raptor, -1, False))
            _ = [action.toString() for action in agent_1.getValidActions()]
            for i, action in enumerate(agent_1.getValidActions()):
                source = game_context.resolveSingleTarget(action.getSourceReference())
                target = game_context.resolveSingleTarget(action.getTargetReference())
                print('%d %s %s: Targeting %s' % (i, action.getActionType().toString(), source, target))
            clone = game_context.clone()