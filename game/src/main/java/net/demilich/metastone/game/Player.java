package net.demilich.metastone.game;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.gson.annotations.Expose;
import net.demilich.metastone.game.behaviour.DoNothingBehaviour;
import net.demilich.metastone.game.behaviour.Behaviour;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardZone;
import net.demilich.metastone.game.decks.Deck;
import net.demilich.metastone.game.entities.*;
import net.demilich.metastone.game.entities.heroes.Hero;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.entities.weapons.Weapon;
import net.demilich.metastone.game.heroes.powers.HeroPowerCard;
import net.demilich.metastone.game.spells.trigger.secrets.Quest;
import net.demilich.metastone.game.spells.trigger.secrets.Secret;
import net.demilich.metastone.game.statistics.GameStatistics;
import net.demilich.metastone.game.gameconfig.PlayerConfig;
import net.demilich.metastone.game.targeting.Zones;
import net.demilich.metastone.game.utils.Attribute;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * The {@link Player} class stores all the state that corresponds to a particular player, like a collection of {@link
 * EntityZone} objects, a reference to a {@link Behaviour} that gets delegated requests for actions from the {@link
 * GameContext}, and select {@link Attribute} and {@link net.demilich.metastone.game.spells.PlayerAttribute} attributes
 * as an {@link Entity} that exists in the game.
 *
 * @see Behaviour for more on what player entities are requests to do.
 * @see Zones for a description of the difference zones (i.e. lists) of entities that each player has.
 * @see EntityZone for a description of the class that stores the {@link Entity} objects in the game.
 */
public class Player extends Entity implements Serializable {
	private static final long serialVersionUID = 1L;

	public static Player empty() {
		Player player = new Player();
		PlayerConfig config = new PlayerConfig(Deck.EMPTY, new DoNothingBehaviour());
		player.buildFromConfig(config);
		return player;
	}

	public static Player forUser(String userId, int id, Deck deck) {
		Player player = new Player();
		PlayerConfig config = new PlayerConfig(deck, new DoNothingBehaviour());
		config.setHeroCard(deck.getHeroCard());
		player.setId(id);
		player.buildFromConfig(config);
		player.setUserId(userId);
		return player;
	}

	protected String deckName;
	protected CardZone deck = new CardZone(getId(), Zones.DECK);
	private CardZone hand = new CardZone(getId(), Zones.HAND);
	private CardZone discoverZone = new CardZone(getId(), Zones.DISCOVER);
	private EntityZone<Entity> setAsideZone = new EntityZone<>(getId(), Zones.SET_ASIDE_ZONE);
	private EntityZone<Entity> graveyard = new EntityZone<>(getId(), Zones.GRAVEYARD);
	private EntityZone<Entity> removedFromPlay = new EntityZone<>(getId(), Zones.REMOVED_FROM_PLAY);
	private EntityZone<Minion> minions = new EntityZone<>(getId(), Zones.BATTLEFIELD);
	private EntityZone<Hero> heroZone = new EntityZone<>(getId(), Zones.HERO);
	private EntityZone<Secret> secretZone = new EntityZone<>(getId(), Zones.SECRET);
	private EntityZone<Quest> quests = new EntityZone<>(getId(), Zones.QUEST);
	private EntityZone<Player> playerZone = new EntityZone<>(getId(), Zones.PLAYER);

	private final GameStatistics statistics = new GameStatistics();

	/**
	 * @see #getMana()
	 */
	private int mana;
	/**
	 * @see #getMaxMana()
	 */
	private int maxMana;
	private int lockedMana;

	private boolean hideCards;

	@Expose(serialize = false, deserialize = false)
	private Behaviour behaviour;

	private Player(Player otherPlayer) {
		this.setName(otherPlayer.getName());
		this.deckName = otherPlayer.getDeckName();
		this.getAttributes().putAll(otherPlayer.getAttributes());
		this.playerZone.add(this);
		this.setId(otherPlayer.getId());
		this.secretZone = otherPlayer.getSecrets().clone();
		this.quests = otherPlayer.getQuests().clone();
		this.deck = otherPlayer.getDeck().clone();
		this.hand = otherPlayer.getHand().clone();
		this.minions = otherPlayer.getMinions().clone();
		this.discoverZone = otherPlayer.getDiscoverZone().clone();
		this.removedFromPlay = otherPlayer.getRemovedFromPlay().clone();
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
	public Player() {
		this.playerZone.add(this);
	}

	public Player(PlayerConfig config) {
		this();
		buildFromConfig(config);
	}

	protected void buildFromConfig(PlayerConfig config) {
		config.build();
		Deck selectedDeck = config.getDeckForPlay();

		this.deck = new CardZone(getId(), Zones.DECK, selectedDeck.getCardsCopy());
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

	public Behaviour getBehaviour() {
		return behaviour;
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

	/**
	 * The maximum amount of mana the player can currently have. At the start of the turn, the player's {@link #mana} is
	 * set to this value.
	 *
	 * @return The maximum amount of mana this player can have.
	 */
	public int getMaxMana() {
		return maxMana;
	}

	public EntityZone<Minion> getMinions() {
		return minions;
	}

	public Set<String> getSecretCardIds() {
		return secretZone.stream().map(Secret::getSourceCard).map(Card::getCardId).collect(Collectors.toSet());
	}

	public EntityZone<Secret> getSecrets() {
		return secretZone;
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

	public void setBehaviour(Behaviour behaviour) {
		this.behaviour = behaviour;
	}

	public void setHero(Hero hero) {
		if (heroZone.size() != 0) {
			// Move the existing hero to the graveyard
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

	@Override
	public void setId(int id) {
		super.setId(id);
		minions.setPlayer(id);
		discoverZone.setPlayer(id);
		removedFromPlay.setPlayer(id);
		graveyard.setPlayer(id);
		setAsideZone.setPlayer(id);
		hand.setPlayer(id);
		deck.setPlayer(id);
		heroZone.setPlayer(id);
		secretZone.setPlayer(id);
		playerZone.setPlayer(id);
		quests.setPlayer(id);
	}

	@Override
	public Player getCopy() {
		return this.clone();
	}

	public EntityZone getZone(Zones zone) {
		switch (zone) {
			case PLAYER:
				final EntityZone<Player> playerZone = new EntityZone<>(getId(), Zones.PLAYER);
				playerZone.add(this);
				return playerZone;
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
			case SECRET:
				return getSecrets();
			case DISCOVER:
				return getDiscoverZone();
			case REMOVED_FROM_PLAY:
				return getRemovedFromPlay();
			case QUEST:
				return getQuests();
			case NONE:
				return EntityZone.empty(getId());
		}
		return null;
	}

	public EntityZone<Hero> getHeroZone() {
		return heroZone;
	}

	public EntityZone<HeroPowerCard> getHeroPowerZone() {
		return getHero().getHeroPowerZone();
	}

	public EntityZone<Weapon> getWeaponZone() {
		return getHero().getWeaponZone();
	}

	public CardZone getDiscoverZone() {
		return discoverZone;
	}

	public EntityZone<Entity> getRemovedFromPlay() {
		return removedFromPlay;
	}

	@Override
	public int getOwner() {
		return getId();
	}

	public EntityZone<Quest> getQuests() {
		return quests;
	}

	@Override
	public Card getSourceCard() {
		return getHero().getSourceCard();
	}

	public Stream<? extends Actor> getActors() {
		return Stream.concat(Stream.concat(getHeroZone().stream(), getMinions().stream()), getWeaponZone().stream());
	}
}
