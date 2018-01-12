package net.demilich.metastone.game.cards;

import co.paralleluniverse.fibers.Suspendable;
import com.google.gson.annotations.SerializedName;
import io.vertx.core.json.JsonObject;
import net.demilich.metastone.game.utils.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.actions.PlayCardAction;
import net.demilich.metastone.game.cards.desc.CardDesc;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.EntityType;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.minions.Race;
import net.demilich.metastone.game.spells.desc.trigger.TriggerDesc;
import net.demilich.metastone.game.spells.desc.valueprovider.ValueProvider;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.targeting.IdFactory;
import net.demilich.metastone.game.utils.AttributeMap;

import java.util.List;

/**
 * The Card class is an entity that contains card information.
 * <p>
 * Cards are typically in the hand, deck or graveyard. They are playable from the hand or as hero powers. They may be
 * created by other cards. Like all entities, they have attributes and are mutable.
 *
 * @see Entity#getSourceCard() for a way to retrieve the card that backs an entity. For a {@link
 * net.demilich.metastone.game.entities.minions.Minion} summoned from the hand, this typically corresponds to a {@link
 * MinionCard} in the {@link net.demilich.metastone.game.targeting.Zones#GRAVEYARD}. This saves you from doing many
 * kinds of casts for {@link net.demilich.metastone.game.entities.Actor} objects.
 * @see CardDesc for the class that is the base of the serialized representation of cards.
 * @see CardParser#parseCard(JsonObject) to see how cards are deserialized from their JSON representation.
 */
public abstract class Card extends Entity {
	private String description = "";
	@SerializedName("cardType2")
	private CardType cardType;
	private CardSet cardSet;
	private Rarity rarity;
	private HeroClass heroClass;
	private HeroClass[] heroClasses;
	private boolean collectible = true;
	private ValueProvider manaCostModifier;
	private boolean hasPersistentEffects = false;
	/**
	 * @see #getCardId()
	 */
	private String cardId;
	protected CardDesc desc;

	public Card() {
		super();
	}

	/**
	 * Creates a card from a description of a card.
	 *
	 * @param desc The Card description.
	 */
	public Card(CardDesc desc) {
		// Save a reference to the description for later use.
		this.desc = desc;
		cardId = desc.id;
		setName(desc.name);
		setDescription(desc.description);
		setCollectible(desc.collectible);
		cardType = desc.type;
		cardSet = desc.set;
		rarity = desc.rarity;
		heroClass = desc.heroClass;
		hasPersistentEffects = desc.legacy != null && desc.legacy;
		if (desc.heroClasses != null) {
			heroClasses = desc.heroClasses;
		}

		setAttribute(Attribute.BASE_MANA_COST, desc.baseManaCost);
		if (desc.attributes != null) {
			getAttributes().putAll(desc.attributes);
		}

		if (desc.manaCostModifier != null) {
			manaCostModifier = desc.manaCostModifier.createInstance();
		}

		if (desc.passiveTrigger != null) {
			getAttributes().put(Attribute.PASSIVE_TRIGGERS, new TriggerDesc[]{desc.passiveTrigger});
		}

		if (desc.passiveTriggers != null) {
			getAttributes().put(Attribute.PASSIVE_TRIGGERS, desc.passiveTriggers);
		}

		if (desc.deckTrigger != null) {
			getAttributes().put(Attribute.DECK_TRIGGER, desc.deckTrigger);
		}

		if (desc.gameTriggers != null) {
			getAttributes().put(Attribute.GAME_TRIGGERS, desc.gameTriggers);
		}
	}

	/**
	 * Clones a card's base fields, like name and description, and its current attributes. The entity ID and location
	 * match the source object and are not cleared. {@link #getCopy()} is typically more appropriate choice for when
	 * copies of cards are needed.
	 *
	 * @return An exact clone.
	 */
	@Override
	public Card clone() {
		Card clone = (Card) super.clone();
		clone.setAttributes(new AttributeMap(getAttributes()));
		return clone;
	}

	/**
	 * Evaluates an expression written on a rule.
	 *
	 * @param operator An expression operator
	 * @param value1   The left value.
	 * @param value2   The right value.
	 * @return The result of evaluating the expression.
	 */
	public static boolean evaluateExpression(String operator, int value1, int value2) {
		switch (operator) {
			case "=":
				return value1 == value2;
			case ">":
				return value1 > value2;
			case "<":
				return value1 < value2;
			case ">=":
				return value1 >= value2;
			case "<=":
				return value1 <= value2;
			case "!=":
				return value1 != value2;
		}
		return false;
	}

