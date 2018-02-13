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
import net.demilich.metastone.game.cards.SecretCard;
import net.demilich.metastone.game.cards.WeaponCard;
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
import net.demilich.metastone.game.targeting.EntityReference;
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

	/**
	 * Create an empty player instance.
	 *
	 * @return A player specified with an {@link Deck#EMPTY} and a {@link DoNothingBehaviour}.
	 */
	public static Player empty() {
		Player player = new Player();
		PlayerConfig config = new PlayerConfig(Deck.EMPTY, new DoNothingBehaviour());
		player.buildFromConfig(config);
		return player;
	}

	/**
	 * Creates a player for the given integer id, userId and deck.
	 *
	 * @param userId The networked user ID of the player.
	 * @param id     The player's ID, {@link net.demilich.metastone.game.targeting.IdFactory#PLAYER_1} or {@link
	 *               net.demilich.metastone.game.targeting.IdFactory#PLAYER_2}
	 * @param deck   The deck to initialize the player with.
	 * @return A new player instance with the specified settings and a {@link DoNothingBehaviour}.
	 */
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

	/**
	 * Creates a player from the specified deck.
	 *
	 * @param deck The deck instance to use.
	 */
	public Player(Deck deck) {
		this(PlayerConfig.fromDeck(deck));
	}

	/**
	 * Builds a player with the specified {@link PlayerConfig} object.
	 * <p>
	 * Since a player instance also contains match data, a {@link PlayerConfig} better models the idea of a template
	 * from which player objects are created for possibly many games.
	 *
	 * @param config A {@link PlayerConfig} instance.
	 */
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
	}

	/**
	 * Clones the underlying data and behaviour of this player instance.
	 *
	 * @return A new clone.
	 */
	@Override
	public Player clone() {
		return new Player(this);
	}

	/**
	 * The behaviour that specifies what actions are taken by the player who owns this hero, minions, deck, etc. It is a
	 * delegate.
	 *
	 * @return The behaviour instance.
	 * @see Behaviour for more about this object model.
	 */
	public Behaviour getBehaviour() {
		return behaviour;
	}

	/**
	 * Retrieves the deck for this player as it is in game. This {@link CardZone} is mutated over time. This is distinct
	 * from a {@link Deck} object, which is better interpreted as the base deck from which this object was initialized.
	 *
	 * @return The player's deck in game.
	 */
	public CardZone getDeck() {
		return deck;
	}

	@Deprecated
	public String getDeckName() {
		return deckName;
	}

	@Override
	public EntityType getEntityType() {
		return EntityType.PLAYER;
	}

	/**
	 * Retrieves the player's graveyard.
	 *
	 * @return An {@link EntityZone} containing played cards and dead minions.
	 * @see Zones#GRAVEYARD for more about the graveyard.
	 */
	public EntityZone<Entity> getGraveyard() {
		return graveyard;
	}

	/**
	 * Retrieves the player's hand.
	 *
	 * @return A {@link CardZone} containing the player's current hand.
	 * @see Zones#HAND for more about the hand.
	 */
	public CardZone getHand() {
		return hand;
	}

	/**
	 * Retrieves the hero specified inside the {@link #heroZone} field, an {@link EntityZone} that typically holds just
	 * one hero object for the player.
	 *
	 * @return A {@link Hero} instance.
	 * @see #getHeroZone() for the one-item {@link EntityZone} that this field consults for the {@link Hero} entity.
	 * @see Zones#HERO for more about the hero zone.
	 */
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

	/**
	 * Gets the player's mana locked by the Overload mechanic. The locked mana is set to the amount of mana overloaded
	 * the previous turn.
	 *
	 * @return The amount of mana that is unusable this turn due to playing a card with {@link Attribute#OVERLOAD} last
	 * turn.
	 * @see Attribute#OVERLOAD for more about locking mana.
	 */
	public int getLockedMana() {
		return lockedMana;
	}

	/**
	 * Retrieves the current amount of mana the player has to spend this turn. This amount of mana is set to {@link
	 * #getMaxMana()} minus the amount of {@link #getLockedMana()} at the start of the player's turn/
	 *
	 * @return The amount of mana available to spend.
	 */
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

	/**
	 * Gets the minions on this player's side of the battlefield.
	 *
	 * @return An {@link EntityZone} of minions.
	 */
	public EntityZone<Minion> getMinions() {
		return minions;
	}

	/**
	 * Retrieves the card IDs of the secrets owned by this player. Used to enforce that players can only have at most
	 * one of each secret in their {@link #secretZone}.
	 *
	 * @return The set of secret card IDs.
	 * @see net.demilich.metastone.game.logic.GameLogic#canPlaySecret(Player, SecretCard) to see how this method plays
	 * into rules regarding the ability to play secrets.
	 */
	public Set<String> getSecretCardIds() {
		return secretZone.stream().map(Secret::getSourceCard).map(Card::getCardId).collect(Collectors.toSet());
	}

	/**
	 * Retrieves the secrets owned by this player.
	 *
	 * @return Secret entities.
	 */
	public EntityZone<Secret> getSecrets() {
		return secretZone;
	}

	/**
	 * Retrieves the set aside zone, or the location where cards are temporarily moved during complex interactions.
	 *
	 * @return The zone.
	 * @see Zones#SET_ASIDE_ZONE for more about the set aside zone.
	 */
	public EntityZone<Entity> getSetAsideZone() {
		return setAsideZone;
	}

	/**
	 * Retrieves statistics collected about this player in the current game.
	 *
	 * @return A {@link GameStatistics} object.
	 */
	public GameStatistics getStatistics() {
		return statistics;
	}

	/**
	 * Sets the behaviour for this player.
	 *
	 * @param behaviour A behaviour.
	 * @see Behaviour for more about behaviours.
	 */
	public void setBehaviour(Behaviour behaviour) {
		this.behaviour = behaviour;
	}

	/**
	 * Sets the player's current hero. If a {@link Hero} currently exists in the hero zone, it is removed.
	 *
	 * @param hero The hero entity.
	 * @see net.demilich.metastone.game.logic.GameLogic#changeHero(Player, Hero) for the appropriate hero changing
	 * method for spells.
	 */
	public void setHero(Hero hero) {
		if (heroZone.size() != 0) {
			// Move the existing hero to the graveyard
			heroZone.remove(0);
		}
		heroZone.add(hero);
	}

	/**
	 * Sets the amount of mana that was overloaded.
	 *
	 * @param lockedMana The amount of mana to lock this turn.
	 * @see Attribute#OVERLOAD for more about overloading mana.
	 */
	public void setLockedMana(int lockedMana) {
		this.lockedMana = lockedMana;
	}

	/**
	 * Sets the current mana this player has. Usually invoked by spells that increase mana temporarily or when cards are
	 * played.
	 *
	 * @param mana The amount of mana this player should now have.
	 */
	public void setMana(int mana) {
		this.mana = mana;
	}

	/**
	 * Gives the player this many "empty mana crystals."
	 *
	 * @param maxMana The maximum amount of mana a player can have. Increased by one each turn.
	 */
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

	/**
	 * Compares two player objects.
	 * <p>
	 * They are considered equal if their IDs and names match.
	 *
	 * @param other The other player object.
	 * @return {@code true} if the other player object's ID and name matches this one's. Otherwise, {@code false}.
	 */
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

	/**
	 * Sets the player's ID. Can only be called once. Sets the owner fields on the zones stored in this player object.
	 *
	 * @param id The ID to set to, either {@link net.demilich.metastone.game.targeting.IdFactory#PLAYER_1} or {@link
	 *           net.demilich.metastone.game.targeting.IdFactory#PLAYER_2}.
	 */
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

	/**
	 * Clones this player.
	 *
	 * @return A clone.
	 */
	@Override
	public Player getCopy() {
		return this.clone();
	}

	/**
	 * Retrieves a zone by key.
	 *
	 * @param zone The key.
	 * @return An {@link EntityZone} for the corresponding zone. For {@link Zones#PLAYER}, a new zone is created on the
	 * fly containing this player entity. For {@link Zones#NONE}, an empty zone is returned.
	 */
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

	/**
	 * Retrieves the hero zone.
	 *
	 * @return The zone that stores this player's hero entity.
	 */
	public EntityZone<Hero> getHeroZone() {
		return heroZone;
	}

	/**
	 * Retrieves the hero power zone stored inside the hero entity.
	 *
	 * @return The hero power stored by this hero.
	 * @see net.demilich.metastone.game.logic.GameLogic#changeHero(Player, Hero) for the appropriate way to change
	 * heroes.
	 */
	public EntityZone<HeroPowerCard> getHeroPowerZone() {
		return getHero().getHeroPowerZone();
	}

	/**
	 * Retrieves the weapon zone belonging to this player's hero entity.
	 *
	 * @return A weapon zone.
	 * @see net.demilich.metastone.game.logic.GameLogic#equipWeapon(int, Weapon, WeaponCard, boolean) for the
	 * appropriate way to mutate this zone.
	 */
	public EntityZone<Weapon> getWeaponZone() {
		return getHero().getWeaponZone();
	}

	/**
	 * Retrieves the cards the player is currently discovering.
	 *
	 * @return A {@link CardZone} of cards.
	 */
	public CardZone getDiscoverZone() {
		return discoverZone;
	}

	/**
	 * Retrieves entities that are removed from play. Typically enchantments like {@link Quest} and {@link Secret} go
	 * here, and cards created during a {@link net.demilich.metastone.game.spells.DiscoverSpell} go here.
	 * <p>
	 * Entities that are in {@link Zones#REMOVED_FROM_PLAY} should not be targetable, so it would be unusual to iterate
	 * through this zone.
	 *
	 * @return Entities removed from play.
	 * @see GameContext#resolveTarget(Player, Entity, EntityReference) for the  method that finds entities inside zones.
	 */
	public EntityZone<Entity> getRemovedFromPlay() {
		return removedFromPlay;
	}

	@Override
	public int getOwner() {
		return getId();
	}

	/**
	 * Gets the {@link Quest} entities that are in play from this player.
	 *
	 * @return An {@link EntityZone}.
	 */
	public EntityZone<Quest> getQuests() {
		return quests;
	}

	/**
	 * For a player entity, its source card corresponds to the hero's source card.
	 *
	 * @return The {@link Hero}'s source card, or {@code null} if no hero is set.
	 */
	@Override
	public Card getSourceCard() {
		if (getHero() == null) {
			return null;
		}
		return getHero().getSourceCard();
	}

	/**
	 * Determines whether this player object is backed by a human player.
	 *
	 * @return True if the behaviour has a human making the {@link Behaviour#requestAction(GameContext, Player, List)}
	 * decisions.
	 */
	public boolean isHuman() {
		return !hasAttribute(Attribute.AI_OPPONENT) && getBehaviour().isHuman();
	}
}
