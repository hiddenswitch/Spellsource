package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.cards.Attribute;

import java.util.Map;

/**
 * Removes the {@link SpellArg#ATTRIBUTE} from the specified {@code target}.
 * <p>
 * The removal can be reverted by specifying a {@link SpellArg#REVERT_TRIGGER} {@link
 * net.demilich.metastone.game.spells.trigger.EventTrigger}.
 */
public class RemoveAttributeSpell extends RevertableSpell {
	public static SpellDesc create(Attribute tag) {
		return create(null, tag);
	}

	public static SpellDesc create(EntityReference target, Attribute tag) {
		Map<SpellArg, Object> arguments = new SpellDesc(RemoveAttributeSpell.class);
		arguments.put(SpellArg.ATTRIBUTE, tag);
		arguments.put(SpellArg.TARGET, target);
		return new SpellDesc(arguments);
	}

	@Override
	protected SpellDesc getReverseSpell(GameContext context, Player player, Entity source, SpellDesc desc, EntityReference target) {
		return AddAttributeSpell.create(target, (Attribute) desc.get(SpellArg.ATTRIBUTE));
	}

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		Attribute tag = (Attribute) desc.get(SpellArg.ATTRIBUTE);
		context.getLogic().removeAttribute(target, null, tag);
		super.onCast(context, player, desc, source, target);
	}
}
