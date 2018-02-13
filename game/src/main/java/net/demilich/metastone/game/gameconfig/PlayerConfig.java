package net.demilich.metastone.game.gameconfig;

import net.demilich.metastone.game.behaviour.Behaviour;
import net.demilich.metastone.game.behaviour.DoNothingBehaviour;
import net.demilich.metastone.game.behaviour.PlayRandomBehaviour;
import net.demilich.metastone.game.cards.HeroCard;
import net.demilich.metastone.game.decks.Deck;
import net.demilich.metastone.game.decks.MetaDeck;
import net.demilich.metastone.game.decks.RandomDeck;
import net.demilich.metastone.game.entities.heroes.MetaHero;

import java.io.Serializable;

public class PlayerConfig implements Cloneable, Serializable {

	private String name;
	private HeroCard heroCard;
	private Deck deck;
	private Behaviour behaviour;

	private Deck deckForPlay;
	private HeroCard heroForPlay;

	public PlayerConfig() {
	}

	public PlayerConfig(Deck deck, Behaviour behaviour) {
		this.deck = deck;
		this.behaviour = behaviour;
	}

	public void build() {
		if (deck instanceof MetaDeck) {
			MetaDeck metaDeck = (MetaDeck) deck;
			deckForPlay = metaDeck.selectRandom();

			heroForPlay = MetaHero.getHeroCard(deckForPlay.getHeroClass());
		} else {
			deckForPlay = deck;
			if (heroCard == null) {
				heroCard = MetaHero.getHeroCard(deckForPlay.getHeroClass());
			}
			heroForPlay = heroCard;
		}
	}

	public Behaviour getBehaviour() {
		return behaviour;
	}

	public Deck getDeck() {
		return deck;
	}

	public Deck getDeckForPlay() {
		return deckForPlay;
	}

	public HeroCard getHeroCard() {
		return heroCard;
	}

	public HeroCard getHeroForPlay() {
		if (deck.getHeroCard() != null) {
			return deck.getHeroCard();
		}
		return heroForPlay;
	}

	public String getName() {
		return name != null ? name : heroCard.getName();
	}

	public void setBehaviour(Behaviour behaviour) {
		this.behaviour = behaviour;
	}

	public void setDeck(Deck deck) {
		this.deck = deck;
	}

	public void setHeroCard(HeroCard HeroCard) {
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
		if (deck != null) {
			clone.setDeck(deck.clone());
		}
		if (behaviour != null) {
			clone.setBehaviour(behaviour.clone());
		}
		if (heroCard != null) {
			clone.setHeroCard((HeroCard) heroCard.clone());
		}
		return clone;
	}
}
