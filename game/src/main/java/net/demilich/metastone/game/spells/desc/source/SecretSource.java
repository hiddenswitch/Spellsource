package net.demilich.metastone.game.spells.desc.source;

import com.google.common.collect.Sets;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.cards.SecretCard;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.heroes.HeroClass;

public class SecretSource extends CardSource implements HasCardCreationSideEffects {

	public SecretSource(SourceDesc desc) {
		super(desc);
	}

	@Override
	protected CardList match(GameContext context, Entity source, Player player) {
		// If the player doesn't ordinarily have secrets, return GOLD secrets
		final HeroClass playerClass = player.getHero().getHeroClass();
		final HeroClass heroClass =
				Sets.newHashSet(HeroClass.BLACK, HeroClass.BLUE, HeroClass.GOLD, HeroClass.GREEN)
						.contains(playerClass) ?
						playerClass :
						HeroClass.GOLD;
		return CardCatalogue.query(context.getDeckFormat())
				.filtered(c -> c.getHeroClass() == heroClass && c instanceof SecretCard);
	}
}
