package net.demilich.metastone.game.cards;

import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.desc.SecretCardDesc;
import net.demilich.metastone.game.spells.AddSecretSpell;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.trigger.EventTrigger;
import net.demilich.metastone.game.spells.trigger.secrets.Secret;
import net.demilich.metastone.game.targeting.TargetSelection;

public class SecretCard extends SpellCard {

	public SecretCard(SecretCardDesc desc) {
		super(desc);
		EventTrigger trigger = desc.trigger.create();
		setSecret(new Secret(trigger, desc.spell, this));
		setAttribute(Attribute.SECRET);
	}

	/**
	 * Determines whether or not this secret can be cast.
	 *
	 * @param context The context to use.
	 * @param player  The player.
	 * @return {@code true} if the secret can be cast.
	 * @see net.demilich.metastone.game.logic.GameLogic#canPlaySecret(Player, SecretCard) for complete rules on playing
	 * secrets.
	 */
	public boolean canBeCast(GameContext context, Player player) {
		return context.getLogic().canPlaySecret(player, this);
	}

	public void setSecret(Secret secret) {
		SpellDesc spell = AddSecretSpell.create(secret);
		setTargetRequirement(TargetSelection.NONE);
		setSpell(spell);
	}

}
