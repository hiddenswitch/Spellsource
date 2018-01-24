package net.demilich.metastone.game.spells;

import java.util.Map;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.cards.ActorCard;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.utils.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;

public class SetHpSpell extends Spell {

	public static SpellDesc create(EntityReference target, int hp) {
		Map<SpellArg, Object> arguments = SpellDesc.build(SetHpSpell.class);
		arguments.put(SpellArg.VALUE, hp);
		arguments.put(SpellArg.TARGET, target);
		return new SpellDesc(arguments);
	}

	public static SpellDesc create(int hp) {
		return create(null, hp);
	}

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		int hp = desc.getValue(SpellArg.VALUE, context, player, target, source, 0);
		target.getAttributes().remove(Attribute.HP_BONUS);
		// When exclusive, the set hp spell will overwrite bonuses. When not exclusive, the BASE HP will change
		// (to protect it from silencing) and the changed HP will honor bonuses.
		boolean exclusive = (boolean) desc.getOrDefault(SpellArg.EXCLUSIVE, true);

		if (target instanceof Actor) {
			context.getLogic().setHpAndMaxHp((Actor) target, hp);
		} else if (target instanceof ActorCard) {
			target.getAttributes().put(Attribute.HP, hp);
		}

		if (!exclusive) {
			target.setAttribute(Attribute.BASE_HP, hp);
		}
	}

}
