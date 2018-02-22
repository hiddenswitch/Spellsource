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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adds the specified {@link SpellArg#QUEST} for the specified {@link SpellArg#TARGET_PLAYER}.
 * <p>
 * For <b>example</b>, a spell may create a quest for both players that reads, "After your characters have been damaged
 * 10 times, lose the game."
 * <p>
 * <pre>
 *     {
 *         "class": "AddQuestSpell",
 *         "targetPlayer": "BOTH",
 *         "quest": {
 *              "countUntilCast": 10,
 *              "eventTrigger": {
 *                  "class": "DamageReceivedTrigger",
 *                  "targetPlayer": "SELF"
 *              },
 *              "spell": {
 *                  "class": "DestroySpell",
 *                  "target": "FRIENDLY_HERO"
 *              },
 *              "maxFires": 10
 *          }
 *     }
 * </pre>
 *
 * @see Quest for more about quests and the format for specifying them.
 */
public class AddQuestSpell extends Spell {

	private static Logger logger = LoggerFactory.getLogger(AddQuestSpell.class);

	/**
	 * Creates this spell for the casting player and the specified {@link Quest}.
	 *
	 * @param quest The quest object to use for this spell.
	 * @return The spell
	 */
	public static SpellDesc create(Quest quest) {
		return create(TargetPlayer.SELF, quest);
	}

	/**
	 * Creates this spell for the specified {@link TargetPlayer} and {@link Quest}.
	 *
	 * @param target The {@link TargetPlayer} interpreted from the caster's point of view.
	 * @param quest  The quest.
	 * @return The spell
	 */
	public static SpellDesc create(TargetPlayer target, Quest quest) {
		Map<SpellArg, Object> arguments = SpellDesc.build(AddQuestSpell.class);
		arguments.put(SpellArg.QUEST, quest);
		arguments.put(SpellArg.TARGET_PLAYER, target);
		return new SpellDesc(arguments);
	}

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		final Object questObject = desc.get(SpellArg.QUEST);
		if (questObject == null) {
			logger.error("onCast {} {}: The specified QUEST argument is null", context.getGameId(), source);
			return;
		}

		if (!(questObject instanceof Quest)) {
			logger.error("onCast {} {}: The specified QUEST argument {} is not a Quest object, it is a {}", context.getGameId(), source, questObject, questObject.getClass());
			return;
		}

		Quest quest = ((Quest) questObject).clone();
		if (quest.getSourceCard() == null) {
			quest.setSourceCard(source.getSourceCard());
		}

		context.getLogic().playQuest(player, quest.clone());
	}

}
