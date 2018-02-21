package net.demilich.metastone.game.spells.desc.source;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.CardArrayList;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.EntityType;

import java.io.Serializable;
import java.util.Objects;

@Deprecated
public class GraveyardMinionsSource extends GraveyardActorsSource {
	public GraveyardMinionsSource(SourceDesc desc) {
		super(desc);
	}
}

