package net.demilich.metastone.game.spells;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.trigger.secrets.Secret;
import net.demilich.metastone.game.targeting.EntityReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Puts the specified {@link SpellArg#SECRET} (an {@link net.demilich.metastone.game.spells.desc.trigger.EnchantmentDesc})
 * into play.
 * <p>
 * If the secret is a card, use {@link CastCardsSpell}.
 */
public class AddSecretSpell extends Spell {
	private static Logger logger = LoggerFactory.getLogger(AddSecretSpell.class);

	public static SpellDesc create(Secret secret) {
		return create(EntityReference.FRIENDLY_PLAYER, secret);
	}

	public static SpellDesc create(EntityReference target, Secret secret) {
		Map<SpellArg, Object> arguments = new SpellDesc(AddSecretSpell.class);
		arguments.put(SpellArg.SECRET, secret);
		arguments.put(SpellArg.TARGET, target);
		return new SpellDesc(arguments);
	}

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		checkArguments(logger, context, source, desc, SpellArg.ATTRIBUTE, SpellArg.SECRET);
		Secret secret = ((Secret) desc.get(SpellArg.SECRET)).clone();
		if (secret.getSourceCard() == null) {
			secret.setSourceCard(source.getSourceCard());
		}
		if (context.getLogic().canPlaySecret(player, secret.getSourceCard())) {
			context.getLogic().playSecret(player, secret);
		}
	}
}
