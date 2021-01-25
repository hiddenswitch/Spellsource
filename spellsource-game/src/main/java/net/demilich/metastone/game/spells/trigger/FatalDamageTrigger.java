package net.demilich.metastone.game.spells.trigger;

import net.demilich.metastone.game.entities.Entity;
import com.hiddenswitch.spellsource.rpc.Spellsource.EntityTypeMessage.EntityType;
import net.demilich.metastone.game.entities.heroes.Hero;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.events.PreDamageEvent;
import net.demilich.metastone.game.spells.TargetPlayer;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerArg;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FatalDamageTrigger extends PreDamageTrigger {

	private static Logger logger = LoggerFactory.getLogger(FatalDamageTrigger.class);

	public static EventTriggerDesc create(TargetPlayer damageSourceOwner, TargetPlayer damageVictimOwner, EntityType victimEntityType) {
		EventTriggerDesc desc = new EventTriggerDesc(FatalDamageTrigger.class);
		desc.put(EventTriggerArg.SOURCE_PLAYER, damageSourceOwner);
		desc.put(EventTriggerArg.TARGET_PLAYER, damageVictimOwner);
		desc.put(EventTriggerArg.TARGET_ENTITY_TYPE, victimEntityType);
		return desc;
	}

	public FatalDamageTrigger(EventTriggerDesc desc) {
		super(desc);
	}

	@Override
	protected boolean innerQueues(GameEvent event, Enchantment enchantment, Entity host) {
		if (!super.innerQueues(event, enchantment, host)) {
			return false;
		} else {
			PreDamageEvent preDamageEvent = (PreDamageEvent) event;
			Entity victim = preDamageEvent.getTarget();
			var damage = event.getGameContext().getDamageStack().peek();
			switch (victim.getEntityType()) {
				case HERO:
					Hero hero = (Hero) victim;
					return hero.getEffectiveHp() <= damage;
				case MINION:
					Minion minion = (Minion) victim;
					return minion.getHp() <= damage;
				default:
					logger.warn("Invalid entity type in FatalDamageTrigger: {}", victim);
					break;
			}

		}
		return false;
	}

}
