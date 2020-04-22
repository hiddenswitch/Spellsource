package net.demilich.metastone.game.spells.custom;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.*;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;
import net.demilich.metastone.game.spells.trigger.Enchantment;
import net.demilich.metastone.game.spells.trigger.EventTrigger;
import net.demilich.metastone.game.spells.trigger.SilenceTrigger;
import net.demilich.metastone.game.spells.trigger.TurnEndTrigger;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.cards.Attribute;

import java.util.Map;

public class MindControlOneTurnSpell extends MindControlSpell {

	public static SpellDesc create() {
		return create(null);
	}

	public static SpellDesc create(EntityReference target) {
		Map<SpellArg, Object> arguments = new SpellDesc(MindControlOneTurnSpell.class);
		if (target != null) {
			arguments.put(SpellArg.TARGET, target);
		}
		return new SpellDesc(arguments);
	}

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		// mind control minion
		super.onCast(context, player, desc, source, target);

		if (target.isDestroyed()) {
			return;
		}

		SpellDesc effectSpell = (SpellDesc) desc.get(SpellArg.SPELL);
		if (effectSpell == null) {
			// Minion should be able to attack this turn
			effectSpell = SpellDesc.join(
					RemoveAttributeSpell.create(target.getReference(), Attribute.SUMMONING_SICKNESS),
					RefreshAttacksSpell.create(target.getReference()));
		}


		// mind control is terminated either when silenced or turn ends
		EventTrigger silenceTrigger = new SilenceTrigger(new EventTriggerDesc(SilenceTrigger.class));
		SpellDesc reverseMindcontrolSpell = MindControlSpell.create(EntityReference.SELF, TargetPlayer.OPPONENT, false);
		EventTriggerDesc revertTrigger = (EventTriggerDesc) desc.get(SpellArg.REVERT_TRIGGER);
		if (revertTrigger == null) {
			revertTrigger = TurnEndTrigger.create(TargetPlayer.BOTH);
		}

		EventTrigger eventTrigger = revertTrigger.create();
		Enchantment returnOnSilence = new Enchantment();
		returnOnSilence.getTriggers().add(silenceTrigger);
		returnOnSilence.getTriggers().add(eventTrigger);
		returnOnSilence.setSpell(reverseMindcontrolSpell);
		returnOnSilence.setOneTurn(true);
		returnOnSilence.setMaxFires(1);
		returnOnSilence.setUsesSpellTrigger(true);

		context.getLogic().addEnchantment(player, returnOnSilence, source, target);

		// Apply the effects
		SpellUtils.castChildSpell(context, player, effectSpell, source, target);
	}

}