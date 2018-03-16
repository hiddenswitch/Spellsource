package net.demilich.metastone.game.cards;

import co.paralleluniverse.fibers.Suspendable;
import io.vertx.core.json.JsonObject;
import net.demilich.metastone.game.actions.*;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.heroes.Hero;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.entities.weapons.Weapon;
import net.demilich.metastone.game.spells.AddQuestSpell;
import net.demilich.metastone.game.spells.AddSecretSpell;
import net.demilich.metastone.game.spells.EquipWeaponSpell;
import net.demilich.metastone.game.spells.TransformMinionSpell;
import net.demilich.metastone.game.spells.aura.Aura;
import net.demilich.metastone.game.spells.desc.BattlecryDesc;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.aura.AuraDesc;
import net.demilich.metastone.game.spells.desc.condition.ConditionDesc;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
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
import net.demilich.metastone.game.spells.desc.trigger.TriggerDesc;
import net.demilich.metastone.game.spells.desc.valueprovider.ValueProvider;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.utils.AttributeMap;

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
 * @see CardParser#parseCard(JsonObject) to see how cards are deserialized from their JSON representation.
 */
public class Card extends Entity implements HasChooseOneActions {
	protected static final Set<Attribute> ignoredAttributes = new HashSet<>(
			Arrays.asList(Attribute.PASSIVE_TRIGGERS, Attribute.DECK_TRIGGER, Attribute.BASE_ATTACK,
					Attribute.BASE_HP, Attribute.SECRET, Attribute.CHOOSE_ONE, Attribute.BATTLECRY, Attribute.COMBO,
					Attribute.TRANSFORM_REFERENCE));

	protected static final Set<Attribute> inheritedAttributes = new HashSet<>(
			Arrays.asList(Attribute.HP, Attribute.MAX_HP, Attribute.BASE_HP, Attribute.ARMOR));

	private CardDesc desc;
	private List<SpellDesc> deathrattleEnchantments = new ArrayList<>();

	protected Card() {
		super();
	}

