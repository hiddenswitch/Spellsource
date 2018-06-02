package net.demilich.metastone.game.cards;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.actions.*;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.heroes.Hero;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.entities.weapons.Weapon;
import net.demilich.metastone.game.spells.*;
import net.demilich.metastone.game.spells.aura.Aura;
import net.demilich.metastone.game.spells.desc.BattlecryDesc;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.aura.AuraDesc;
import net.demilich.metastone.game.spells.desc.condition.ConditionDesc;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import net.demilich.metastone.game.spells.trigger.Enchantment;
import net.demilich.metastone.game.spells.trigger.MinionDeathTrigger;
import net.demilich.metastone.game.spells.trigger.NullTrigger;
import net.demilich.metastone.game.spells.trigger.secrets.Quest;
import net.demilich.metastone.game.spells.trigger.secrets.Secret;
import net.demilich.metastone.game.targeting.IdFactory;
import net.demilich.metastone.game.targeting.TargetSelection;
import net.demilich.metastone.game.utils.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.desc.CardDesc;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.EntityType;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.minions.Race;
import net.demilich.metastone.game.spells.desc.trigger.EnchantmentDesc;
import net.demilich.metastone.game.spells.desc.valueprovider.ValueProvider;
import net.demilich.metastone.game.targeting.EntityReference;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.jetbrains.annotations.NotNull;
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
 * net.demilich.metastone.game.entities.minions.Minion} summoned from the hand, this typically corresponds to a {@link
 * Card} in the {@link net.demilich.metastone.game.targeting.Zones#GRAVEYARD}. This saves you from doing many kinds of
 * casts for {@link net.demilich.metastone.game.entities.Actor} objects.
 * @see CardDesc for the class that is the base of the serialized representation of cards.
 */
public class Card extends Entity implements HasChooseOneActions {
	private static Logger logger = LoggerFactory.getLogger(Card.class);

	protected static final Set<Attribute> IGNORED_MINION_ATTRIBUTES = new HashSet<>(
			Arrays.asList(Attribute.PASSIVE_TRIGGERS, Attribute.DECK_TRIGGERS, Attribute.BASE_ATTACK,
					Attribute.BASE_HP, Attribute.SECRET, Attribute.CHOOSE_ONE, Attribute.BATTLECRY, Attribute.COMBO,
					Attribute.TRANSFORM_REFERENCE, Attribute.ECHO, Attribute.AURA_ECHO));

	protected static final Set<Attribute> HERO_ATTRIBUTES = new HashSet<>(
			Arrays.asList(Attribute.HP, Attribute.MAX_HP, Attribute.BASE_HP, Attribute.ARMOR, Attribute.TAUNT));

	private CardDesc desc;
	private List<SpellDesc> deathrattleEnchantments = new ArrayList<>();

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

		if ((hasAttribute(Attribute.ECHO) || hasAttribute(Attribute.AURA_ECHO))
				&& hasAttribute(Attribute.REMOVES_SELF_AT_END_OF_TURN)) {
			minion.getAttributes().remove(Attribute.REMOVES_SELF_AT_END_OF_TURN);
		}

		applyText(minion);

		minion.setBaseAttack(getBaseAttack());
		minion.setAttack(getAttack());
		minion.setHp(getHp());
		minion.setMaxHp(getHp());
		minion.setBaseHp(getBaseHp());
		minion.setHp(minion.getMaxHp());

		return minion;
	}

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

	public List<Enchantment> createEnchantments() {
		if (getCardType() != CardType.ENCHANTMENT) {
			logger.warn("createEnchantments {}: Trying to interpret a {} as an enchantment", this, getCardType());
		}

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
			EnchantmentDesc deathrattleDesc = new EnchantmentDesc();
			deathrattleDesc.spell = getDesc().getDeathrattle().clone();
			deathrattleDesc.eventTrigger = MinionDeathTrigger.create();
			deathrattleDesc.maxFires = 1;
			enchantments.add(deathrattleDesc.create());
		}

		// If there is no enchantment, create a dummy one
		if (enchantments.size() == 0) {
			EnchantmentDesc enchantmentDesc = new EnchantmentDesc();
			enchantmentDesc.spell = NullSpell.create();
			enchantmentDesc.eventTrigger = NullTrigger.create();
			enchantments.add(enchantmentDesc.create());
		}

		for (Enchantment enchantment : enchantments) {
			enchantment.setOwner(getOwner());
			enchantment.setSourceCard(this);
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
		clone.deathrattleEnchantments = new ArrayList<>();
		deathrattleEnchantments.forEach(de -> clone.deathrattleEnchantments.add(de.clone()));
		return clone;
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

	/**
	 * Gets the set that the card belongs to.
	 *
	 * @return
	 */
	public CardSet getCardSet() {
		return getDesc().getSet();
	}

	/**
	 * Gets the card type, like Hero, Secret, Spell or Minion.
	 *
	 * @return
	 */
	public CardType getCardType() {
		return getDesc().getType();
	}

	/**
	 * Gets the hero class that this card belongs to. Valid classes include ANY (neutral) or any of the main 9 classes.
	 *
	 * @return
	 */
	public HeroClass getHeroClass() {
		return (HeroClass) getAttributes().getOrDefault(Attribute.HERO_CLASS, getDesc().getHeroClass());
	}

	public void setHeroClass(HeroClass heroClass) {
		getAttributes().put(Attribute.HERO_CLASS, heroClass);
	}

	/**
	 * Some cards have multiple hero classes. This field stores those multiple classes when they are defined.
	 *
	 * @return
	 */
	public HeroClass[] getHeroClasses() {
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
		copy.getAttributes().remove(Attribute.ATTACK_BONUS);
		copy.getAttributes().remove(Attribute.HP_BONUS);
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
	 * {@link CardDesc#description} field.
	 */
	public String getDescription() {
		// Cleanup the html tags that appear in the description
		final String description = hasAttribute(Attribute.DESCRIPTION) ? (String) getAttribute(Attribute.DESCRIPTION) : getDesc().getDescription();
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
	 * @param player  The {@link Player} whose point of view should be considered for the cost. This is almost always the
	 *                owner.
	 * @return The cost.
	 * @see net.demilich.metastone.game.logic.GameLogic#getModifiedManaCost(Player, Card) for the best method to get the
	 * cost of a card.
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
	@Override
	public Race getRace() {
		return (Race) getAttributes().getOrDefault(Attribute.RACE, getDesc().getRace() == null ? Race.NONE : getDesc().getRace());
	}

	/**
	 * Checks if the hero class specified is in its list of hero classes when this card belongs to multiple hero classes.
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
		return getDesc().isCollectible();
	}

	/**
	 * Create an action representing playing the card.
	 *
	 * @return An action that should be evaluated by {@link net.demilich.metastone.game.logic.GameLogic#performGameAction(int,
	 * GameAction)}.
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

	@Override
	public Card getSourceCard() {
		return this;
	}

	public EnchantmentDesc[] getPassiveTriggers() {
		return (EnchantmentDesc[]) getAttribute(Attribute.PASSIVE_TRIGGERS);
	}

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

	public SpellDesc getSpell() {
		if (isSecret()) {
			return AddSecretSpell.create(new Secret(getDesc().getSecret().create(), getDesc().getSpell(), this));
		} else if (isQuest()) {
			return AddQuestSpell.create(new Quest(getDesc().getQuest().create(), getDesc().getSpell(), this, getDesc().getCountUntilCast()));
		} else {
			return getDesc().getSpell();
		}
	}

	public TargetSelection getTargetSelection() {
		return (TargetSelection) getAttributes().getOrDefault(Attribute.TARGET_SELECTION, getDesc().getTargetSelection() == null ? TargetSelection.NONE : getDesc().getTargetSelection());
	}

	public boolean isActor() {
		return getCardType() == CardType.MINION || getCardType() == CardType.WEAPON || getCardType() == CardType.HERO;
	}

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

	@Override
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
					if (card == null) {
						throw new NullPointerException("card");
					}
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
					BattlecryAction battlecry = getChooseOneBattlecries()[i].toBattlecryAction();
					PlayCardAction option;
					if (getCardType() == CardType.MINION) {
						option = new PlayMinionCardAction(getReference(), battlecry);
					} else if (getCardType() == CardType.HERO) {
						option = new PlayHeroCardAction(getReference());
						((HasBattlecry) option).setBattlecryAction(battlecry);
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
		}
		return new PlayCardAction[0];
	}

	public String[] getChooseOneCardIds() {
		return getDesc().getChooseOneCardIds();
	}

	public BattlecryDesc[] getChooseOneBattlecries() {
		return getDesc().getChooseOneBattlecries();
	}

	@Override
	public PlayCardAction playBothOptions() {
		if (getChooseBothCardId() == null &&
				getDesc().getChooseBothBattlecry() == null) {
			return null;
		}

		PlayCardAction action = null;
		switch (getCardType()) {
			case HERO_POWER:
				action = new HeroPowerAction(CardCatalogue.getCardById(getChooseBothCardId()).getSpell(), this, getTargetSelection());
				break;
			case CHOOSE_ONE:
			case SPELL:
				Card card = (Card) CardCatalogue.getCardById(getChooseBothCardId());
				action = new PlayChooseOneCardAction(card.getSpell(), this, getChooseBothCardId(), card.getTargetSelection());
				break;
			case WEAPON:
				action = new PlayWeaponCardAction(getReference(), getDesc().getChooseBothBattlecry().toBattlecryAction());
				break;
			case HERO:
				action = new PlayHeroCardAction(getReference(), getDesc().getChooseBothBattlecry().toBattlecryAction());
				break;
			case MINION:
				BattlecryDesc battlecryOption = getDesc().getChooseBothBattlecry();
				BattlecryAction battlecry = BattlecryAction.createBattlecry(battlecryOption.getSpell(), battlecryOption.getTargetSelection());
				action = new PlayMinionCardAction(getReference(), battlecry);
				break;
			case GROUP:
				throw new UnsupportedOperationException("group");
		}
		action.setChooseOneOptionIndex(-1);
		return action;
	}

	public String getChooseBothCardId() {
		return getDesc().getChooseBothCardId();
	}

	@Override
	public boolean hasBothOptions() {
		return getDesc().getChooseBothBattlecry() != null;
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

		applyText(weapon);

		weapon.setOnEquip(getDesc().getOnEquip());
		weapon.setOnUnequip(getDesc().getOnUnequip());
		return weapon;

	}

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

	public boolean canBeCast(GameContext context, Player player) {
		if (isQuest()) {
			return context.getLogic().canPlayQuest(player, this);
		} else if (isSecret()) {
			return context.getLogic().canPlaySecret(player, this);
		}

		Player opponent = context.getOpponent(player);
		switch (getTargetSelection()) {
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

	public boolean isQuest() {
		return getDesc().getQuest() != null;
	}

	public boolean canBeCastOn(GameContext context, Player player, Entity target) {
		EntityFilter filter = getSpell().getEntityFilter();
		if (filter == null) {
			return true;
		}
		return filter.matches(context, player, target, this);
	}

	public ConditionDesc getCondition() {
		return getDesc().getCondition();
	}

	public int hasBeenUsed() {
		return (int) getAttributes().getOrDefault(Attribute.USED_THIS_TURN, 0);
	}

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
	@Suspendable
	public Actor applyText(Actor instance) {
		instance.setBattlecry(getDesc().getBattlecryAction());
		instance.setRace((getAttributes() != null && getAttributes().containsKey(Attribute.RACE)) ?
				(Race) getAttribute(Attribute.RACE) :
				getDesc().getRace());

		if (getDesc().getDeathrattle() != null) {
			instance.getAttributes().remove(Attribute.DEATHRATTLES);
			instance.addDeathrattle(getDesc().getDeathrattle());
		}

		if (deathrattleEnchantments.size() > 0) {
			deathrattleEnchantments.forEach(instance::addDeathrattle);
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

	public void addDeathrattle(SpellDesc deathrattle) {
		// TODO: Should Forlorn Stalker affect cards with deathrattle added this way?
		deathrattleEnchantments.add(deathrattle);
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
		if (index < 0 || index >= getChooseOneBattlecries().length) {
			return null;
		}
		if (getChooseOneBattlecries()[index] == null) {
			return null;
		}
		return getChooseOneBattlecries()[index].getDescription();
	}

	public String getBattlecryName(int index) {
		if (index < 0 || index >= getChooseOneBattlecries().length) {
			return getBattlecryDescription(index);
		}
		if (getChooseOneBattlecries()[index] == null) {
			return getBattlecryDescription(index);
		}
		final String name = getChooseOneBattlecries()[index].getName();
		return name == null ? getBattlecryDescription(index) : name;
	}

	public String getTransformMinionCardId(int index) {
		BattlecryDesc battlecryOption = getChooseOneBattlecries()[index];
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

	public boolean isSecret() {
		return getDesc().getSecret() != null;
	}

	public boolean isSpell() {
		return getCardType().isCardType(CardType.SPELL);
	}

	public boolean hasDeathrattle() {
		return getDesc().getDeathrattle() != null
				|| deathrattleEnchantments.size() > 0;
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
}
