package net.demilich.metastone.game.spells.trigger;

import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.GameEvent;
;
import net.demilich.metastone.game.spells.TargetPlayer;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerArg;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;

import java.util.Map;

/**
 * Fires before the game starts but after mulligans. Appropriate for putting passives into play.
 * <p>
 * For example, to implement the passive "Passive: Your starting skill is Summon a 1/1 Beast":
 * <pre>
 *   "gameTriggers": [
 *     {
 *       "eventTrigger": {
 *         "class": "PreGameStartTrigger",
 *         "targetPlayer": "SELF"
 *       },
 *       "spell": {
 *         "class": "MetaSpell",
 *         "spells": [
 *           {
 *             "class": "RevealCardSpell",
 *             "target": "SELF"
 *           },
 *           {
 *             "class": "ChangeHeroPowerSpell",
 *             "target": "FRIENDLY_PLAYER",
 *             "card": "hero_power_dire_beast"
 *           },
 *           {
 *             "class": "RemoveCardSpell",
 *             "target": "SELF"
 *           }
 *         ]
 *       }
 *     }
 *   ]
 * </pre>
 * Observe the card is revealed, followed by the actual effect, followed by removing itself.
 * <p>
 * To ensure the card isn't mulliganed, use {@link net.demilich.metastone.game.cards.Attribute#NEVER_MULLIGANS}.
 */
public class PreGameStartTrigger extends EventTrigger {
	public static EventTriggerDesc create(TargetPlayer targetPlayer) {
		Map<EventTriggerArg, Object> arguments = new EventTriggerDesc(PreGameStartTrigger.class);
		arguments.put(EventTriggerArg.TARGET_PLAYER, targetPlayer);
		return new EventTriggerDesc(arguments);
	}

	public PreGameStartTrigger() {
		this(new EventTriggerDesc(PreGameStartTrigger.class));
	}

	public PreGameStartTrigger(EventTriggerDesc desc) {
		super(desc);
	}

	@Override
	protected boolean innerQueues(GameEvent event, Enchantment enchantment, Entity host) {
		return true;
	}

	@Override
	public com.hiddenswitch.spellsource.rpc.Spellsource.GameEventTypeMessage.GameEventType interestedIn() {
		return com.hiddenswitch.spellsource.rpc.Spellsource.GameEventTypeMessage.GameEventType.PRE_GAME_START;
	}

}
