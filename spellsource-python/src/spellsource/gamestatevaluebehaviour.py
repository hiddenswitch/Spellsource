from typing import List

from spellsource.behaviour import Behaviour, GameContext, Hero, Card, Minion, GameAction, Player
from spellsource.context import Context
from collections import OrderedDict, defaultdict


class GameStateValueBehaviour(Behaviour):
    THREAT_LEVEL_RED = 0
    THREAT_LEVEL_YELLOW = 1
    THREAT_LEVEL_GREEN = 2
    
    HARD_REMOVAL = frozenset([
        "spell_polymorph",
        "spell_execute",
        "spell_crush",
        "spell_assassinate",
        "spell_siphon_soul",
        "spell_shadow_word_death",
        "spell_naturalize",
        "spell_hex",
        "spell_humility",
        "spell_equality",
        "spell_deadly_shot",
        "spell_sap",
        "minion_doomsayer",
        "minion_big_game_hunter"
    ])
    
    def __init__(self, context: Context):
        self._ctx = context
        # Weights for calculating heuristic scoring of state
        self.weights = OrderedDict({
            'RED_MODIFIER': -43,
            'YELLOW_MODIFIER': -17,
            'OWN_HP_FACTOR': 0.214,
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
        
        self.card_weights = defaultdict(int)
        self.card_weights['spell_cursed'] = -11.912
    
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
        
        return GameStateValueBehaviour.THREAT_LEVEL_GREEN
    
    def get_hero_damage(self, hero: Hero) -> int:
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
        
        if player.getWeaponZone().get(0) is not None:
            hero_damage += player.getWeaponZone().get(0).getWeaponDamage()
        
        return hero_damage
    
    def is_hard_removal(self, card: Card) -> bool:
        is_poisonous = card.hasAttribute(self._ctx.Attribute.POISONOUS) or card.hasAttribute(
            self._ctx.Attribute.AURA_POISONOUS)
        destroy_spell = False
        
        if card.getDesc().getBattlecry() is not None and card.getDesc().getBattlecry().getSpell() is not None:
            spell = card.getDesc().getBattlecry().getSpell()
            destroy_spell = destroy_spell or 'DestroySpell' in spell.getDescClass().getName()
            destroy_spell = destroy_spell or any(
                'DestroySpell' in sub_spell.getDescClass().getName() for sub_spell in spell.subSpells())
        
        if card.getSpell() is not None:
            spell = card.getSpell()
            destroy_spell = destroy_spell or 'DestroySpell' in spell.getDescClass().getName()
            destroy_spell = destroy_spell or any(
                'DestroySpell' in sub_spell.getDescClass().getName() for sub_spell in spell.subSpells())
        
        return card.getCardId() in GameStateValueBehaviour.HARD_REMOVAL or is_poisonous or destroy_spell
    
    def calculate_minion_score(self, minion: Minion, threat_level) -> float:
        minion_score = self.weights['MINION_INTRINSIC_VALUE'] * 1.0
        minion_score += self.weights['MINION_ATTACK_FACTOR'] * (
                minion.getAttack() - minion.getAttributeValue(self._ctx.Attribute.TEMPORARY_ATTACK_BONUS))
        minion_score += self.weights['MINION_HP_FACTOR'] * minion.getHp()
        
        if minion.hasAttribute(self._ctx.Attribute.TAUNT) or minion.hasAttribute(self._ctx.Attribute.AURA_TAUNT):
            if threat_level == GameStateValueBehaviour.THREAT_LEVEL_RED:
                minion_score += self.weights['MINION_RED_TAUNT_MODIFIER']
            elif threat_level == GameStateValueBehaviour.THREAT_LEVEL_YELLOW:
                minion_score += self.weights['MINION_YELLOW_TAUNT_MODIFIER']
            else:
                minion_score += self.weights['MINION_DEFAULT_TAUNT_MODIFIER']
        
        if minion.hasAttribute(self._ctx.Attribute.WINDFURY) or minion.hasAttribute(self._ctx.Attribute.AURA_WINDFURY):
            minion_score += self.weights['MINION_WINDFURY_MODIFIER']
        elif minion.hasAttribute(self._ctx.Attribute.MEGA_WINDFURY):
            minion_score += 2 * self.weights['MINION_WINDFURY_MODIFIER']
        
        if minion.hasAttribute(self._ctx.Attribute.DIVINE_SHIELD):
            minion_score += self.weights['MINION_DIVINE_SHIELD_MODIFIER']
        
        if minion.hasAttribute(self._ctx.Attribute.SPELL_DAMAGE):
            minion_score += self.weights['MINION_SPELL_POWER_MODIFIER'] * minion.getAttributeValue(
                self._ctx.Attribute.SPELL_DAMAGE)
        
        if minion.hasAttribute(self._ctx.Attribute.STEALTH) or minion.hasAttribute(self._ctx.Attribute.AURA_STEALTH):
            minion_score += self.weights['MINION_STEALTHED_MODIFIER']
        
        if minion.hasAttribute(self._ctx.Attribute.UNTARGETABLE_BY_SPELLS):
            minion_score += self.weights['MINION_UNTARGETABLE_BY_SPELLS_MODIFIER']
        
        return minion_score
    
    def get_score(self, context: GameContext, player_id: int) -> float:
        player = context.getPlayer(player_id)
        opponent = context.getOpponent(player)
        if player.getHero().isDestroyed():
            return float('-Inf')
        if opponent.getHero().isDestroyed():
            return float('Inf')
        score = 0.0
        threat_level = self.calculate_threat_level(context, player_id)
        if threat_level == GameStateValueBehaviour.THREAT_LEVEL_RED:
            score += self.weights['RED_MODIFIER']
        elif threat_level == GameStateValueBehaviour.THREAT_LEVEL_YELLOW:
            score += self.weights['YELLOW_MODIFIER']
        
        score += player.getHero().getEffectiveHp() * self.weights['OWN_HP_FACTOR']
        score += opponent.getHero().getEffectiveHp() * self.weights['OPPONENT_HP_FACTOR']
        
        for card in player.getHand():
            if self.is_hard_removal(card):
                score += self.weights['HARD_REMOVAL_VALUE']
            
            score += self.card_weights[card.getCardId()]
        
        score += len(player.getHand()) * self.weights['OWN_CARD_COUNT']
        score += len(opponent.getHand()) * self.weights['OPPONENT_CARD_COUNT']
        
        for minion in player.getMinions():
            score += self.calculate_minion_score(minion, threat_level)
        
        for minion in opponent.getMinions():
            score -= self.calculate_minion_score(minion, threat_level)
        
        quest_count = len(player.getQuests())
        if quest_count > 0:
            quest_count += player.getQuests()[0].getFires()
        
        quest_rewards = 0
        for e in player.getRemovedFromPlay():
            if e.getEntityType() == self._ctx.EntityType.QUEST:
                # Inspects the Quest object to determine whether it delivered an award
                if e.isExpired() and e.getFires() == e.getCountUntilCast():
                    quest_rewards += 1
        
        score += quest_count * self.weights['QUEST_COUNTER_VALUE']
        score += quest_rewards * self.weights['QUEST_REWARD_VALUE']
        return score
    
    def alpha_beta(self, context: GameContext, player_id: int, action: GameAction, depth: int) -> float:
        """
        Perform "alpha beta pruning" (a kind of minimax) to find the next action to take.
        
        Since we do not simulate the opponent's turn, there is no beta used here. Indeed, this is just a basic minimax
        algorithm that brute-force searches all the actions to find the best possible action, up to a certain depth.
        
        See the Java implementation of GameStateValueBehaviour for one that caches the search.
        :param context:
        :param player_id:
        :param action:
        :param depth:
        :return:
        """
        simulation = context.clone()
        score = float('-Inf')
        opponent = simulation.getOpponent(simulation.getPlayer(player_id))
        simulation.getLogic().removeSecrets(opponent)
        if simulation.isDisposed():
            return float('-Inf')
        simulation.getLogic().performGameAction(player_id, action)
        
        if any(any(c.getSourceCard().getCardId() == 'minion_doomsayer' for c in p.getMinions()) for p in
               simulation.getPlayers()):
            for m in simulation.getPlayer(player_id).getMinions():
                simulation.getLogic().markAsDestroyed(m)
                simulation.getLogic().endOfSequence()
        
        if simulation.getActivePlayerId() != player_id or simulation.updateAndGetGameOver():
            return self.get_score(simulation, player_id)
        
        valid_actions = simulation.getValidActions()
        for game_action in valid_actions:
            score = max(score, self.alpha_beta(simulation, player_id, game_action, depth - 1))
            if score >= 100000:
                break
        
        return score
    
    def get_name(self):
        return 'Game state value behaviour'
    
    def mulligan(self, context: GameContext, player: Player, cards: List[Card]):
        return [c for c in cards if c.getBaseManaCost() > 3]
    
    def request_action(self, context: GameContext, player: Player, valid_actions: List[GameAction], depth=2):
        if len(valid_actions) == 1:
            return valid_actions[0]
        
        if valid_actions[0].getActionType().toString() == 'BATTLECRY':
            depth = 0
        elif valid_actions[0].getActionType().toString() == 'DISCOVER':
            return valid_actions[0]
        
        best_action = valid_actions[0]
        best_score = float('-Inf')
        
        for game_action in valid_actions:
            score = self.alpha_beta(context, player.getId(), game_action, depth)
            if score > best_score:
                best_action = game_action
                best_score = score
        
        return best_action
    
    def clone(self):
        return GameStateValueBehaviour(context=self._ctx)
