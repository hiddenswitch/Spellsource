package net.demilich.metastone.game;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import com.google.gson.annotations.Expose;
import net.demilich.metastone.game.behaviour.DoNothingBehaviour;
import net.demilich.metastone.game.behaviour.IBehaviour;
import net.demilich.metastone.game.behaviour.human.HumanBehaviour;
import net.demilich.metastone.game.cards.CardZone;
import net.demilich.metastone.game.decks.Deck;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.EntityType;
import net.demilich.metastone.game.entities.EntityZone;
import net.demilich.metastone.game.entities.heroes.Hero;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.entities.weapons.Weapon;
import net.demilich.metastone.game.heroes.powers.HeroPower;
import net.demilich.metastone.game.statistics.GameStatistics;
import net.demilich.metastone.game.gameconfig.PlayerConfig;
import net.demilich.metastone.game.targeting.PlayerZones;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Player extends Entity implements Serializable {
	private static final long serialVersionUID = 1L;

	public static Player empty() {
		Player player = new Player();
		PlayerConfig config = new PlayerConfig(Deck.EMPTY, new DoNothingBehaviour());
		player.buildFromConfig(config);
		return player;
	}

	public static Player forUser(String userId, int id) {
		Player player = empty();
		player.setBehaviour(new HumanBehaviour());
		player.setId(id);
		player.setUserId(userId);
		return player;
	}

	protected String deckName;
	protected CardZone deck = new CardZone(getId(), PlayerZones.DECK);
	private CardZone hand = new CardZone(getId(), PlayerZones.HAND);
	private EntityZone<Entity> setAsideZone = new EntityZone<>(getId(), PlayerZones.SET_ASIDE_ZONE);
	private EntityZone<Entity> graveyard = new EntityZone<>(getId(), PlayerZones.GRAVEYARD);
	private EntityZone<Minion> minions = new EntityZone<>(getId(), PlayerZones.BATTLEFIELD);
	private EntityZone<Hero> heroZone = new EntityZone<>(getId(), PlayerZones.HERO);
	private HashSet<String> secrets = new HashSet<>();

	private final GameStatistics statistics = new GameStatistics();

	private int mana;
	private int maxMana;
	private int lockedMana;

	private boolean hideCards;

	@Expose(serialize = false, deserialize = false)
	private IBehaviour behaviour;

	private Player(Player otherPlayer) {
		this.setName(otherPlayer.getName());
		this.deckName = otherPlayer.getDeckName();
		this.getAttributes().putAll(otherPlayer.getAttributes());
		this.secrets.addAll(otherPlayer.secrets);
		this.setId(otherPlayer.getId());
		this.deck = otherPlayer.getDeck().clone();
		this.hand = otherPlayer.getHand().clone();
		this.minions = otherPlayer.getMinions().clone();
		this.graveyard = otherPlayer.getGraveyard().clone();
		this.setAsideZone = otherPlayer.getSetAsideZone().clone();
		this.heroZone = otherPlayer.getHeroZone().clone();
		this.mana = otherPlayer.mana;
		this.maxMana = otherPlayer.maxMana;
		this.lockedMana = otherPlayer.lockedMana;
		this.behaviour = otherPlayer.behaviour;
		this.getStatistics().merge(otherPlayer.getStatistics());
	}

	/**
	 * Use build from config to actually build the class.
	 */
	protected Player() {
	}

	public Player(PlayerConfig config) {
		buildFromConfig(config);
	}

	protected void buildFromConfig(PlayerConfig config) {
		config.build();
		Deck selectedDeck = config.getDeckForPlay();

		//gets overwritten by procedural player with a random deck.
		this.deck = new CardZone(getId(), PlayerZones.DECK, selectedDeck.getCardsCopy());
		this.setHero(config.getHeroForPlay().createHero());
		this.setName(config.getName() + " - " + getHero().getName());
		this.deckName = selectedDeck.getName();
		setBehaviour(config.getBehaviour().clone());
		setHideCards(config.hideCards());
	}

	@Override
	public Player clone() {
		return new Player(this);
	}

	public IBehaviour getBehaviour() {
		return behaviour;
	}

	public List<Actor> getCharacters() {
		List<Actor> characters = new ArrayList<Actor>();
		characters.add(getHero());
		characters.addAll(getMinions());
		return characters;
	}

	public CardZone getDeck() {
		return deck;
	}

	public String getDeckName() {
		return deckName;
	}

	@Override
	public EntityType getEntityType() {
		return EntityType.PLAYER;
	}

	public EntityZone<Entity> getGraveyard() {
		return graveyard;
	}

	public CardZone getHand() {
		return hand;
	}

	public Hero getHero() {
		if (getHeroZone().size() == 0) {
			// Check the graveyard
			Optional<Entity> hero = getGraveyard().stream().filter(e -> e.getEntityType() == EntityType.HERO).findFirst();
			if (hero.isPresent()) {
				hero.get().setAttribute(Attribute.DESTROYED);
				return (Hero) hero.get();
			} else {
				return null;
			}
		} else {
			return getHeroZone().get(0);
		}

	}

	public int getLockedMana() {
		return lockedMana;
	}

	public int getMana() {
		return mana;
	}

	public int getMaxMana() {
		return maxMana;
	}

	public EntityZone<Minion> getMinions() {
		return minions;
	}

	public HashSet<String> getSecrets() {
		return secrets;
	}

	public EntityZone<Entity> getSetAsideZone() {
		return setAsideZone;
	}

	public GameStatistics getStatistics() {
		return statistics;
	}

	public boolean hideCards() {
		return hideCards;
	}

	public void setBehaviour(IBehaviour behaviour) {
		this.behaviour = behaviour;
	}

	public void setHero(Hero hero) {
		if (heroZone.size() != 0) {
			heroZone.remove(0);
		}
		heroZone.add(hero);
	}

	public void setHideCards(boolean hideCards) {
		this.hideCards = hideCards;
	}

	public void setLockedMana(int lockedMana) {
		this.lockedMana = lockedMana;
	}

	public void setMana(int mana) {
		this.mana = mana;
	}

	public void setMaxMana(int maxMana) {
		this.maxMana = maxMana;
	}

	@Override
	public String toString() {
		return "[PLAYER " + "id: " + getId() + ", name: " + getName() + ", hero: " + getHero() + "]";
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
				.append(getId())
				.append(getName())
				.toHashCode();
	}

	@Override
	public boolean equals(Object other) {
		if (other == null
				|| !(other instanceof Player)) {
			return false;
		}

		Player rhd = (Player) other;
		return new EqualsBuilder()
				.append(getId(), rhd.getId())
				.append(getName(), rhd.getName())
				.isEquals();
	}

	public Player withDeck(Deck deck) {
		if (deck == null) {
			return this;
		}

		if (getDeck() == null
				|| getDeck().getCount() > 0) {
			this.deck = new CardZone(getId(), PlayerZones.DECK, deck.getCardsCopy());
		} else if (getDeck().getCount() == 0) {
			this.deck.addAll(deck.getCards());
		}

		return this;
	}

	@Override
	public void setId(int id) {
		super.setId(id);
		minions.setPlayer(id);
		graveyard.setPlayer(id);
		setAsideZone.setPlayer(id);
		hand.setPlayer(id);
		deck.setPlayer(id);
		heroZone.setPlayer(id);
//		getHeroPowerZone().setPlayer(id);
//		getWeaponZone().setPlayer(id);
	}

	public EntityZone getZone(PlayerZones zone) {
		switch (zone) {
			case BATTLEFIELD:
				return getMinions();
			case DECK:
				return getDeck();
			case GRAVEYARD:
				return getGraveyard();
			case HAND:
				return getHand();
			case HERO:
				return getHeroZone();
			case HERO_POWER:
				return getHeroPowerZone();
			case SET_ASIDE_ZONE:
				return getSetAsideZone();
			case WEAPON:
				return getWeaponZone();
			case NONE:
				return null;
			case SECRET:
			case HIDDEN:
				// TODO: Deal with secret zones
				return null;
		}
		return null;
	}

	public EntityZone<Hero> getHeroZone() {
		return heroZone;
	}

	public EntityZone<HeroPower> getHeroPowerZone() {
		return getHero().getHeroPowerZone();
	}

	public EntityZone getWeaponZone() {
		return getHero().getWeaponZone();
	}
}
