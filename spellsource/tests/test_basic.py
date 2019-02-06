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

    def test_notebook(self):
        from tqdm import tqdm
        from spellsource.context import Context
        from spellsource.utils import simulate

        DECK_1 = '''### Cubelock - Standard Meta Snapshot - May 9, 2018
        # Class: Warlock
        # Format: Standard
        # Year of the Raven
        #
        # 2x (1) Dark Pact
        # 2x (1) Kobold Librarian
        # 1x (2) Acidic Swamp Ooze
        # 2x (2) Defile
        # 2x (2) Plated Beetle
        # 2x (3) Stonehill Defender
        # 2x (4) Hellfire
        # 2x (4) Lesser Amethyst Spellstone
        # 1x (4) Spiritsinger Umbra
        # 2x (5) Carnivorous Cube
        # 2x (5) Doomguard
        # 1x (5) Faceless Manipulator
        # 2x (5) Possessed Lackey
        # 1x (5) Skull of the Man'ari
        # 1x (6) Rin, the First Disciple
        # 1x (7) Lord Godfrey
        # 2x (9) Voidlord
        # 1x (10) Bloodreaver Gul'dan
        # 1x (12) Mountain Giant
        '''

        DECK_2 = '''### Even Paladin - Standard Meta Snapshot - May 9, 2018
        # Class: Paladin
        # Format: Standard
        # Year of the Raven
        #
        # 1x (2) Acidic Swamp Ooze
        # 2x (2) Amani Berserker
        # 2x (2) Dire Wolf Alpha
        # 2x (2) Equality
        # 2x (2) Knife Juggler
        # 2x (2) Loot Hoarder
        # 2x (4) Blessing of Kings
        # 2x (4) Call to Arms
        # 2x (4) Consecration
        # 2x (4) Saronite Chain Gang
        # 2x (4) Spellbreaker
        # 2x (4) Truesilver Champion
        # 2x (6) Argent Commander
        # 2x (6) Avenging Wrath
        # 1x (6) Genn Greymane
        # 1x (6) Sunkeeper Tarim
        # 1x (6) Val'anyr
        '''
        DECK_3 = '''### Spiteful Druid - Standard Meta Snapshot - May 9, 2018
        # Class: Druid
        # Format: Standard
        # Year of the Raven
        #
        # 2x (1) Fire Fly
        # 2x (1) Glacial Shard
        # 1x (2) Prince Keleseth
        # 2x (3) Crypt Lord
        # 2x (3) Druid of the Scythe
        # 2x (3) Greedy Sprite
        # 2x (3) Mind Control Tech
        # 1x (3) Tar Creeper
        # 2x (4) Saronite Chain Gang
        # 2x (4) Spellbreaker
        # 2x (5) Cobalt Scalebane
        # 2x (5) Fungalmancer
        # 1x (5) Leeroy Jenkins
        # 2x (6) Spiteful Summoner
        # 1x (7) Malfurion the Pestilent
        # 1x (8) Grand Archivist
        # 1x (8) The Lich King
        # 2x (10) Ultimate Infestation
        '''

        DECK_4 = '''### Aggro Mage - Standard Meta Snapshot - Apr. 30, 2018
        # Class: Mage
        # Format: Standard
        # Year of the Raven
        #
        # 2x (1) Arcane Missiles
        # 2x (1) Mana Wyrm
        # 1x (1) Mirror Image
        # 1x (2) Amani Berserker
        # 2x (2) Arcanologist
        # 1x (2) Bloodmage Thalnos
        # 2x (2) Frostbolt
        # 2x (2) Primordial Glyph
        # 2x (2) Sorcerer's Apprentice
        # 2x (3) Arcane Intellect
        # 2x (3) Cinderstorm
        # 2x (3) Counterspell
        # 2x (3) Explosive Runes
        # 2x (3) Kirin Tor Mage
        # 2x (4) Fireball
        # 1x (4) Lifedrinker
        # 1x (6) Aluneth
        # 1x (10) Pyroblast
        '''

        with Context() as ctx:
            results = list(tqdm(simulate(
                behaviours=('PlayRandomBehaviour', 'PlayRandomBehaviour'),
                decks=(DECK_1, DECK_2, DECK_3, DECK_4),
                number=100,
                context=ctx)))
            self.assertIsNotNone(results)
            self.assertEqual(len(results), 6)
            results = list(tqdm(simulate(
                behaviours=('GameStateValueBehaviour', 'GameStateValueBehaviour'),
                decks=(DECK_1, DECK_2, DECK_3, DECK_4),
                number=1,
                context=ctx)))
            self.assertIsNotNone(results)
            self.assertEqual(len(results), 6)