	/**
	 * The base mana cost of a card. This is the cost that's written on the card.
	 *
	 * @return The base mana cost of a card.
	 */
	public int getBaseManaCost() {
		return getAttributeValue(Attribute.BASE_MANA_COST);
	}

	/**
	 * Gets the card's ID as it corresponds to the card catalogue. This is the base definition of the card.
	 *
	 * @return
	 */
	public String getCardId() {
		if (cardId == null) {
			return null;
		}
		return cardId.toLowerCase();
	}

	/**
	 * Gets an object which refers to the card's location, owner, and ID. Used for lookups.
	 * <p>
	 * Unusually, card references include the card name. Some cards, like the Rogue Quest, interact with card names
	 * instead of their IDs.
	 *
	 * @return A reference to the card.
	 */
	public EntityReference getEntityReference() {
		return new EntityReference(getId());
	}

	/**
	 * Gets the set that the card belongs to.
	 *
	 * @return
	 */
	public CardSet getCardSet() {
		return cardSet;
	}

	/**
	 * Gets the card type, like Hero, Secret, Spell or Minion.
	 *
	 * @return
	 */
	public CardType getCardType() {
		return cardType;
	}

	/**
	 * Gets the hero class that this card belongs to. Valid classes include ANY (neutral) or any of the main 9 classes.
	 *
	 * @return
	 */
	public HeroClass getHeroClass() {
		return heroClass;
	}

	/**
	 * Some cards have multiple hero classes. This field stores those multiple classes when they are defined.
	 *
	 * @return
	 */
	public HeroClass[] getHeroClasses() {
		return heroClasses;
	}

	/**
	 * Gets a copy of the card with some attributes like its attack or HP bonuses and mana cost modifiers removed. The
	 * ID and owner is set to unassigned.
	 * <p>
	 * Typically you should use the {@link net.demilich.metastone.game.logic.GameLogic#receiveCard(int, Card)} method in
	 * order to put a copy into e.g. the player's hand.
	 * <p>
	 * Take a look at its logic to see how to assign an ID and an owner to a card for other uses of copies. A copy can
	 * become valid for play like this: {@code Card copiedCard = card.getCopy(); int owningPlayer = player.getId();
	 * copiedCard.setId(getGameLogic().getIdFactory().generateId(); copiedCard.setOwner(owningPlayer);
	 * <p>
	 * // Add to an appropriate zone. For example, to add the card to the end of the owning player's deck...
	 * context.getPlayer(owningPlayer).getDeck().add(copiedCard); }
	 *
	 * @return A copy of the card with no ID or owner (and therefore no location).
	 */
	@Override
	public Card getCopy() {
		Card copy = clone();
		copy.setId(IdFactory.UNASSIGNED);
		copy.setOwner(IdFactory.UNASSIGNED);
		copy.getAttributes().remove(Attribute.ATTACK_BONUS);
		copy.getAttributes().remove(Attribute.HP_BONUS);
		copy.getAttributes().remove(Attribute.MANA_COST_MODIFIER);
		// Always use the origin copy if it isn't none
		if (hasAttribute(Attribute.COPIED_FROM)) {
			copy.getAttributes().put(Attribute.COPIED_FROM, getAttribute(Attribute.COPIED_FROM));
		} else if (!getReference().equals(EntityReference.NONE)) {
			copy.getAttributes().put(Attribute.COPIED_FROM, getReference());
		}

		copy.resetEntityLocations();
		return copy;
	}

	/**
	 * Gets a cleaned up description of the card. In the future, this description should "fill in the blanks" for cards
	 * that have variables, like which minion will be summoned or how much spell damage the spell will deal.
	 *
	 * @return The description.
	 */
	public String getDescription() {
		// Cleanup the html tags that appear in the description
		final String description = hasAttribute(Attribute.DESCRIPTION) ? (String) getAttribute(Attribute.DESCRIPTION) : this.description;
		if (description == null || description.isEmpty()) {
			return description;
		}
		return description.replaceAll("(</?[bi]>)|\\[x\\]", "");
	}

