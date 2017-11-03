package net.demilich.metastone.game.spells.custom;

import java.util.Map;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.utils.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.MindControlSpell;
import net.demilich.metastone.game.spells.TargetPlayer;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;
import net.demilich.metastone.game.spells.trigger.EventTrigger;
import net.demilich.metastone.game.spells.trigger.SilenceTrigger;
import net.demilich.metastone.game.spells.trigger.Enchantment;
import net.demilich.metastone.game.spells.trigger.TurnEndTrigger;
import net.demilich.metastone.game.targeting.EntityReference;

public class MindControlOneTurnSpell extends MindControlSpell {

	public static SpellDesc create() {
		return create(null);
	}

	public static SpellDesc create(EntityReference target) {
		Map<SpellArg, Object> arguments = SpellDesc.build(MindControlOneTurnSpell.class);
		arguments.put(SpellArg.TARGET, target);
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

		// minion should be able to attack this turn
		target.getAttributes().remove(Attribute.SUMMONING_SICKNESS);
		context.getLogic().refreshAttacksPerRound((Actor) target);

		// mind control is terminated either when silenced or turn ends
		SpellDesc reverseMindcontrolSpell = MindControlSpell.create(EntityReference.SELF, TargetPlayer.OPPONENT, false);
		EventTrigger silenceTrigger = new SilenceTrigger(EventTriggerDesc.createEmpty(SilenceTrigger.class));
		EventTrigger turnEndTrigger = new TurnEndTrigger(EventTriggerDesc.createEmpty(TurnEndTrigger.class));
		turnEndTrigger.setOwner(player.getId());
		Enchantment returnOnSilence = new Enchantment(silenceTrigger, turnEndTrigger, reverseMindcontrolSpell, true);
		context.getLogic().addGameEventListener(player, returnOnSilence, target);
	}

}