	/**
	 * Creates a card from a description of a card.
	 *
	 * @param desc The Card description.
	 */
	public Card(CardDesc desc) {
		// Save a reference to the description for later use.
		setDesc(desc);
		setAttribute(Attribute.BASE_MANA_COST, desc.baseManaCost);

		if (desc.attributes != null) {
			getAttributes().putAll(desc.attributes);
		}

		if (desc.manaCostModifier != null) {
			getAttributes().put(Attribute.MANA_COST_MODIFIER, desc.manaCostModifier.create());
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

		if (getCardType() == CardType.WEAPON) {
			setAttribute(Attribute.BASE_ATTACK, desc.damage);
			setAttribute(Attribute.ATTACK, desc.damage);
			setAttribute(Attribute.BASE_HP, desc.durability);
			setAttribute(Attribute.HP, desc.durability);
			setAttribute(Attribute.MAX_HP, desc.durability);
		} else if (getCardType() == CardType.MINION) {
			setAttribute(Attribute.BASE_ATTACK, desc.baseAttack);
			setAttribute(Attribute.ATTACK, desc.baseAttack);
			setAttribute(Attribute.BASE_HP, desc.baseHp);
			setAttribute(Attribute.HP, desc.baseHp);
			setAttribute(Attribute.MAX_HP, desc.baseHp);
		} else if (getCardType() == CardType.HERO) {
			setAttribute(Attribute.BASE_HP, getAttributeValue(Attribute.MAX_HP));

		}

		if (desc.race != null) {
			setRace(desc.race);
		}

		if (desc.secret != null) {
			setAttribute(Attribute.SECRET);
		}

		if (desc.quest != null) {
			setAttribute(Attribute.QUEST);
		}
	}

	public Minion summon() {
		if (getCardType() != CardType.MINION) {
			throw new UnsupportedOperationException("not minion");
		}

		Minion minion = new Minion(this);
		for (Attribute gameTag : getAttributes().keySet()) {
			if (!ignoredAttributes.contains(gameTag)) {
				minion.setAttribute(gameTag, getAttribute(gameTag));
			}
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
		Card heroPower = CardCatalogue.getCardById(getDesc().heroPower);
		Hero hero = new Hero(this, heroPower);
		for (Attribute gameTag : getAttributes().keySet()) {
			if (inheritedAttributes.contains(gameTag)) {
				hero.setAttribute(gameTag, getAttribute(gameTag));
			}
		}

		applyText(hero);

		return hero;
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
		clone.desc = this.desc;
		clone.setAttributes(new AttributeMap(getAttributes()));
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
	 * @return
	 */
	public String getCardId() {
		return ((String) getAttributes().getOrDefault(Attribute.AURA_CARD_ID, getDesc().id)).toLowerCase();
	}

	/**
	 * Gets the set that the card belongs to.
	 *
	 * @return
	 */
	public CardSet getCardSet() {
		return getDesc().set;
	}

	/**
	 * Gets the card type, like Hero, Secret, Spell or Minion.
	 *
	 * @return
	 */
	public CardType getCardType() {
		return getDesc().type;
	}

	/**
	 * Gets the hero class that this card belongs to. Valid classes include ANY (neutral) or any of the main 9 classes.
	 *
	 * @return
	 */
	public HeroClass getHeroClass() {
		return (HeroClass) getAttributes().getOrDefault(Attribute.HERO_CLASS, getDesc().heroClass);
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
		return getDesc().heroClasses;
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
		final String description = hasAttribute(Attribute.DESCRIPTION) ? (String) getAttribute(Attribute.DESCRIPTION) : getDesc().description;
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
		return getDesc().rarity;
	}

	/**
	 * Gets the race of a card. Typically only applies to {@link Card} that summon minions when played.
	 *
	 * @return A {@link Race}
	 */
	public Race getRace() {
		return (Race) getAttributes().getOrDefault(Attribute.RACE, getDesc().race == null ? Race.NONE : getDesc().race);
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
		return getDesc().collectible;
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
		return getDesc().legacy == null ? false : getDesc().legacy;
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

	public void setDesc(CardDesc desc) {
		this.desc = desc;
	}

	public SpellDesc getSpell() {
		if (isSecret()) {
			return AddSecretSpell.create(new Secret(getDesc().secret.create(), getDesc().spell, this));
		} else if (isQuest()) {
			return AddQuestSpell.create(new Quest(getDesc().quest.create(), getDesc().spell, this, getDesc().countUntilCast));
		} else {
			return getDesc().spell;
		}
	}

	public TargetSelection getTargetSelection() {
		return (TargetSelection) getAttributes().getOrDefault(Attribute.TARGET_SELECTION, getDesc().targetSelection == null ? TargetSelection.NONE : getDesc().targetSelection);
	}

	@Override
	public PlayCardAction[] playOptions() {
		switch (getCardType()) {
			case HERO_POWER:
			case CHOOSE_ONE:
			case SPELL:
				PlayCardAction[] spellActions = new PlayCardAction[getChooseOneCardIds().length];
				for (int i = 0; i < getChooseOneCardIds().length; i++) {
					String cardId = getChooseOneCardIds()[i];
					Card card = CardCatalogue.getCardById(cardId);
					if (card == null) {
						throw new NullPointerException("cardId");
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
				PlayCardAction[] actions = new PlayCardAction[getChooseOneBattlecries().length];
				for (int i = 0; i < getChooseOneBattlecries().length; i++) {
					BattlecryAction battlecry = getChooseOneBattlecries()[i].toBattlecryAction();
					PlayCardAction option;
					if (getCardType() == CardType.MINION) {
						option = new PlayMinionCardAction(getReference(), battlecry);
					} else if (getCardType() == CardType.HERO) {
						option = new PlayHeroCardChooseOneAction(this, getReference());
						((PlayHeroCardChooseOneAction) option).setBattlecryAction(battlecry);
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
		return getDesc().chooseOneCardIds;
	}

	public BattlecryDesc[] getChooseOneBattlecries() {
		return getDesc().chooseOneBattlecries;
	}

	@Override
	public PlayCardAction playBothOptions() {
		switch (getCardType()) {
			case HERO_POWER:
				return new HeroPowerAction(CardCatalogue.getCardById(getChooseBothCardId()).getSpell(), this, getTargetSelection());
			case CHOOSE_ONE:
			case SPELL:
				Card card = (Card) CardCatalogue.getCardById(getChooseBothCardId());
				return new PlayChooseOneCardAction(card.getSpell(), this, getChooseBothCardId(), card.getTargetSelection());
			case WEAPON:
				return new PlayWeaponCardAction(getReference(), getDesc().chooseBothBattlecry.toBattlecryAction());
			case HERO:
				return new PlayHeroCardAction(getReference(), getDesc().chooseBothBattlecry.toBattlecryAction());
			case MINION:
				BattlecryDesc battlecryOption = getDesc().chooseBothBattlecry;
				BattlecryAction battlecry = BattlecryAction.createBattlecry(battlecryOption.spell, battlecryOption.getTargetSelection());
				return new PlayMinionCardAction(getReference(), battlecry);
			case GROUP:
				throw new UnsupportedOperationException("group");
		}
		throw new RuntimeException();
	}

	public String getChooseBothCardId() {
		return getDesc().chooseBothCardId;
	}

	@Override
	public boolean hasBothOptions() {
		return getDesc().chooseBothBattlecry != null;
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
		for (Attribute gameTag : getAttributes().keySet()) {
			if (!ignoredAttributes.contains(gameTag)) {
				weapon.setAttribute(gameTag, getAttribute(gameTag));
			}
		}
		weapon.setAttack(getDamage());
		weapon.setBaseAttack(getBaseDamage());
		weapon.setMaxHp(getDurability());
		weapon.setHp(weapon.getMaxDurability());
		weapon.setBaseHp(getBaseDurability());

		applyText(weapon);

		weapon.setOnEquip(getDesc().onEquip);
		weapon.setOnUnequip(getDesc().onUnequip);
		return weapon;

	}

	public Card getWeapon() {
		if (getDesc().battlecry == null) {
			return null;
		}

		if (getDesc().battlecry.spell == null) {
			return null;
		}

		// Return the first weapon we find equipped by the battlecry
		SpellDesc spell = getDesc().battlecry.spell;
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
		return getDesc().quest != null;
	}

	public boolean canBeCastOn(GameContext context, Player player, Entity target) {
		EntityFilter filter = getSpell().getEntityFilter();
		if (filter == null) {
			return true;
		}
		return filter.matches(context, player, target, this);
	}

	public ConditionDesc getCondition() {
		return getDesc().condition;
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
		instance.setBattlecry(desc.getBattlecryAction());
		instance.setRace((getAttributes() != null && getAttributes().containsKey(Attribute.RACE)) ?
				(Race) getAttribute(Attribute.RACE) :
				desc.race);

		if (desc.deathrattle != null) {
			instance.getAttributes().remove(Attribute.DEATHRATTLES);
			instance.addDeathrattle(desc.deathrattle);
		}

		if (deathrattleEnchantments.size() > 0) {
			deathrattleEnchantments.forEach(instance::addDeathrattle);
		}

		if (desc.trigger != null) {
			instance.addEnchantment(desc.trigger.create());
		}

		if (desc.triggers != null) {
			for (TriggerDesc trigger : desc.triggers) {
				instance.addEnchantment(trigger.create());
			}
		}

		if (desc.aura != null) {
			final Aura enchantment = desc.aura.create();
			instance.addEnchantment(enchantment);
		}

		if (desc.auras != null) {
			for (AuraDesc auraDesc : desc.auras) {
				instance.addEnchantment(auraDesc.create());
			}
		}

		if (desc.cardCostModifier != null) {
			instance.setCardCostModifier(desc.cardCostModifier.create());
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
		return getDesc().trigger != null || (getDesc().triggers != null && getDesc().triggers.length > 0);
	}

	public boolean hasAura() {
		return getDesc().aura != null
				|| getDesc().auras != null && getDesc().auras.length > 0;
	}

	public boolean hasCardCostModifier() {
		return getDesc().cardCostModifier != null;
	}

	public boolean hasBattlecry() {
		return getDesc().battlecry != null;
	}

	public void setRace(Race race) {
		setAttribute(Attribute.RACE, race);
	}


	public String getBattlecryDescription(int index) {
		if (index < 0 || index >= getChooseOneBattlecries().length) {
			return null;
		}
		if (getChooseOneBattlecries()[index] == null) {
			return null;
		}
		return getChooseOneBattlecries()[index].description;
	}

	public String getBattlecryName(int index) {
		if (index < 0 || index >= getChooseOneBattlecries().length) {
			return getBattlecryDescription(index);
		}
		if (getChooseOneBattlecries()[index] == null) {
			return getBattlecryDescription(index);
		}
		final String name = getChooseOneBattlecries()[index].name;
		return name == null ? getBattlecryDescription(index) : name;
	}

	public String getTransformMinionCardId(int index) {
		BattlecryDesc battlecryOption = getChooseOneBattlecries()[index];
		if (battlecryOption == null) {
			return null;
		}

		SpellDesc spell = battlecryOption.spell;
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
		return getDesc().group;
	}

	public void setTargetRequirement(TargetSelection targetRequirement) {
		getAttributes().put(Attribute.TARGET_SELECTION, targetRequirement);
	}

	public boolean isSecret() {
		return getDesc().secret != null;
	}

	public boolean isSpell() {
		return getCardType().isCardType(CardType.SPELL);
	}

	public boolean hasDeathrattle() {
		return getDesc().deathrattle != null
				|| deathrattleEnchantments.size() > 0;
	}

	public boolean isChooseOne() {
		return hasChoices();
	}

	@Override
	public String getName() {
		return (String) getAttributes().getOrDefault(Attribute.NAME, getDesc().name);
	}
}
