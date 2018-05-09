from pyspellsource.behaviour import Behaviour, GameContext, Hero
from pyspellsource.context import Context
from collections import OrderedDict


class GameStateValueBehaviour(Behaviour):
    THREAT_LEVEL_RED = 0
    THREAT_LEVEL_YELLOW = 1
    THREAT_LEVEL_GREEN = 2
    
    def __init__(self, context: Context):
        self._ctx = context
        self.feature_vector = OrderedDict({
            'RED_MODIFIER': -43,
            'YELLOW_MODIFIER': -17,
            'OWN_HP_FACTOR': 0.214,
            'CURSED_FACTOR': -11.912,
            'OPPONENT_HP_FACTOR': -1.115,
            'OWN_CARD_COUNT': 3.572,
            'OPPONENT_CARD_COUNT': 0,
            'MINION_INTRINSIC_VALUE': 1.181,
            'MINION_ATTACK_FACTOR': 2.419,
            'MINION_HP_FACTOR': 3,
            'MINION_RED_TAUNT_MODIFIER': 10.1,
            'MINION_YELLOW_TAUNT_MODIFIER': 7.1,
            'MINION_DEFAULT_TAUNT_MODIFIER': 0.671,
            'MINION_WINDFURY_MODIFIER': 15.71,
            'MINION_DIVINE_SHIELD_MODIFIER': 6.1,
            'MINION_SPELL_POWER_MODIFIER': 3.841,
            'MINION_STEALTHED_MODIFIER': 1.281,
            'MINION_UNTARGETABLE_BY_SPELLS_MODIFIER': 0,
            'HARD_REMOVAL_VALUE': 2,
            'QUEST_COUNTER_VALUE': 33.3,
            'QUEST_REWARD_VALUE': 55.7,
        })
    
    def calculate_threat_level(self, context: GameContext, player_id: int):
        damage_on_board = 0
        player = context.getPlayer(player_id)
        opponent = context.getOpponent(player)
        for minion in opponent.getMinions():
            damage_on_board += minion.getAttack() * minion.getAttributeValue(self._ctx.Attribute.NUMBER_OF_ATTACKS)
        damage_on_board += self.get_hero_damage(opponent.getHero())
        
        remaining_hp = player.getHero().getEffectiveHp() - damage_on_board
        if remaining_hp < 1:
            return GameStateValueBehaviour.THREAT_LEVEL_RED
        elif remaining_hp < 15:
            return GameStateValueBehaviour.THREAT_LEVEL_YELLOW
        
        return GameStateValueBehaviour.THREAT_LEVEL_RED
    
    def get_hero_damage(self, hero: Hero):
        hero_damage = 0
        hc = hero.getHeroClass()
        if hc == self._ctx.HeroClass.BLUE:
            hero_damage += 1
        elif hc == self._ctx.HeroClass.GREEN:
            hero_damage += 2
        elif hc == self._ctx.HeroClass.BROWN:
            hero_damage += 1
        elif hc == self._ctx.HeroClass.BLACK:
            hero_damage += 1
        
        if hero.getWeapon() is not None:
            hero_damage += hero.getWeapon().getWeaponDamage()
