package net.demilich.metastone.game.spells;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.cards.Attribute;

import java.util.Map;

/**
 * Swaps the {@code target}'s attack and hitpoints, making the target's hitpoints its attack value and vice versa.
 */
public class SwapAttackAndHpSpell extends Spell {

	public static SpellDesc create() {
		return create(null);
	}

	public static SpellDesc create(EntityReference target) {
		Map<SpellArg, Object> arguments = new SpellDesc(SwapAttackAndHpSpell.class);
		arguments.put(SpellArg.TARGET, target);
		return new SpellDesc(arguments);
	}

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		if (target instanceof Minion) {
			Minion minion = (Minion) target;
			int attack = minion.getAttack();
			int hp = minion.getHp();
			minion.getAttributes().remove(Attribute.TEMPORARY_ATTACK_BONUS);
			minion.getAttributes().remove(Attribute.ATTACK_BONUS);
			minion.getAttributes().remove(Attribute.HP_BONUS);
			minion.setAttack(hp);
			context.getLogic().setHpAndMaxHp(minion, attack);
		} else if (target instanceof Card) {
			Card card = (Card) target;
			int attack = card.getAttack();
			int attackBonus = card.getBonusAttack();
			int hp = card.getHp();
			int hpBonus = card.getBonusHp();
			card.getAttributes().put(Attribute.HP, attack);
			card.getAttributes().put(Attribute.HP_BONUS, attackBonus);
			card.getAttributes().put(Attribute.ATTACK, hp);
			card.getAttributes().put(Attribute.ATTACK_BONUS, hpBonus);
		}

	}

}