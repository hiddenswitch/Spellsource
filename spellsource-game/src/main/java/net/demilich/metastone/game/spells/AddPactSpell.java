package net.demilich.metastone.game.spells;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.trigger.EnchantmentDescArg;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerArg;
import net.demilich.metastone.game.spells.trigger.secrets.Quest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Adds the specified {@link SpellArg#PACT} for the specified {@link SpellArg#TARGET_PLAYER}.
 * <p>
 * A pact is a permanent that triggers its written effect when <b>either</b> player performs the action written on the
 * card. Their {@link EnchantmentDescArg#EVENT_TRIGGER} event trigger should have {@link EventTriggerArg#TARGET_PLAYER}
 * set to {@link TargetPlayer#BOTH}.
 * <p>
 * For <b>example</b>, a spell may create a pact for both players that reads, "After your characters have been damaged
 * 10 times, lose the game."
 * <pre>
 *     {
 *         "class": "AddQuestSpell",
 *         "targetPlayer": "BOTH",
 *         "pact": {
 *              "countUntilCast": 10,
 *              "eventTrigger": {
 *                  "class": "DamageReceivedTrigger",
 *                  "targetPlayer": "BOTH"
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
public class AddPactSpell extends AddQuestSpell {

	private static Logger logger = LoggerFactory.getLogger(AddPactSpell.class);

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
	 * @param pact   The pact.
	 * @return The spell
	 */
	public static SpellDesc create(TargetPlayer target, Quest pact) {
		Map<SpellArg, Object> arguments = new SpellDesc(AddPactSpell.class);
		arguments.put(SpellArg.PACT, pact);
		arguments.put(SpellArg.TARGET_PLAYER, target);
		return new SpellDesc(arguments);
	}

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		checkArguments(logger, context, source, desc, SpellArg.PACT);
		final Object pactObject = desc.get(SpellArg.PACT);
		if (pactObject == null) {
			logger.error("onCast {} {}: The specified PACT argument is null", context.getGameId(), source);
			return;
		}

		if (!(pactObject instanceof Quest)) {
			logger.error("onCast {} {}: The specified PACT argument {} is not a Quest object, it is a {}", context.getGameId(), source, pactObject, pactObject.getClass());
			return;
		}

		Quest pact = ((Quest) pactObject).clone();
		pact.setPact(true);
		if (pact.getSourceCard() == null) {
			pact.setSourceCard(source.getSourceCard());
		}

		if (context.getLogic().canPlayPact(player, pact.getSourceCard())) {
			context.getLogic().playPact(player, pact.clone());
		}
	}
}
