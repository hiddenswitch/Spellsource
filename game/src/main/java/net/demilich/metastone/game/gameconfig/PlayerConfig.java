package net.demilich.metastone.game.gameconfig;

import com.fasterxml.jackson.annotation.JsonIgnore;
import net.demilich.metastone.game.behaviour.Behaviour;
import net.demilich.metastone.game.behaviour.DoNothingBehaviour;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.decks.Deck;
import net.demilich.metastone.game.entities.heroes.HeroClass;

import java.io.Serializable;

public class PlayerConfig implements Cloneable, Serializable {

	private String name;
	private Card heroCard;
	private Deck deck;
	private Behaviour behaviour;

	private Deck deckForPlay;
	private Card heroForPlay;

	public PlayerConfig() {
	}

	public PlayerConfig(Deck deck, Behaviour behaviour) {
		this.setDeck(deck);
		this.setBehaviour(behaviour);
	}

	@JsonIgnore
	public Behaviour getBehaviour() {
		return behaviour;
	}

	public Deck getDeck() {
		return deck;
	}

	public Deck getDeckForPlay() {
		return deckForPlay;
	}

	public Card getHeroCard() {
		return heroCard;
	}

	public Card getHeroForPlay() {
		if (getDeck().getHeroCard() != null) {
			return getDeck().getHeroCard();
		}
		return heroForPlay;
	}

	public String getName() {
		return name != null ? name : getHeroCard().getName();
	}

	public void setBehaviour(Behaviour behaviour) {
		this.behaviour = behaviour;
	}

	public void setDeck(Deck deck) {
		this.deck = deck;
	}

	public void setHeroCard(Card HeroCard) {
		this.heroCard = HeroCard;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("[PlayerConfig]\n");
		builder.append("name: " + getName() + "\n");
		builder.append("deck: ");
		builder.append(getDeck().toString());
		builder.append("\n");
		return builder.toString();
	}

	public static PlayerConfig fromDeck(Deck deck) {
		PlayerConfig config = new PlayerConfig();
		config.setBehaviour(new DoNothingBehaviour());
		config.setName(deck.getName());
		config.setHeroCard(deck.getHeroCard());
		config.setDeck(deck);
		return config;
	}

	@Override
	protected PlayerConfig clone() throws CloneNotSupportedException {
		PlayerConfig clone = (PlayerConfig) super.clone();
		if (getDeck() != null) {
			clone.setDeck(getDeck().clone());
		}
		if (getBehaviour() != null) {
			clone.setBehaviour(getBehaviour().clone());
		}
		if (getHeroCard() != null) {
			clone.setHeroCard((Card) getHeroCard().clone());
		}
		return clone;
	}

	public void setDeckForPlay(Deck deckForPlay) {
		this.deckForPlay = deckForPlay;
	}

	public void setHeroForPlay(Card heroForPlay) {
		this.heroForPlay = heroForPlay;
	}
}
