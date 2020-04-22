package net.demilich.metastone.game;

import net.demilich.metastone.game.behaviour.Behaviour;
import net.demilich.metastone.game.behaviour.ChooseLastBehaviour;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardZone;
import net.demilich.metastone.game.cards.desc.CardDesc;
import net.demilich.metastone.game.decks.GameDeck;
import net.demilich.metastone.game.entities.Entity;
import com.hiddenswitch.spellsource.client.models.EntityType;
import net.demilich.metastone.game.entities.EntityZone;
import net.demilich.metastone.game.entities.heroes.Hero;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.entities.weapons.Weapon;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.spells.DiscoverSpell;
import net.demilich.metastone.game.spells.PlayerAttribute;
import net.demilich.metastone.game.spells.TargetPlayer;
import net.demilich.metastone.game.spells.trigger.Enchantment;
import net.demilich.metastone.game.spells.trigger.secrets.Quest;
import net.demilich.metastone.game.spells.trigger.secrets.Secret;
import net.demilich.metastone.game.statistics.GameStatistics;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.targeting.IdFactory;
import net.demilich.metastone.game.targeting.Zones;
import net.demilich.metastone.game.cards.Attribute;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The {@link Player} class stores almost the state that corresponds to a particular player, like a collection of {@link
 * EntityZone} objects and select {@link Attribute} and {@link PlayerAttribute} attributes as an {@link Entity} that
 * exists in the game.
 * <p>
 * Unusually, the {@link Zones#WEAPON} and {@link Zones#HERO_POWER} zones are located on the {@link Hero} entity
 * retrievable by {@link #getHero()}.
 * <p>
 * More state is discoverable on the {@link GameContext#getEnvironment()} and {@link TriggerManager#getTriggers()}
 * fields.
 * <p>
 * Player entities are the appropriate {@code target} of many effects, especially text that seems to "live on" after a
 * card is played. For example, take the card Mark of the Future, which reads: "The next minion you play gains +2/+2 and
 * Taunt." The {@link Enchantment} (called also a {@code "trigger"} in the card JSON format described by {@link
 * CardDesc}) that actually gives the next minion played its buff lives on the {@link EntityReference#FRIENDLY_PLAYER},
 * not on the spell.
 *
 * @see Behaviour for more on what player entities are requests to do.
 * @see Zones for a description of the difference zones (i.e. lists) of entities that each player has.
 * @see EntityZone for a description of the class that stores the {@link Entity} objects in the game.
 */
public class Player extends Entity implements Serializable {
	private static final long serialVersionUID = 1L;
	private Map<Integer, Entity> lookup = new HashMap<>(55);
	protected CardZone deck = new CardZone(getId(), Zones.DECK, lookup);
	private CardZone hand = new CardZone(getId(), Zones.HAND, lookup);
	private CardZone discoverZone = new CardZone(getId(), Zones.DISCOVER, lookup);
	private EntityZone<Entity> setAsideZone = new EntityZone<>(getId(), Zones.SET_ASIDE_ZONE, lookup);
	private EntityZone<Entity> graveyard = new EntityZone<>(getId(), Zones.GRAVEYARD, lookup);
	private EntityZone<Entity> removedFromPlay = new EntityZone<>(getId(), Zones.REMOVED_FROM_PLAY, lookup);
	private EntityZone<Minion> minions = new EntityZone<>(getId(), Zones.BATTLEFIELD, lookup);
	private EntityZone<Hero> heroZone = new EntityZone<>(getId(), Zones.HERO, lookup);
	private EntityZone<Secret> secretZone = new EntityZone<>(getId(), Zones.SECRET, lookup);
	private EntityZone<Quest> quests = new EntityZone<>(getId(), Zones.QUEST, lookup);
	private EntityZone<Player> playerZone = new EntityZone<>(getId(), Zones.PLAYER, lookup);
	private EntityZone<Card> heroPowerZone = new EntityZone<>(getId(), Zones.HERO_POWER, lookup);
	private EntityZone<Weapon> weaponZone = new EntityZone<>(getId(), Zones.WEAPON, lookup);
	private GameStatistics statistics = new GameStatistics();

	/**
	 * @see #getMana()
	 */
	private int mana;
	/**
	 * @see #getMaxMana()
	 */
	private int maxMana;
	private int lockedMana;

	/**
	 * Create an empty player instance.
	 *
	 * @return A player specified with an {@link GameDeck#EMPTY} and a {@link ChooseLastBehaviour}.
	 */
	public static Player empty() {
		return new Player(GameDeck.EMPTY, "Empty player");
	}

	/**
	 * Creates a player for the given integer id, userId and deck.
	 *
	 * @param userId The networked user ID of the player.
	 * @param id     The player's ID, {@link IdFactory#PLAYER_1} or {@link IdFactory#PLAYER_2}
	 * @param deck   The deck to initialize the player with.
	 * @return A new player instance with the specified settings and a {@link ChooseLastBehaviour}.
	 */
	public static Player forUser(String userId, int id, GameDeck deck) {
		Player player = new Player(deck, "Player " + userId);
		player.setId(id);
		player.setUserId(userId);
		return player;
	}

	private Player(Player otherPlayer) {
		this.setName(otherPlayer.getName());
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
		this.heroPowerZone = otherPlayer.getHeroPowerZone().clone();
		this.weaponZone = otherPlayer.getWeaponZone().clone();
		this.mana = otherPlayer.mana;
		this.maxMana = otherPlayer.maxMana;
		this.lockedMana = otherPlayer.lockedMana;
		this.statistics = otherPlayer.getStatistics().clone();
		this.lookup = new HashMap<>(playerZone.size()
				+ secretZone.size()
				+ quests.size()
				+ deck.size()
				+ hand.size()
				+ minions.size()
				+ discoverZone.size()
				+ removedFromPlay.size()
				+ graveyard.size()
				+ setAsideZone.size()
				+ heroZone.size()
				+ heroPowerZone.size()
				+ weaponZone.size());
		for (Zones zone : Zones.validZones()) {
			@SuppressWarnings("unchecked")
			EntityZone<? extends Entity> zone1 = (EntityZone<? extends Entity>) getZone(zone);
			zone1.setLookup(lookup);
			for (Object entity : zone1) {
				Entity entity1 = (Entity) entity;
				lookup.put(entity1.getId(), entity1);
			}
		}
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
	public Player(GameDeck deck) {
		this(deck, "New Player");
	}

	/**
	 * Creates a player from the specified deck.
	 *
	 * @param deck The deck instance to use.
	 */

	public Player(GameDeck deck, String name) {
		this();
		this.deck = new CardZone(getId(), Zones.DECK, deck.getCardsCopy(), lookup);
		this.setHero(deck.getHeroCard().hero());
		this.setName(name);
	}

	/**
	 * Creates a player with the hero card of the specified hero class
	 *
	 * @param heroClass
	 */
	public Player(String heroClass) {
		this();
		this.setHero(HeroClass.getHeroCard(heroClass).hero());
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
	 * Retrieves the deck for this player as it is in game. This {@link CardZone} is mutated over time. This is distinct
	 * from a {@link GameDeck} object, which is better interpreted as the base deck from which this object was
	 * initialized.
	 *
	 * @return The player's deck in game.
	 */
	public CardZone getDeck() {
		return deck;
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
	 * Gets the player's mana locked by the Overload mechanic. The locked mana is set to the amount of mana overloaded the
	 * previous turn.
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
	 * Retrieves the card IDs of the secrets owned by this player. Used to enforce that players can only have at most one
	 * of each secret in their {@link #secretZone}.
	 *
	 * @return The set of secret card IDs.
	 * @see GameLogic#canPlaySecret(Player, Card) to see how this method plays into rules regarding the ability to play
	 * secrets.
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
	 * Sets the player's current hero. If a {@link Hero} currently exists in the hero zone, it is removed.
	 *
	 * @param hero The hero entity.
	 * @see GameLogic#changeHero(Player, Entity, Hero) for the appropriate hero changing method for spells.
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
	 * @param id The ID to set to, either {@link IdFactory#PLAYER_1} or {@link IdFactory#PLAYER_2}.
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
		heroPowerZone.setPlayer(id);
		weaponZone.setPlayer(id);
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
				final EntityZone<Player> playerZone = new EntityZone<>(getId(), Zones.PLAYER, lookup);
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
				return EntityZone.empty(Zones.NONE, getId());
			case ENCHANTMENT:
				return EntityZone.empty(Zones.ENCHANTMENT, getId());
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
	 * @see GameLogic#changeHero(Player, Entity, Hero) for the appropriate way to change heroes.
	 */
	public EntityZone<Card> getHeroPowerZone() {
		return heroPowerZone;
	}

	/**
	 * Retrieves the weapon zone belonging to this player's hero entity.
	 *
	 * @return A weapon zone.
	 * @see GameLogic#equipWeapon(int, Weapon, Card, boolean) for the appropriate way to mutate this zone.
	 */
	public EntityZone<Weapon> getWeaponZone() {
		return weaponZone;
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
	 * here, and cards created during a {@link DiscoverSpell} go here.
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
	 * Returns a {@link TargetPlayer} specifier for this player.
	 *
	 * @return Either {@link TargetPlayer#PLAYER_1} or {@link TargetPlayer#PLAYER_2}.
	 */
	public TargetPlayer toTargetPlayer() {
		return getId() == GameContext.PLAYER_1 ? TargetPlayer.PLAYER_1 : TargetPlayer.PLAYER_2;
	}

	/**
	 * Updates the lookup table with the specified entity's ID
	 *
	 * @param entity
	 */
	public void updateLookup(@NotNull Entity entity) {
		if (entity.getId() == IdFactory.UNASSIGNED) {
			throw new IllegalArgumentException("unassigned ID");
		}
		lookup.put(entity.getId(), entity);
	}

	public <T extends Entity> Optional<T> findEntity(int id) {
		@SuppressWarnings("unchecked")
		Optional<T> val = Optional.ofNullable((T) lookup.getOrDefault(id, null));
		return val;
	}

	public Map<Integer, Entity> getLookup() {
		return lookup;
	}

	public Weapon getWeapon() {
		return getWeaponZone().isEmpty() ? null : getWeaponZone().get(0);
	}
}
