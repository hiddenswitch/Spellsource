package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.SecretPlayedEvent;
import net.demilich.metastone.game.events.SecretRevealedEvent;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.trigger.secrets.Secret;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Triggers a specific secret, either from the {@link SpellArg#CARD} if specified or the {@code target} card.
 */
public class TriggerSecretSpell extends Spell {

	private static final long serialVersionUID = 7437795153420236815L;
	private static Logger logger = LoggerFactory.getLogger(TriggerSecretSpell.class);

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		Card card = SpellUtils.getCard(context, desc);
		if (card == null) {
			card = (Card) target;
		}
		if (card.isSecret()) {
			SpellDesc secretSpell =
					AddSecretSpell.class.isAssignableFrom(card.getSpell().getDescClass())
							? ((Secret) (card.getSpell().get(SpellArg.SECRET))).getSpell().clone()
							: card.getSpell().clone();

			context.fireGameEvent(new SecretPlayedEvent(context, player.getId(), card));
			SpellUtils.castChildSpell(context, player, secretSpell, card, target);
			context.fireGameEvent(new SecretRevealedEvent(context, card, player.getId()));
		} else {
			logger.warn("onCast {} {}: Targeting {} which is not a secret", context.getGameId(), source, card);
		}
	}

}