	@Override
	public EntityType getEntityType() {
		return EntityType.CARD;
	}

	/**
	 * Gets the mana cost of this card from the point of view of the specified card, player and a given context. Does
	 * <b>NOT</b> consider the effect of card cost modifiers.
	 * <p>
	 * Costs can be modified lots of different ways, so this method ensures the cost is calculate considering all the
	 * rules that are on the board.
	 *
	 * @param context The {@link GameContext} to compute the cost against.
	 * @param player  The {@link Player} whose point of view should be considered for the cost. This is almost always
	 *                the owner.
	 * @return The cost.
	 * @see net.demilich.metastone.game.logic.GameLogic#getModifiedManaCost(Player, Card) for the best method to get the
	 * cost of a card.
	 */
	public int getManaCost(GameContext context, Player player) {
		int actualManaCost = getBaseManaCost();
		if (manaCostModifier != null) {
			actualManaCost -= manaCostModifier.getValue(context, player, null, this);
		}
		return actualManaCost;
	}

	/**
	 * A rarity of the card. Rarer cards are generally more powerful; they appear in card packs less frequently; and
	 * {@link Rarity#LEGENDARY} cards can only appear once in a deck.
	 *
	 * @return A {@link Rarity}
	 */
	public Rarity getRarity() {
		return rarity;
	}

	/**
	 * Gets the race of a card. Typically only applies to {@link MinionCard} that summon minions when played.
	 *
	 * @return A {@link Race}
	 */
	public Race getRace() {
		return hasAttribute(Attribute.RACE) ? (Race) getAttribute(Attribute.RACE) : Race.NONE;
	}

	/**
	 * Checks if the hero class specified is in its list of hero classes when this card belongs to multiple hero
	 * classes.
	 *
	 * @param heroClass The {@link HeroClass} to search.
	 * @return <code>True</code> if this card has the specified class.
	 */
	public boolean hasHeroClass(HeroClass heroClass) {
		if (getHeroClasses() != null) {
			for (HeroClass h : getHeroClasses()) {
				if (heroClass.equals(h)) {
					return true;
				}
			}
		} else if (heroClass == getHeroClass()) {
			return true;
		} else if (heroClass == HeroClass.INHERIT) {
			return true;
		}
		return false;
	}

	/**
	 * Collectible cards can be put into decks. Non-collectible cards are typically either "tokens," or cards that are
	 * spawned by other cards, or narrative cards.
	 * <p>
	 * Even though tokens are almost always minions, effects like {@link net.demilich.metastone.game.spells.ReturnTargetToHandSpell}
	 * can create a card that represents a minion.
	 *
	 * @return <code>True</code> if the card is collectible.
	 */
	public boolean isCollectible() {
		return collectible;
	}

	/**
	 * Create an action representing playing the card.
	 *
	 * @return An action that should be evaluated by {@link net.demilich.metastone.game.logic.GameLogic#performGameAction(int,
	 * GameAction)}.
	 */
	@Suspendable
	public abstract PlayCardAction play();

	public void setCollectible(boolean collectible) {
		this.collectible = collectible;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String toString() {
		return String.format("[%s '%s' %s Manacost:%d]", getCardType(), getName(), getReference(), getBaseManaCost());
	}

	/**
	 * Gets the original {@link CardDesc} that was used to create this card. Modifying this description does not modify
	 * the card, and the {@link CardDesc} may be referenced by multiple instances of {@link Card}.
	 *
	 * @return A {@link CardDesc}
	 */
	public CardDesc getDesc() {
		return desc;
	}

	@Override
	public Card getSourceCard() {
		return this;
	}

	public TriggerDesc[] getPassiveTriggers() {
		return (TriggerDesc[]) getAttribute(Attribute.PASSIVE_TRIGGERS);
	}

	@Override
	public boolean hasPersistentEffects() {
		return hasPersistentEffects;
	}

	/**
	 * Returns the triggers that are active when the card is in the deck.
	 *
	 * @return A list of {@link TriggerDesc} objects.
	 */
	public TriggerDesc[] getDeckTriggers() {
		final TriggerDesc triggerDesc = (TriggerDesc) getAttribute(Attribute.DECK_TRIGGER);
		if (triggerDesc == null) {
			return new TriggerDesc[0];
		}
		return new TriggerDesc[]{triggerDesc};
	}

}
