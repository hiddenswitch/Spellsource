package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.cards.Attribute;

import java.util.Map;

/**
 * Doubles the {@code target} {@link Actor}'s total attack.
 * <p>
 * If some amount of the actor's current attack is temporary (stored in {@link Attribute#TEMPORARY_ATTACK_BONUS}), the
 * portion of the attack doubling that came from temporary attack is temporary too.
 */
public class DoubleAttackSpell extends Spell {

	public static SpellDesc create() {
		return create(null);
	}

	public static SpellDesc create(EntityReference target) {
		Map<SpellArg, Object> arguments = new SpellDesc(DoubleAttackSpell.class);
		arguments.put(SpellArg.TARGET, target);
		return new SpellDesc(arguments);
	}

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		Actor targetActor = (Actor) target;
		targetActor.modifyAttribute(Attribute.ATTACK_BONUS, targetActor.getAttributeValue(Attribute.ATTACK) + targetActor.getAttributeValue(Attribute.ATTACK_BONUS));
		targetActor.modifyAttribute(Attribute.TEMPORARY_ATTACK_BONUS, targetActor.getAttributeValue(Attribute.TEMPORARY_ATTACK_BONUS));
	}
}
