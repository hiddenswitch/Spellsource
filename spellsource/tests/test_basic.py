import unittest
from spellsource.context import Context
from spellsource.playrandombehaviour import PlayRandomBehaviour
from spellsource.gamestatevaluebehaviour import GameStateValueBehaviour


class BasicTest(unittest.TestCase):
    def test_start_jvm(self):
        context_entered = False
        with Context() as ctx:
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
            gc.getPlayer1().setBehaviour(behaviour1.wrap(ctx))
            gc.getPlayer2().setBehaviour(behaviour2.wrap(ctx))
            gc.play()
            self.assertTrue(gc.updateAndGetGameOver())
    
    def test_advanced_behaviour(self):
        with Context() as ctx:
            gc = ctx.game.GameContext.fromTwoRandomDecks()
            behaviour1 = GameStateValueBehaviour(ctx)
            behaviour2 = PlayRandomBehaviour()
            gc.getPlayer1().setBehaviour(behaviour1.wrap(ctx))
            gc.getPlayer2().setBehaviour(behaviour2.wrap(ctx))
            gc.play()
            self.assertTrue(gc.updateAndGetGameOver())
