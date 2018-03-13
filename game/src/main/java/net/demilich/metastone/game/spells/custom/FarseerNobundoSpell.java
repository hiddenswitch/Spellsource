package net.demilich.metastone.game.spells.custom;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.CardType;
import net.demilich.metastone.game.cards.MinionCard;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.minions.Race;
import net.demilich.metastone.game.spells.AddEnchantmentSpell;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.SpellUtils;
import net.demilich.metastone.game.spells.TargetPlayer;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.filter.CardFilter;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerArg;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;
import net.demilich.metastone.game.spells.desc.trigger.TriggerDesc;
import net.demilich.metastone.game.spells.trigger.BeforeMinionSummonedTrigger;
import net.demilich.metastone.game.targeting.EntityReference;

import java.util.stream.Stream;

public class FarseerNobundoSpell extends Spell {

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity spellSource, Entity spellTarget) {
		SpellDesc existingTotems = AddActorEffectsToTargetActorSpell.create(spellTarget.getReference());
		existingTotems.put(SpellArg.FILTER, CardFilter.create(CardType.MINION, Race.TOTEM));
		existingTotems.put(SpellArg.TARGET, EntityReference.OTHER_FRIENDLY_MINIONS);

		TriggerDesc trigger = new TriggerDesc();
		trigger.eventTrigger = new EventTriggerDesc(BeforeMinionSummonedTrigger.class);
		trigger.eventTrigger.put(EventTriggerArg.TARGET_PLAYER, TargetPlayer.SELF);
		trigger.eventTrigger.put(EventTriggerArg.RACE, Race.TOTEM);

		trigger.spell = new SpellDesc(AddActorEffectsToTargetActorSpell.create(spellTarget.getReference(), EntityReference.EVENT_TARGET));
		SpellDesc newTotems = AddEnchantmentSpell.create(EntityReference.FRIENDLY_PLAYER, trigger);

		Stream.of(existingTotems, newTotems)
				.forEach(spell -> SpellUtils.castChildSpell(context, player, spell, spellSource, spellTarget));
	}
}

