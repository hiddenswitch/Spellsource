package net.demilich.metastone.game.spells.desc.source;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.Entity;

/**
 * Returns a list of {@link net.demilich.metastone.game.spells.trigger.secrets.Secret} cards from the controller's hero
 * class or the {@link CardSourceArg#HERO_CLASS} if this controller ordinarily does not have secrets.
 */
public class SecretSource extends CardSource implements HasCardCreationSideEffects {

	public SecretSource(CardSourceDesc desc) {
		super(desc);
	}

	@Override
	protected CardList match(GameContext context, Entity source, Player player) {
		String defaultHeroClass = (String) getDesc().getOrDefault(CardSourceArg.HERO_CLASS, "GOLD");
		// If the player doesn't ordinarily have secrets, return GOLD or otherwise specified secrets
		CardList secretCards = context.getCardCatalogue().query(context.getDeckFormat())
				.filtered(c -> c.getHeroClass().equals(player.getHero().getHeroClass()) && c.isSecret() && c.isCollectible());
		if (secretCards.isEmpty()) {
			secretCards = context.getCardCatalogue().query(context.getDeckFormat())
					.filtered(c -> c.getHeroClass().equals(defaultHeroClass) && c.isSecret() && c.isCollectible());
		}
		return secretCards;
	}
}
