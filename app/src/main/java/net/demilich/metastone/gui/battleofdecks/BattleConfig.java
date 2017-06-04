package net.demilich.metastone.gui.battleofdecks;

import java.util.Collection;

import net.demilich.metastone.game.behaviour.Behaviour;
import net.demilich.metastone.game.decks.Deck;

public class BattleConfig {

	private final int numberOfGames;
	private final Behaviour behaviour;
	private final Collection<Deck> decks;

	public BattleConfig(int numberOfGames, Behaviour behaviour, Collection<Deck> decks) {
		this.numberOfGames = numberOfGames;
		this.behaviour = behaviour;
		this.decks = decks;
	}

	public Behaviour getBehaviour() {
		return behaviour;
	}

	public Collection<Deck> getDecks() {
		return decks;
	}

	public int getNumberOfGames() {
		return numberOfGames;
	}

}
