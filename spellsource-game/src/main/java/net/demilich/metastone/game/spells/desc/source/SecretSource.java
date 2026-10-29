package net.demilich.metastone.game.spells.desc.source;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.Default;

/**
 * Returns a list of {@link net.demilich.metastone.game.spells.trigger.secrets.Secret} cards from the controller's hero
 * class or the {@link CardSourceArg#HERO_CLASS} if this controller ordinarily does not have secrets.
 */
public class SecretSource extends CardSource implements HasCardCreationSideEffects {

	public SecretSource(CardSourceDesc desc) {
		super(desc);
	}

	protected CardList match(GameContext context, Entity source, Player player, @Default(string = "GOLD") String heroClass) {
		// If the player doesn't ordinarily have secrets, return GOLD or otherwise specified secrets
		String playerHeroClass = player.getHero().getHeroClass();
		CardList secretCards = context.getCardCatalogue().query(context.getDeckFormat())
				.filtered(c -> c.hasHeroClass(playerHeroClass) && c.isSecret() && c.isCollectible());
		if (secretCards.isEmpty()) {
			secretCards = context.getCardCatalogue().query(context.getDeckFormat())
					.filtered(c -> c.hasHeroClass(heroClass) && c.isSecret() && c.isCollectible());
		}
		return secretCards;
	}
}
