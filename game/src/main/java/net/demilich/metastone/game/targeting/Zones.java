package net.demilich.metastone.game.targeting;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.heroes.Hero;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import net.demilich.metastone.game.spells.trigger.Enchantment;
import net.demilich.metastone.game.spells.trigger.secrets.Quest;
import net.demilich.metastone.game.spells.trigger.secrets.Secret;

import java.util.EnumSet;
import java.util.Set;

/**
 * Zones describe the different locations for entities in the game.
 * <p>
 * In a standard game, the local player can see their {@link #HAND}, their {@link #SECRET} zone, and both player's
 * {@link #BATTLEFIELD} zones. They know the count of the number of entities in the opponent's {@link #HAND}, opponent's
 * {@link #DECK}, opponent's {@link #SECRET} zone and their own {@link #DECK}. While neither player can browse through
 * the {@link #GRAVEYARD} the information inside of it is not considered secret.
 * <p>
 * Many effects interact with zones in special ways. For example, a {@link GameLogic#summon(int, Minion, Entity, int, boolean)} performs the consequences of playing a {@link Card}; the card is moved to the {@link #GRAVEYARD} and a new
 * {@link Minion} is created by {@link Card#summon()} and placed into the {@link #BATTLEFIELD}.
 *
 * @see Entity#moveOrAddTo(GameContext, Zones) for the method that generally moves entities from one zone to another.
 * @see net.demilich.metastone.game.entities.EntityLocation for more about entity locations and how zones are
 * 		manipulated.
 */
public enum Zones {
	/**
	 * This zone specifies the entity belongs to no zone or the zone is not yet assigned.
	 */
	NONE("N"),
	/**
	 * This zone is a player's hand. Only {@link Card} entities can be in this zone.
	 *
	 * @see net.demilich.metastone.game.cards.CardZone for some special rules about zones that contain only cards.
	 */
	HAND("H"),
	/**
	 * This zone is a player's deck. Only {@link Card} entities can be in this zone.
	 *
	 * @see net.demilich.metastone.game.cards.CardZone for some special rules about zones that contain only cards.
	 */
	DECK("D"),
	/**
	 * The graveyard is where a {@link Card} has been played with {@link GameLogic#playCard(int, EntityReference, EntityReference)} goes;
	 * and where an {@link Actor} that has been destroyed with {@link GameLogic#destroy(Actor...)} goes. A {@link
	 * net.demilich.metastone.game.spells.trigger.secrets.Secret} and other entities subclassing {@link Enchantment} go to
	 * {@link #REMOVED_FROM_PLAY}.
	 *
	 * @see #REMOVED_FROM_PLAY for the alternative location for "destroyed" entities.
	 * @see GameLogic#destroy(Actor...) for more about destroying actors.
	 */
	GRAVEYARD("G"),
	/**
	 * A {@link Minion} is typically summoned into this zone. Anything in this zone is targetable by physical attacks.
	 */
	BATTLEFIELD("B"),
	/**
	 * This zone is where a {@link net.demilich.metastone.game.spells.trigger.secrets.Secret} entity goes. Its contents
	 * are not visible to the opponent.
	 *
	 * @see GameLogic#playSecret(Player, Secret, boolean) for more about secrets.
	 */
	SECRET("S"),
	/**
	 * This zone is  where {@link net.demilich.metastone.game.spells.trigger.secrets.Quest} entities go, which behave like
	 * secrets that are visible to the opponent and do not go away the first time they  are triggered.
	 *
	 * @see GameLogic#playQuest(Player, Quest) for more about quests.
	 */
	QUEST("Q"),
	/**
	 * The hero power zone stores the hero power for a corresponding {@link net.demilich.metastone.game.entities.heroes.Hero}.
	 * Only one such card can be in the zone at a time.
	 *
	 * @see Hero#getHeroPowerZone() for more about the hero power zone.
	 */
	HERO_POWER("K"),
	/**
	 * The hero zone stores the {@link Hero} actor that represents a player's targetable avatar in the game.
	 *
	 * @see Hero for more about heroes.
	 */
	HERO("E"),
	/**
	 * The weapon zone stores the {@link net.demilich.metastone.game.entities.weapons.Weapon} that a {@link Hero} has
	 * equipped.
	 *
	 * @see net.demilich.metastone.game.entities.weapons.Weapon for more about weapons.
	 */
	WEAPON("W"),
	/**
	 * The discover zone has any cards that are being currently chosen by the player as part of a {@link
	 * net.demilich.metastone.game.actions.DiscoverAction}.
	 * <p>
	 * The opposing player can see the count, but not the contents, of cards the player is choosing between.
	 *
	 * @see net.demilich.metastone.game.spells.SpellUtils#discoverCard(GameContext, Player, Entity, SpellDesc, CardList)
	 * 		for more about how discover is implemented.
	 * @see net.demilich.metastone.game.actions.DiscoverAction for more about a discover action.
	 */
	DISCOVER("D"),
	/**
	 * An {@link Entity} in this zone is "deleted" in the sense that it will never appear in any {@link EntityFilter}
	 * filters or targeting lists.
	 *
	 * @see #GRAVEYARD for the usual place entities go when they are destroyed by effects rather than deleted.
	 */
	REMOVED_FROM_PLAY("R"),
	/**
	 * The set aside zone holds an {@link Entity} existing in any intermediate or "not really on the board" state, like
	 * the original minion after Recycle puts a new copy in the deck, the prior state of transformed minions and Lord
	 * Jaraxxus the minion after his Battlecry occurs.
	 * <p>
	 * Unlike the official game rules, the three cards presented to a player by Tracking go into the {@link #DISCOVER}
	 * zone.
	 *
	 * @see GameLogic#removeActor(Actor, boolean) for an example of usage of a set aside zone (when the method is called
	 * 		with {@code peacefully = false;}.
	 */
	SET_ASIDE_ZONE("A"),
	/**
	 * Metastone originally used the same object for what is now the {@link Player} and {@link Hero} entity. Since the
	 * {@link Player} is still targetable (primarily by special buffing spells), it needs a {@link Zones} zone to belong
	 * to. This zone is the zone a {@link Player} entity belongs to.
	 *
	 * @see Player for more about player entities.
	 */
	PLAYER("P");

	/**
	 * These zones are public for notification purposes: both players ought to see their contents.
	 */
	public static final Set<Zones> PUBLIC = EnumSet.of(Zones.BATTLEFIELD, Zones.PLAYER, Zones.HERO, Zones.HERO_POWER, Zones.WEAPON, Zones.QUEST, Zones.SECRET);
	/**
	 * These zones are private: only the player that owns the entity in the zone ought to see notifications originating
	 * from that zone.
	 */
	public static final Set<Zones> PRIVATE = EnumSet.of(Zones.DISCOVER, Zones.HAND, Zones.DECK, Zones.SET_ASIDE_ZONE, Zones.GRAVEYARD, Zones.REMOVED_FROM_PLAY);
	private String serialized;


	Zones(String serialized) {
		this.serialized = serialized;
	}

	public String getSerialized() {
		return serialized;
	}
}
