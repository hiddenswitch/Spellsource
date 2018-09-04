package net.demilich.metastone.game.spells.custom;

import com.github.fromage.quasi.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.CardType;
import net.demilich.metastone.game.cards.costmodifier.CardCostModifier;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.minions.Race;
import net.demilich.metastone.game.spells.*;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.filter.CardFilter;
import net.demilich.metastone.game.spells.desc.trigger.EnchantmentDesc;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerArg;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;
import net.demilich.metastone.game.spells.trigger.BeforeMinionSummonedTrigger;
import net.demilich.metastone.game.spells.trigger.Trigger;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.utils.Attribute;

import java.util.List;
import java.util.stream.Stream;

public class CopyEnchantmentsSpell extends Spell {

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity host, Entity target) {
		List<Entity> copyFrom = context.resolveTarget(player, host, desc.getSecondaryTarget());

		for (Entity originSource : copyFrom) {
			if (originSource instanceof Actor) {
				Actor source = (Actor) originSource;
				Actor actorTarget = (Actor) target;
				// Copy deathrattle
				for (SpellDesc deathrattle : source.getDeathrattles()) {
					actorTarget.addDeathrattle(deathrattle.clone());
				}
			}

			// Copy enchantments
			List<Trigger> triggers = context.getTriggersAssociatedWith(originSource.getReference());
			for (Trigger trigger : triggers) {
				Trigger cloned = trigger.clone();
				cloned.setHost(target);
				cloned.setOwner(target.getOwner());
				context.getLogic().addGameEventListener(player, cloned, target);
			}

			// Copy attributes that aren't present on the card's text (?)
			for (Attribute attr : new Attribute[]{Attribute.ATTACK_BONUS, Attribute.HP_BONUS}) {
				target.setAttribute(attr, originSource.getAttributeValue(attr));
			}
		}
	}
}
