package net.demilich.metastone.game.spells.desc.source;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.heroes.HeroClass;

public class SecretSource extends CardSource implements HasCardCreationSideEffects {

	private static final long serialVersionUID = 4499722668264597899L;

	public SecretSource(CardSourceDesc desc) {
		super(desc);
	}

	@Override
	protected CardList match(GameContext context, Entity source, Player player) {
		HeroClass defaultHeroClass = (HeroClass) getDesc().getOrDefault(CardSourceArg.HERO_CLASS, HeroClass.GOLD);
		// If the player doesn't ordinarily have secrets, return GOLD or otherwise specified secrets
		CardList secretCards = CardCatalogue.query(context.getDeckFormat())
				.filtered(c -> c.getHeroClass() == player.getHero().getHeroClass() && c.isSecret() && c.isCollectible());
		if (secretCards.isEmpty()) {
			secretCards = CardCatalogue.query(context.getDeckFormat())
					.filtered(c -> c.getHeroClass() == defaultHeroClass && c.isSecret() && c.isCollectible());
		}
		return secretCards;
	}
}
