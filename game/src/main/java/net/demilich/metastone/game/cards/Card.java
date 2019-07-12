package net.demilich.metastone.game.cards;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.*;
import net.demilich.metastone.game.cards.desc.CardDesc;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.EntityType;
import net.demilich.metastone.game.entities.heroes.Hero;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.entities.minions.Race;
import net.demilich.metastone.game.entities.weapons.Weapon;
import net.demilich.metastone.game.spells.*;
import net.demilich.metastone.game.spells.aura.Aura;
import net.demilich.metastone.game.spells.desc.BattlecryDesc;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.aura.AuraDesc;
import net.demilich.metastone.game.spells.desc.condition.ConditionDesc;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import net.demilich.metastone.game.spells.desc.trigger.EnchantmentDesc;
import net.demilich.metastone.game.spells.desc.valueprovider.ValueProvider;
import net.demilich.metastone.game.spells.trigger.Enchantment;
import net.demilich.metastone.game.spells.trigger.MinionDeathTrigger;
import net.demilich.metastone.game.spells.trigger.NullTrigger;
import net.demilich.metastone.game.spells.trigger.secrets.Quest;
import net.demilich.metastone.game.spells.trigger.secrets.Secret;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.targeting.IdFactory;
import net.demilich.metastone.game.targeting.TargetSelection;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * The Card class is an entity that contains card information.
 * <p>
 * Cards are typically in the hand, deck or graveyard. They are playable from the hand or as hero powers. They may be
 * created by other cards. Like all entities, they have attributes and are mutable.
 *
 * @see Entity#getSourceCard() for a way to retrieve the card that backs an entity. For a {@link
 *    net.demilich.metastone.game.entities.minions.Minion} summoned from the hand, this typically corresponds to a {@link
 *    Card} in the {@link net.demilich.metastone.game.targeting.Zones#GRAVEYARD}. This saves you from doing many kinds of
 * 		casts for {@link net.demilich.metastone.game.entities.Actor} objects.
 * @see CardDesc for the class that is the base of the serialized representation of cards.
 */
public class Card extends Entity implements HasChooseOneActions, HasDeathrattleEnchantments {

	private static Logger logger = LoggerFactory.getLogger(Card.class);

	public static final Set<Attribute> IGNORED_MINION_ATTRIBUTES = new HashSet<>(
			Arrays.asList(
					Attribute.PASSIVE_TRIGGERS,
					Attribute.DECK_TRIGGERS,
					Attribute.BASE_ATTACK,
					Attribute.BASE_HP,
					Attribute.SECRET,
					Attribute.CHOOSE_ONE,
					Attribute.BATTLECRY,
					Attribute.COMBO,
					Attribute.TRANSFORM_REFERENCE,
					Attribute.ECHO,
					Attribute.AURA_ECHO,
					Attribute.BEING_PLAYED,
					Attribute.INDEX,
					Attribute.INDEX_FROM_END,
					Attribute.HAND_INDEX,
					Attribute.CARD_TAUNT
			));

	protected static final Set<Attribute> HERO_ATTRIBUTES = new HashSet<>(
			Arrays.asList(
					Attribute.HP,
					Attribute.MAX_HP,
					Attribute.BASE_HP,
					Attribute.ARMOR,
					Attribute.TAUNT
			));

	private CardDesc desc;
	private List<SpellDesc> deathrattleEnchantments = new ArrayList<>();
	private List<EnchantmentDesc> storedEnchantments = new ArrayList<>();
	private List<BattlecryDesc> battlecryEnchantments = new ArrayList<>();

	protected Card() {
		attributes = new CardAttributeMap(this);
	}

	/**
	 * Creates a card from a description of a card.
	 *
	 * @param desc The Card description.
	 */
	public Card(CardDesc desc) {
		this();
		// Save a reference to the description for later use.
		setDesc(desc);
	}

	/**
	 * Creates a minion from the attributes written on the card.
	 * <p>
	 * By default, all the attributes written on the card except those contained in {@link #IGNORED_MINION_ATTRIBUTES} are
	 * copied onto the minion created here. The {@link Attribute#REMOVES_SELF_AT_END_OF_TURN} attribute is also removed if
	 * it was present on the card.
	 * <p>
	 * Text is applied using the {@link Card#applyText(Actor)} method, which works for all actors.
	 *
	 * @return A new Minion instance.
	 */
	public Minion summon() {
		if (getCardType() != CardType.MINION) {
			throw new UnsupportedOperationException("not minion");
		}

		Minion minion = new Minion(this);
		for (Attribute gameTag : getAttributes().unsafeKeySet()) {
			if (!IGNORED_MINION_ATTRIBUTES.contains(gameTag)) {
				minion.getAttributes().put(gameTag, getAttributes().get(gameTag));
			}
		}

		// Card buffs
		if (hasAttribute(Attribute.CARD_TAUNT)) {
			minion.setAttribute(Attribute.TAUNT);
		}

		minion.getAttributes().remove(Attribute.REMOVES_SELF_AT_END_OF_TURN);

		applyText(minion);

		minion.setBaseAttack(getBaseAttack());
		minion.setAttack(getAttack());
		minion.setHp(getHp());
		minion.setMaxHp(getHp());
		minion.setBaseHp(getBaseHp());
		minion.setHp(minion.getMaxHp());

		return minion;
	}

	/**
	 * Creates a hero entity from the text on the card. Works similarly to {@link #summon()}, except for heroes.
	 *
	 * @return A new hero instance.
	 */
	public Hero createHero() {
		if (getCardType() != CardType.HERO) {
			logger.warn("createEnchantments {}: Trying to interpret a {} as an hero", this, getCardType());
		}

		Card heroPower = CardCatalogue.getCardById(getDesc().getHeroPower());
		Hero hero = new Hero(this, heroPower);
		for (Attribute gameTag : getAttributes().unsafeKeySet()) {
			if (HERO_ATTRIBUTES.contains(gameTag)) {
				hero.getAttributes().put(gameTag, getAttributes().get(gameTag));
			}
		}

		applyText(hero);

		return hero;
	}

	/**
	 * Iterates through all the enchantments written on this card and instantiates them.
	 * <p>
	 * Deathrattles are not supported.
	 *
	 * @return A list of enchantments (auras, triggers, etc.)
	 */
	public List<Enchantment> createEnchantments() {
		/*
		if (getCardType() != CardType.ENCHANTMENT) {
			logger.warn("createEnchantments {}: Trying to interpret a {} as an enchantment", this, getCardType());
		}
		*/

		List<Enchantment> enchantments = new ArrayList<>(4);
		if (getDesc().getTrigger() != null) {
			enchantments.add(getDesc().getTrigger().create());
		}

		if (getDesc().getTriggers() != null) {
			for (EnchantmentDesc trigger : getDesc().getTriggers()) {
				enchantments.add(trigger.create());
			}
		}

		if (getDesc().getDeathrattle() != null) {
			logger.warn("createEnchantments {}: Currently creating a deathrattle using a MinionDeathTrigger is not supported", getCardId());
			EnchantmentDesc deathrattleDesc = new EnchantmentDesc();
			deathrattleDesc.spell = getDesc().getDeathrattle().clone();
			// TODO: This doesn't actually trigger maybe?
			deathrattleDesc.eventTrigger = MinionDeathTrigger.create();
			deathrattleDesc.maxFires = 1;
			enchantments.add(deathrattleDesc.create());
		}

		if (getDesc().getAura() != null) {
			enchantments.add(getDesc().getAura().create());
		}

		if (getDesc().getAuras() != null && getDesc().getAuras().length > 0) {
			for (AuraDesc auraDesc : getDesc().getAuras()) {
				enchantments.add(auraDesc.create());
			}
		}

		// If there is no enchantment, create a dummy one
		if (enchantments.isEmpty()) {
			EnchantmentDesc enchantmentDesc = new EnchantmentDesc();
			enchantmentDesc.spell = NullSpell.create();
			enchantmentDesc.eventTrigger = NullTrigger.create();
			enchantments.add(enchantmentDesc.create());
		}

		for (Enchantment enchantment : enchantments) {
			enchantment.setOwner(getOwner());
			enchantment.setSourceCard(this);
		}

		// Add the attributes to the first enchantment
		if (getDesc().getAttributes() != null && !getDesc().getAttributes().isEmpty()) {
			enchantments.get(0)
					.getAttributes().putAll(getDesc().getAttributes());
		}

		return enchantments;
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
		clone.attributes = ((CardAttributeMap) this.attributes).clone();
		clone.getAttributes().setCard(clone);
		clone.setDesc(this.getDesc());
		clone.setDeathrattleEnchantments(new ArrayList<>());
		clone.setBattlecryEnchantments(new ArrayList<>());
		getDeathrattleEnchantments().forEach(de -> clone.getDeathrattleEnchantments().add(de.clone()));
		getBattlecryEnchantments().forEach(be -> clone.addBattlecry(be.clone()));
		clone.setStoredEnchantments(new ArrayList<>());
		clone.getStoredEnchantments().addAll(getStoredEnchantments());
		return clone;
	}

	protected void setBattlecryEnchantments(ArrayList<BattlecryDesc> objects) {
		battlecryEnchantments = objects;
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
	 * @return The card ID as set by an aura, a permanent effect or in the description.
	 */
	public String getCardId() {
		String cardId = getAttributes().getOverrideCardId();
		if (cardId == null) {
			return desc.getId();
		} else {
			return cardId;
		}
	}

	public String getHero() {
		return getDesc().getHero();
	}

	/**
	 * Gets the set that the card belongs to.
	 *
	 * @return The card set
	 */
	public String getCardSet() {
		return getDesc().getSet();
	}

	/**
	 * Gets the sets that this card lists.
	 *
	 * @return The card sets
	 */
	public String[] getCardSets() {
		return getDesc().getSets();
	}

	/**
	 * Gets the card type, like Hero, Secret, Spell or Minion.
	 *
	 * @return The card type
	 */
	public CardType getCardType() {
		return getDesc().getType();
	}

	/**
	 * Gets the hero class that this card belongs to. Valid classes include ANY (neutral) or any of the main 9 classes.
	 *
	 * @return The hero class
	 */
	public String getHeroClass() {
		return (String) getAttributes().getOrDefault(Attribute.HERO_CLASS, getDesc().getHeroClass());
	}

	public void setHeroClass(String heroClass) {
		getAttributes().put(Attribute.HERO_CLASS, heroClass);
	}

	/**
	 * Some cards have multiple hero classes. This field stores those multiple classes when they are defined.
	 *
	 * @return The hero classes (the gang)
	 */
	public String[] getHeroClasses() {
		return getDesc().getHeroClasses();
	}

	/**
	 * Gets a copy of the card with some attributes like its attack or HP bonuses and mana cost modifiers removed. The ID
	 * and owner is set to unassigned.
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
		// Copies lose their attribute enchantments
		if (!hasAttribute(Attribute.KEEPS_ENCHANTMENTS)) {
			copy.getDeathrattleEnchantments().clear();
			copy.getAttributes().remove(Attribute.ATTACK_BONUS);
			copy.getAttributes().remove(Attribute.HP_BONUS);
		}
		copy.getAttributes().remove(Attribute.STARTED_IN_HAND);
		copy.getAttributes().remove(Attribute.STARTED_IN_DECK);
		copy.getAttributes().remove(Attribute.BEING_PLAYED);
		copy.getAttributes().remove(Attribute.TRANSFORM_REFERENCE);
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
	 * @return The value of the {@link Attribute#DESCRIPTION} on this {@link Card}, if it is not null. Otherwise, the
	 *    {@link CardDesc#description} field.
	 */
	public String getDescription() {
		// Cleanup the html tags that appear in the description
		final String description = hasAttribute(Attribute.DESCRIPTION) ? (String) getAttribute(Attribute.DESCRIPTION) : getDesc().getDescription();
		if (description == null || description.isEmpty()) {
			return "";
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
	 * @param player  The {@link Player} whose point of view should be considered for the cost. This is almost always the
	 *                owner.
	 * @return The cost.
	 * @see net.demilich.metastone.game.logic.GameLogic#getModifiedManaCost(Player, Card) for the best method to get the
	 * 		cost of a card.
	 */
	@Suspendable
	public int getManaCost(GameContext context, Player player) {
		int actualManaCost = getBaseManaCost();
		if (getManaCostModifier() != null) {
			actualManaCost -= getManaCostModifier().getValue(context, player, null, this);
		}
		return actualManaCost;
	}

	protected ValueProvider getManaCostModifier() {
		return (ValueProvider) getAttributes().get(Attribute.MANA_COST_MODIFIER);
	}

	/**
	 * A rarity of the card. Rarer cards are generally more powerful; they appear in card packs less frequently; and
	 * {@link Rarity#LEGENDARY} cards can only appear once in a deck.
	 *
	 * @return A {@link Rarity}
	 */
	public Rarity getRarity() {
		return getDesc().getRarity();
	}

	/**
	 * Gets the race of a card. Typically only applies to {@link Card} that summon minions when played.
	 *
	 * @return A {@link Race}
	 */
	@NotNull
	@Override
	public String getRace() {
		return (String) getAttributes().getOrDefault(Attribute.RACE, getDesc().getRace() == null ? Race.NONE : getDesc().getRace());
	}

	/**
	 * Checks if the hero class specified is in its list of hero classes when this card belongs to multiple hero classes.
	 *
	 * @param heroClass The {@link HeroClass} to search.
	 * @return <code>True</code> if this card has the specified class.
	 */
	public boolean hasHeroClass(String heroClass) {
		if (getHeroClasses() != null) {
			for (String h : getHeroClasses()) {
				if (heroClass.equals(h)) {
					return true;
				}
			}
		} else if (Objects.equals(heroClass, getHeroClass())) {
			return true;
		} else if (Objects.equals(heroClass, HeroClass.INHERIT)) {
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
		return getDesc().isCollectible();
	}

	/**
	 * Create an action representing playing the card.
	 *
	 * @return An action that should be evaluated by {@link net.demilich.metastone.game.logic.GameLogic#performGameAction(int,
	 *    GameAction)}.
	 */
	@Suspendable
	public PlayCardAction play() {
		if (hasChoices()) {
			throw new UnsupportedOperationException("Use the choices interfaces instead.");
		}
		switch (getCardType()) {
			case SPELL:
				return new PlaySpellCardAction(getSpell(), this, getTargetSelection());
			case WEAPON:
				return new PlayWeaponCardAction(getReference());
			case HERO:
				return new PlayHeroCardAction(getReference());
			case MINION:
				return new PlayMinionCardAction(getReference());
			case CHOOSE_ONE:
				throw new UnsupportedOperationException("The method .play() should not be called for ChooseOneCard");
			case HERO_POWER:
				return new HeroPowerAction(getSpell(), this, getTargetSelection());
			case GROUP:
				throw new UnsupportedOperationException("The method .play() should not be called for GroupCard");
			case CLASS:
				throw new UnsupportedOperationException("The method .play() should not be called for ClassCard");
			case FORMAT:
				throw new UnsupportedOperationException("The method .play() should not be called for FormatCard");
		}
		throw new UnsupportedOperationException();
	}

	public boolean hasChoices() {
		return (getChooseOneCardIds() != null && getChooseOneCardIds().length > 0)
				|| (getChooseOneBattlecries() != null && getChooseOneBattlecries().length > 0);
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
		if (getAttributes().getOverrideCardId() == null) {
			return desc;
		}
		// Prevents copying here
		return CardCatalogue.getRecords().get(getAttributes().getOverrideCardId()).getDesc();
	}

	/**
	 * The card source of a card is itself.
	 *
	 * @return A reference to this instance.
	 */
	@Override
	public Card getSourceCard() {
		return this;
	}

	/**
	 * Retrieves the card's triggers that are active while the card is in the {@link
	 * net.demilich.metastone.game.targeting.Zones#HAND} or {@link net.demilich.metastone.game.targeting.Zones#HERO_POWER}.
	 *
	 * @return The triggers
	 */
	public EnchantmentDesc[] getPassiveTriggers() {
		return (EnchantmentDesc[]) getAttribute(Attribute.PASSIVE_TRIGGERS);
	}

	/**
	 * Indicates the card has effects that need to be persisted to a database between matches.
	 *
	 * @return {@code true} if the card has effects that need to be persisted
	 */
	@Override
	public boolean hasPersistentEffects() {
		return getDesc().getLegacy() == null ? false : getDesc().getLegacy();
	}

	/**
	 * Returns the triggers that are active when the card is in the deck.
	 *
	 * @return A list of {@link EnchantmentDesc} objects.
	 */
	public EnchantmentDesc[] getDeckTriggers() {
		return (EnchantmentDesc[]) getAttribute(Attribute.DECK_TRIGGERS);
	}

	public void setDesc(CardDesc desc) {
		this.desc = desc;
	}

	/**
	 * Retrieves the spell effects for this card. Secrets and quests automatically wrap their effects in a {@link
	 * AddSecretSpell} and {@link AddQuestSpell}.
	 *
	 * @return The spell
	 */
	public SpellDesc getSpell() {
		if (isSecret() && getDesc().getSecret() != null) {
			return AddSecretSpell.create(new Secret(getDesc().getSecret().create(), getDesc().getSpell(), this));
		} else if (isQuest() && getDesc().getQuest() != null) {
			return AddQuestSpell.create(new Quest(getDesc().getQuest().create(), getDesc().getSpell(), this, getDesc().getCountUntilCast(), getDesc().isCountByValue()));
		} else {
			return getDesc().getSpell();
		}
	}

	/**
	 * Retrieves the card's target requirements.
	 * <p>
	 * This method does <b>not</b> return a reasonable answer for non-spell cards. While a minion card's target
	 * requirement is technically friendly minions, it can be played even if there is no friendly minion on the board.
	 *
	 * @return The target requirements
	 */
	public TargetSelection getTargetSelection() {
		return (TargetSelection) getAttributes().getOrDefault(Attribute.TARGET_SELECTION, getDesc().getTargetSelection() == null ? TargetSelection.NONE : getDesc().getTargetSelection());
	}

	/**
	 * Indicates this card plays an actor, like a minion, weapon or hero, from the hand.
	 *
	 * @return {@code true} if this is an actor card
	 */
	public boolean isActor() {
		return getCardType() == CardType.MINION || getCardType() == CardType.WEAPON || getCardType() == CardType.HERO;
	}

	/**
	 * Creates an instance of the appropriate actor from this card.
	 *
	 * @return The {@link Actor} entity or {@code null} if the card could not have produced an actor.
	 */
	public Actor actor() {
		switch (getCardType()) {
			case MINION:
				return summon();
			case WEAPON:
				return createWeapon();
			case HERO:
				return createHero();
		}

		return null;
	}

	/**
	 * Retrieves the play options from choose one cards. If the card is not actually a choose one card, it returns an
	 * empty array.
	 *
	 * @return The play options array of length 2
	 */
	@Override
	@NotNull
	public PlayCardAction[] playOptions() {
		switch (getCardType()) {
			case HERO_POWER:
			case CHOOSE_ONE:
			case SPELL:
				if (getChooseOneCardIds() == null ||
						getChooseOneCardIds().length == 0) {
					break;
				}

				PlayCardAction[] spellActions = new PlayCardAction[getChooseOneCardIds().length];
				for (int i = 0; i < getChooseOneCardIds().length; i++) {
					String cardId = getChooseOneCardIds()[i];
					Card card = CardCatalogue.getCardById(cardId);
					PlayCardAction cardAction;

					if (getCardType() == CardType.CHOOSE_ONE || getCardType() == CardType.SPELL) {
						cardAction = new PlayChooseOneCardAction(card.getSpell(), this, cardId, card.getTargetSelection());
					} else {
						cardAction = new HeroPowerAction(card.getSpell(), this, getTargetSelection(), card);
					}

					cardAction.setChooseOneOptionIndex(i);
					spellActions[i] = cardAction;
				}
				return spellActions;
			case HERO:
			case WEAPON:
			case MINION:
				if (getChooseOneBattlecries() == null ||
						getChooseOneBattlecries().length == 0) {
					break;
				}

				PlayCardAction[] actions = new PlayCardAction[getChooseOneBattlecries().length];
				for (int i = 0; i < getChooseOneBattlecries().length; i++) {
					BattlecryDesc battlecry = getChooseOneBattlecries()[i];
					PlayCardAction option;
					if (getCardType() == CardType.MINION) {
						option = new PlayMinionCardAction(getReference(), battlecry);
					} else if (getCardType() == CardType.HERO) {
						option = new PlayHeroCardAction(getReference());
						((HasBattlecry) option).setBattlecry(battlecry);
					} else if (getCardType() == CardType.WEAPON) {
						option = new PlayWeaponCardAction(getReference(), battlecry);
					} else {
						throw new UnsupportedOperationException("playOptions()");
					}
					option.setChooseOneOptionIndex(i);
					actions[i] = option;
				}
				return actions;
			case GROUP:
				throw new UnsupportedOperationException("group");
			case CLASS:
				throw new UnsupportedOperationException("class");
			case FORMAT:
				throw new UnsupportedOperationException("format");
		}
		return new PlayCardAction[0];
	}

	/**
	 * Retrieves the card IDs of the choices corresponding to this choose one spell.
	 *
	 * @return The array of card IDs of the choices of length 2
	 */
	public String[] getChooseOneCardIds() {
		return getDesc().getChooseOneCardIds();
	}

	/**
	 * Retreives the battlecries of the choices corresponding to this choose one actor.
	 *
	 * @return The battlecry array of length 2
	 */
	public BattlecryDesc[] getChooseOneBattlecries() {
		return getDesc().getChooseOneBattlecries();
	}

	/**
	 * Returns the action that executes both choose ones for this spell or actor card.
	 *
	 * @return The action
	 */
	@Override
	@Nullable
	public PlayCardAction playBothOptions() {
		if (getChooseBothCardId() == null &&
				getDesc().getChooseBothBattlecry() == null) {
			return null;
		}

		PlayCardAction action = null;
		switch (getCardType()) {
			case HERO_POWER:
				action = new HeroPowerAction(CardCatalogue.getCardById(getChooseBothCardId()).getSpell(), this, getTargetSelection(), CardCatalogue.getCardById(getChooseBothCardId()));
				break;
			case CHOOSE_ONE:
			case SPELL:
				Card card = CardCatalogue.getCardById(getChooseBothCardId());
				action = new PlayChooseOneCardAction(card.getSpell(), this, getChooseBothCardId(), card.getTargetSelection());
				break;
			case WEAPON:
				action = new PlayWeaponCardAction(getReference(), getDesc().getChooseBothBattlecry());
				break;
			case HERO:
				action = new PlayHeroCardAction(getReference(), getDesc().getChooseBothBattlecry());
				break;
			case MINION:
				action = new PlayMinionCardAction(getReference(), getDesc().getChooseBothBattlecry());
				break;
			case GROUP:
				throw new UnsupportedOperationException("group");
		}
		action.setChooseOneOptionIndex(-1);
		return action;
	}

	/**
	 * Gets the card ID of the card that executes both choose one effects for this choose one card.
	 *
	 * @return The card ID
	 */
	public String getChooseBothCardId() {
		return getDesc().getChooseBothCardId();
	}

	/**
	 * Does this card have both choose one options?
	 *
	 * @return {@code true} if this card specified a choose both action.
	 */
	@Override
	public boolean hasBothOptions() {
		return getDesc().getChooseBothBattlecry() != null || getDesc().getChooseBothCardId() != null;
	}


	/**
	 * Gets the weapon equipped by a {@link EquipWeaponSpell} in this hero's battlecry.
	 *
	 * @return A weapon card, or {@code null} if none was found.
	 */
	public Weapon createWeapon() {
		if (getCardType() != CardType.WEAPON) {
			throw new UnsupportedOperationException("not weapon");
		}
		Weapon weapon = new Weapon(this);
		// assign battlecry if there is one specified
		for (Attribute gameTag : getAttributes().unsafeKeySet()) {
			if (!IGNORED_MINION_ATTRIBUTES.contains(gameTag)) {
				weapon.getAttributes().put(gameTag, getAttributes().get(gameTag));
			}
		}
		weapon.setAttack(getDamage());
		weapon.setBaseAttack(getBaseDamage());
		weapon.setMaxHp(getDurability());
		weapon.setHp(weapon.getMaxDurability());
		weapon.setBaseHp(getBaseDurability());
		weapon.getAttributes().remove(Attribute.REMOVES_SELF_AT_END_OF_TURN);
		applyText(weapon);

		weapon.setOnEquip(getDesc().getOnEquip());
		weapon.setOnUnequip(getDesc().getOnUnequip());
		return weapon;

	}

	/**
	 * Gets the weapon that is equipped as a side-effect of playing this actor from the hand, <b>not</b> the underlying
	 * weapon actor represented by playing this card.
	 * <p>
	 * Useful for determining the "attack" of a {@link CardType#HERO} while the card is in the player's hand/deck.
	 *
	 * @return The weapon card
	 */
	public Card getWeapon() {
		if (getDesc().getBattlecry() == null) {
			return null;
		}

		if (getDesc().getBattlecry().getSpell() == null) {
			return null;
		}

		// Return the first weapon we find equipped by the battlecry
		SpellDesc spell = getDesc().getBattlecry().getSpell();
		SpellDesc equipWeaponSpell = spell.subSpells()
				.stream()
				.filter(p -> p.getDescClass().equals(EquipWeaponSpell.class))
				.findFirst().orElse(null);

		if (equipWeaponSpell == null) {
			return null;
		}

		String cardId = equipWeaponSpell.getString(SpellArg.CARD);
		if (cardId == null) {
			return null;
		}

		Card cardById = CardCatalogue.getCardById(cardId);
		if (cardById == null
				|| cardById.getCardType() != CardType.WEAPON) {
			return null;
		}
		return cardById;
	}

	/**
	 * Indicates whether this spell can be cast generally, given its target selection. Does not provide logical answers
	 * for non-spell cards (returns {@code true} in such cases).
	 * <p>
	 * Will return {@code true} even if the player isn't the owner of this card.
	 *
	 * @param context
	 * @param player
	 * @return {@code true} if the card can be cast
	 */
	public boolean canBeCast(GameContext context, Player player) {
		if (isQuest()) {
			return context.getLogic().canPlayQuest(player, this);
		} else if (isSecret()) {
			return context.getLogic().canPlaySecret(player, this);
		}

		Player opponent = context.getOpponent(player);
		TargetSelection selection = hasChoices() || isHeroPower() ?
				getTargetSelection() :
				context.getLogic().processTargetModifiers(play()).getTargetRequirement();
		switch (selection) {
			case ENEMY_MINIONS:
				return context.getMinionCount(opponent) > 0;
			case FRIENDLY_MINIONS:
				return context.getMinionCount(player) > 0;
			case MINIONS:
				return context.getTotalMinionCount() > 0;
			default:
				break;
		}
		if (getCondition() != null) {
			return getCondition().create().isFulfilled(context, player, null, null);
		}
		return true;
	}

	/**
	 * Returns {@code true} if this is a quest.
	 *
	 * @return
	 */
	public boolean isQuest() {
		boolean hasQuestAttribute = getDesc().getAttributes() != null && (boolean) getDesc().getAttributes().getOrDefault(Attribute.QUEST, false);
		return getDesc().getQuest() != null || hasQuestAttribute;
	}

	/**
	 * Given the filter written on this card, indicates whether this spell can be cast on the specified target.
	 * <p>
	 * Used for rolling out actions.
	 *
	 * @param context
	 * @param player
	 * @param target
	 * @return
	 */
	public boolean canBeCastOn(GameContext context, Player player, Entity target) {
		EntityFilter filter = getSpell().getEntityFilter();
		if (filter == null) {
			return true;
		}
		return filter.matches(context, player, target, this);
	}

	/**
	 * Gets the condition written on this card. Cards with failed conditions cannot be played.
	 *
	 * @return
	 */
	public ConditionDesc getCondition() {
		return getDesc().getCondition();
	}

	/**
	 * Indicates if this card was used. Typically intended only for {@link CardType#HERO_POWER} cards.
	 *
	 * @return
	 */
	public int hasBeenUsed() {
		return (int) getAttributes().getOrDefault(Attribute.USED_THIS_TURN, 0);
	}

	/**
	 * Marks this card as used. Typically intended only for {@link CardType#HERO_POWER} cards.
	 */
	public void markUsed() {
		getAttributes().put(Attribute.USED_THIS_TURN, hasBeenUsed() + 1);
	}

	public void setUsed(int used) {
		getAttributes().put(Attribute.USED_THIS_TURN, used);
	}

	/**
	 * Applies this card's effects (everything except mana cost, attack and HP) to the specified actor. Mutates the
	 * provided instance.
	 *
	 * @param instance An actor to apply effects to
	 * @return The provided actor.
	 */
	public Actor applyText(Actor instance) {
		instance.setBattlecry(getDesc().getBattlecry());
		instance.setRace((getAttributes() != null && getAttributes().containsKey(Attribute.RACE)) ?
				(String) getAttribute(Attribute.RACE) :
				getDesc().getRace());

		if (getDesc().getDeathrattle() != null) {
			instance.getAttributes().remove(Attribute.DEATHRATTLES);
			instance.addDeathrattle(getDesc().getDeathrattle());
		}

		if (!getDeathrattleEnchantments().isEmpty()) {
			getDeathrattleEnchantments().forEach(instance::addDeathrattle);
		}

		if (!getBattlecryEnchantments().isEmpty()) {
			getBattlecryEnchantments().forEach(instance::addBattlecry);
		}

		if (getDesc().getTrigger() != null) {
			instance.addEnchantment(getDesc().getTrigger().create());
		}

		if (getDesc().getTriggers() != null) {
			for (EnchantmentDesc trigger : getDesc().getTriggers()) {
				instance.addEnchantment(trigger.create());
			}
		}

		if (getDesc().getAura() != null) {
			final Aura enchantment = getDesc().getAura().create();
			instance.addEnchantment(enchantment);
		}

		if (getDesc().getAuras() != null) {
			for (AuraDesc auraDesc : getDesc().getAuras()) {
				instance.addEnchantment(auraDesc.create());
			}
		}

		if (getDesc().getCardCostModifier() != null) {
			instance.setCardCostModifier(getDesc().getCardCostModifier().create());
		}

		if (getStoredEnchantments() != null) {
			for (EnchantmentDesc storedEnchantment : getStoredEnchantments()) {
				instance.addEnchantment(storedEnchantment.create());
			}
		}

		return instance;
	}

	public int getAttack() {
		return getAttributeValue(Attribute.ATTACK);
	}

	public int getBonusAttack() {
		return getAttributeValue(Attribute.ATTACK_BONUS);
	}

	public int getHp() {
		return getAttributeValue(Attribute.HP);
	}

	public int getBonusHp() {
		return getAttributeValue(Attribute.HP_BONUS);
	}

	public int getBaseAttack() {
		return getAttributeValue(Attribute.BASE_ATTACK);
	}

	public int getBaseHp() {
		return getAttributeValue(Attribute.BASE_HP);
	}

	@Override
	public void addDeathrattle(SpellDesc deathrattle) {
		// TODO: Should Forlorn Stalker affect cards with deathrattle added this way?
		getDeathrattleEnchantments().add(deathrattle);
	}

	public void addBattlecry(BattlecryDesc battlecry) {
		getBattlecryEnchantments().add(battlecry);
	}

	public List<BattlecryDesc> getBattlecryEnchantments() {
		return battlecryEnchantments;
	}

	public void addStoredEnchantment(EnchantmentDesc enchantmentDesc) {
		getStoredEnchantments().add(enchantmentDesc);
	}

	@Override
	public List<SpellDesc> getDeathrattleEnchantments() {
		return deathrattleEnchantments;
	}

	public List<EnchantmentDesc> getStoredEnchantments() {
		return storedEnchantments;
	}

	public boolean hasTrigger() {
		return getDesc().getTrigger() != null || (getDesc().getTriggers() != null && getDesc().getTriggers().length > 0);
	}

	public boolean hasAura() {
		return getDesc().getAura() != null
				|| getDesc().getAuras() != null && getDesc().getAuras().length > 0;
	}

	public boolean hasCardCostModifier() {
		return getDesc().getCardCostModifier() != null;
	}

	public boolean hasBattlecry() {
		return getDesc().getBattlecry() != null;
	}

	public String getBattlecryDescription(int index) {
		if (index < -1 || index >= getChooseOneBattlecries().length) {
			return null;
		}
		if (index == -1) {
			return getDesc().getChooseBothBattlecry().getDescription();
		}
		if (getChooseOneBattlecries()[index] == null) {
			return null;
		}
		return getChooseOneBattlecries()[index].getDescription();
	}

	public String getBattlecryName(int index) {
		if (index == -1) {
			return getDesc().getChooseBothBattlecry().getName();
		}

		if (index < -1 || index >= getChooseOneBattlecries().length) {
			return getBattlecryDescription(index);
		}
		if (getChooseOneBattlecries()[index] == null) {
			return getBattlecryDescription(index);
		}


		final String name = getChooseOneBattlecries()[index].getName();
		return name == null ? getBattlecryDescription(index) : name;
	}

	public String getTransformMinionCardId(int index) {
		BattlecryDesc battlecryOption;
		if (index == -1) {
			battlecryOption = getDesc().getChooseBothBattlecry();
		} else {
			battlecryOption = getChooseOneBattlecries()[index];
		}

		if (battlecryOption == null) {
			return null;
		}

		SpellDesc spell = battlecryOption.getSpell();
		if (spell == null) {
			return null;
		}

		if (TransformMinionSpell.class.isAssignableFrom(spell.getDescClass())) {
			return spell.getString(SpellArg.CARD);
		}

		return null;
	}

	public int getDamage() {
		return getAttack();
	}

	public int getBonusDamage() {
		return getBonusAttack();
	}

	public int getDurability() {
		return getHp();
	}

	public int getBonusDurability() {
		return getBonusHp();
	}

	public int getBaseDamage() {
		return getBaseAttack();
	}

	public int getBaseDurability() {
		return getBaseHp();
	}

	public int getArmor() {
		return getAttributeValue(Attribute.ARMOR);
	}

	public SpellDesc[] getGroup() {
		return getDesc().getGroup();
	}

	public void setTargetRequirement(TargetSelection targetRequirement) {
		getAttributes().put(Attribute.TARGET_SELECTION, targetRequirement);
	}

	/**
	 * Returns {@code true} if this card is a secret.
	 *
	 * @return
	 */
	public boolean isSecret() {
		boolean hasSecretAttribute = getDesc().getAttributes() != null && (boolean) getDesc().getAttributes().getOrDefault(Attribute.SECRET, false);
		return getDesc().getSecret() != null || hasSecretAttribute;
	}

	public boolean isSpell() {
		return getCardType().isCardType(CardType.SPELL);
	}

	public boolean hasDeathrattle() {
		return getDesc().getDeathrattle() != null
				|| getDeathrattleEnchantments().size() > 0;
	}

	public boolean isChooseOne() {
		return hasChoices();
	}

	/**
	 * Determines how this card should be named.
	 *
	 * @return The value of the {@link Attribute#NAME} attribute, or the underlying {@link CardDesc#name} field.
	 */
	@Override
	public String getName() {
		return (String) getAttributes().getOrDefault(Attribute.NAME, getDesc().getName());
	}

	@Override
	public CardAttributeMap getAttributes() {
		return (CardAttributeMap) attributes;
	}

	public boolean isHeroPower() {
		return getCardType().isCardType(CardType.HERO_POWER);
	}

	@Override
	public int compareTo(@NotNull Entity o) {
		if (o instanceof Card) {
			Card rhs = (Card) o;
			boolean equals = new EqualsBuilder()
					.append(getCardId(), rhs.getCardId())
					.append(getAttributes(), rhs.getAttributes())
					.build();

			if (equals) {
				return 0;
			}
		}

		return super.compareTo(o);
	}

	public Card setDeathrattleEnchantments(List<SpellDesc> deathrattleEnchantments) {
		this.deathrattleEnchantments = deathrattleEnchantments;
		return this;
	}

	public Card setStoredEnchantments(List<EnchantmentDesc> storedEnchantments) {
		this.storedEnchantments = storedEnchantments;
		return this;
	}

	public boolean isBlackText() {
		return getDesc().isBlackText();
	}

	public int[] getColor() {
		return getDesc().getColor();
	}
}
