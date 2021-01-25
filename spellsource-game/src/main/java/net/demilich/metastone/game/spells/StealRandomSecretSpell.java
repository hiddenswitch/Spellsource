package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.trigger.Trigger;
import net.demilich.metastone.game.spells.trigger.secrets.Secret;
import net.demilich.metastone.game.targeting.EntityReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Steals a random secret belonging to the opponent of {@link SpellArg#TARGET_PLAYER}.
 * <p>
 * To steal one of the opponent's secrets:
 * <pre>
 *   {
 *     "class": "StealRandomSecretSpell"
 *   }
 * </pre>
 * To give the opponent one of the casting player's secrets:
 * <pre>
 *   {
 *     "class": "StealRandomSecretSpell",
 *     "targetPlayer": "OPPONENT"
 *   }
 * </pre>
 */
public final class StealRandomSecretSpell extends Spell {

	public static SpellDesc create() {
		Map<SpellArg, Object> arguments = new SpellDesc(StealRandomSecretSpell.class);
		arguments.put(SpellArg.TARGET, EntityReference.NONE);
		return new SpellDesc(arguments);
	}

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		Player opponent = context.getOpponent(player);
		List<Secret> secrets = opponent.getSecrets();

		if (secrets.isEmpty()) {
			return;
		}

		// try to steal a secret which we do not own yet
		List<Secret> validSecrets = new ArrayList<>();
		for (Trigger trigger : secrets) {
			Secret secret = (Secret) trigger;
			if (!player.getSecretCardIds().contains(secret.getSourceCard().getCardId()) && player.getSecrets().size() < GameLogic.MAX_SECRETS) {
				validSecrets.add(secret);
			}
		}

		if (!validSecrets.isEmpty()) {
			Secret secret = context.getLogic().getRandom(validSecrets);
			opponent.getSecrets().remove(secret);
			secret.setHostReference(player.getHero().getReference());
			secret.setOwner(player.getId());
			player.getSecrets().add(secret);
		} else {
			// no valid secret to steal; instead destroy one for the opponent at
			// least
			Secret secret = context.getLogic().getRandom(secrets);
			opponent.getSecrets().remove(secret);
			context.getLogic().expire(secret);
		}
	}

}
