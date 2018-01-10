package net.demilich.metastone.game.spells;

import java.util.Map;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.aura.Aura;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.aura.AuraDesc;
import net.demilich.metastone.game.spells.desc.trigger.TriggerDesc;
import net.demilich.metastone.game.spells.trigger.Enchantment;
import net.demilich.metastone.game.targeting.EntityReference;

public class AddEnchantmentSpell extends Spell {

	public static SpellDesc create(EntityReference target, TriggerDesc trigger) {
		Map<SpellArg, Object> arguments = SpellDesc.build(AddEnchantmentSpell.class);
		arguments.put(SpellArg.TRIGGER, trigger);
		arguments.put(SpellArg.TARGET, target);
		return new SpellDesc(arguments);
	}

	public static SpellDesc create(EntityReference target, Aura aura) {
		Map<SpellArg, Object> arguments = SpellDesc.build(AddEnchantmentSpell.class);
		arguments.put(SpellArg.AURA, aura);
		arguments.put(SpellArg.TARGET, target);
		return new SpellDesc(arguments);
	}

	public static SpellDesc create(TriggerDesc trigger) {
		return create(null, trigger);
	}

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		TriggerDesc triggerDesc = (TriggerDesc) desc.get(SpellArg.TRIGGER);
		Aura aura = (Aura) desc.get(SpellArg.AURA);

		if (triggerDesc != null) {
			Enchantment enchantment = triggerDesc.create();
			context.getLogic().addGameEventListener(player, enchantment, target);
		}

		if (aura != null) {
			aura = aura.clone();
			aura.setOwner(player.getId());
			context.getLogic().addGameEventListener(player, aura, target);
		}
	}

}
