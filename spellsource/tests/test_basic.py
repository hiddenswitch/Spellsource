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
            gc.play()
            self.assertTrue(gc.updateAndGetGameOver())
    
    def test_simulation(self):
        with Context() as ctx:
            # Simulation function
            from spellsource.utils import simulate
            
            DECK_1 = '''### Control Warlock First
            # Class: VIOLET
            # Format: Standard
            #
            # 2x (1) Dark Pact
            # 2x (1) Kobold Librarian
            # 2x (1) Mortal Coil
            # 1x (2) Acidic Swamp Ooze
            # 2x (2) Defile
            # 2x (2) Doomsayer
            # 2x (2) Plated Beetle
            # 1x (3) Ironbeak Owl
            # 2x (3) Stonehill Defender
            # 1x (3) Voodoo Doll
            # 2x (4) Hellfire
            # 2x (4) Lesser Amethyst Spellstone
            # 2x (5) Possessed Lackey
            # 1x (6) Rin, the First Disciple
            # 1x (6) Siphon Soul
            # 1x (7) Lord Godfrey
            # 1x (8) Twisting Nether
            # 2x (9) Voidlord
            # 1x (10) Bloodreaver Gul'dan'''
            # Supports a deck list written in this format below:
            DECK_2 = '''### Control Warlock Second
            # Class: VIOLET
            # Format: Standard
            #
            # 2x (1) Dark Pact
            # 2x (1) Kobold Librarian
            # 2x (1) Mortal Coil
            # 1x (2) Acidic Swamp Ooze
            # 2x (2) Defile
            # 2x (2) Doomsayer
            # 2x (2) Plated Beetle
            # 1x (3) Ironbeak Owl
            # 2x (3) Stonehill Defender
            # 1x (3) Voodoo Doll
            # 2x (4) Hellfire
            # 2x (4) Lesser Amethyst Spellstone
            # 2x (5) Possessed Lackey
            # 1x (6) Rin, the First Disciple
            # 1x (6) Siphon Soul
            # 1x (7) Lord Godfrey
            # 1x (8) Twisting Nether
            # 2x (9) Voidlord
            # 1x (10) Bloodreaver Gul'dan'''
            
            per_game_stats = list(simulate(context=ctx, decks=(DECK_1, DECK_2), number=10,
                                           behaviours=('PlayRandomBehaviour', 'PlayRandomBehaviour')))
            self.assertTrue(len(per_game_stats) > 0)
