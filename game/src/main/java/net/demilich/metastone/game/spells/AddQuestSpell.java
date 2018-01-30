package net.demilich.metastone.game.spells;

import java.util.Map;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.trigger.EventTrigger;
import net.demilich.metastone.game.spells.trigger.secrets.Quest;
import net.demilich.metastone.game.targeting.EntityReference;

public class AddQuestSpell extends Spell {

	public static SpellDesc create(Quest quest) {
		return create (EntityReference.FRIENDLY_PLAYER, quest);
	}

	public static SpellDesc create(EntityReference target, Quest quest) {
		Map<SpellArg, Object> arguments = SpellDesc.build(AddQuestSpell.class);
		arguments.put(SpellArg.QUEST, quest);
		arguments.put(SpellArg.TARGET, target);
		return new SpellDesc(arguments);
	}

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		Quest quest = ((Quest) desc.get(SpellArg.QUEST)).clone();
		if (quest.getSourceCard() == null) {
		    quest.setSourceCard(source.getSourceCard());
        }
		context.getLogic().playQuest(player, quest.clone());
	}

}
