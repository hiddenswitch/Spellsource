package net.demilich.metastone.game.spells;

import java.util.Map;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Destroys all the secrets belonging to {@link SpellArg#TARGET_PLAYER}.
 */
public class DestroySecretsSpell extends Spell {
	public static Logger logger = LoggerFactory.getLogger(DestroySecretsSpell.class);

	public static SpellDesc create(TargetPlayer targetPlayer) {
		Map<SpellArg, Object> arguments = new SpellDesc(DestroySecretsSpell.class);
		arguments.put(SpellArg.TARGET_PLAYER, targetPlayer);
		arguments.put(SpellArg.TARGET, EntityReference.NONE);
		return new SpellDesc(arguments);
	}

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		checkArguments(logger, context, source, desc);
		context.getLogic().removeSecrets(player);
	}

}
