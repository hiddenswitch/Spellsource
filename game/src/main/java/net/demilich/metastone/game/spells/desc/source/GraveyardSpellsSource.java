package net.demilich.metastone.game.spells.desc.source;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.CardArrayList;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.cards.CardType;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.EntityType;

import java.io.Serializable;
import java.util.Objects;

public class GraveyardSpellsSource extends CardSource implements Serializable {

	public GraveyardSpellsSource(SourceDesc desc) {
		super(desc);
	}

	@Override
	protected CardList match(GameContext context, Player player) {
		CardList graveyardSpells = new CardArrayList();
		player.getGraveyard()
				.stream()
				.filter(c -> c.getEntityType() == EntityType.CARD)
				.map(Entity::getSourceCard)
				.filter(Objects::nonNull)
				.filter(c -> c.getCardType() == CardType.SPELL)
				.forEach(graveyardSpells::addCard);
		return graveyardSpells.getCopy();
	}

}
