package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.SecretCard;
import net.demilich.metastone.game.cards.SpellCard;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.SecretPlayedEvent;
import net.demilich.metastone.game.events.SecretRevealedEvent;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.trigger.secrets.Secret;

public class TriggerSecretSpell extends Spell {

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		Card card = SpellUtils.getCard(context, desc);
		if (card == null) {
			card = (Card) target;
		}
		if (card instanceof SecretCard) {
			SecretCard secretCard = (SecretCard) card;
			SpellDesc secretSpell =
					AddSecretSpell.class.isAssignableFrom(secretCard.getSpell().getSpellClass())
							? ((Secret) (secretCard.getSpell().get(SpellArg.SECRET))).getSpell().clone()
							: secretCard.getSpell().clone();

			context.fireGameEvent(new SecretPlayedEvent(context, player.getId(), secretCard));
			SpellUtils.castChildSpell(context, player, secretSpell, secretCard, target);
			context.fireGameEvent(new SecretRevealedEvent(context, secretCard, player.getId()));
		}
	}

}
