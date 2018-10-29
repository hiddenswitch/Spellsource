package net.demilich.metastone.game.spells;

import com.github.fromage.quasi.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.valueprovider.AlgebraicOperation;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.utils.Attribute;

public class ModifyDamageSpell extends Spell {

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		if (context.getDamageStack().isEmpty()) {
			return;
		}

		int damage = context.getDamageStack().pop();
		AlgebraicOperation operation = (AlgebraicOperation) desc.get(SpellArg.OPERATION);
		int value = desc.getValue(SpellArg.VALUE, context, player, target, source, 0);
		int minDamage = 0;

		// Fel's grip will modify all incoming damage when played as a spell.
		EntityReference eventTarget = context.getEventTargetStack().peek();
		boolean hasTakeDoubleDamage = false;
		if (eventTarget != null) {
			Entity damageTarget = context.resolveSingleTarget(eventTarget);
			hasTakeDoubleDamage = damageTarget.hasAttribute(Attribute.TAKE_DOUBLE_DAMAGE) || damageTarget.hasAttribute(Attribute.AURA_TAKE_DOUBLE_DAMAGE);
		}

		switch (operation) {
			case ADD:
				if (hasTakeDoubleDamage) {
					value *= 2;
				}
				damage += value;
				break;
			case SUBTRACT:
				if (hasTakeDoubleDamage) {
					value *= 2;
				}
				damage -= value;
				damage = Math.max(minDamage, damage);
				break;
			case MODULO:
				if (hasTakeDoubleDamage) {
					damage /= 2;
				}
				damage %= value;
				if (hasTakeDoubleDamage) {
					damage *= 2;
				}
				break;
			case MULTIPLY:
				damage *= value;
				break;
			case DIVIDE:
				damage /= value;
				damage = Math.max(minDamage, damage);
				break;
			case NEGATE:
				damage = -damage;
				break;
			case SET:
				if (hasTakeDoubleDamage) {
					value *= 2;
				}
				damage = value;
				break;
			case MINIMUM:
				if (hasTakeDoubleDamage) {
					value *= 2;
				}
				if (damage < value) {
					damage = value;
				}
				break;
			case MAXIMUM:
				if (hasTakeDoubleDamage) {
					value *= 2;
				}
				if (damage > value) {
					damage = value;
				}
				break;
			default:
				break;
		}

		context.getDamageStack().push(damage);
	}

}
