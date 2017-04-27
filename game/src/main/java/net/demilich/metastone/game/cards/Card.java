package net.demilich.metastone.game.cards;

import co.paralleluniverse.fibers.Suspendable;
import com.google.gson.annotations.SerializedName;
import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.PlayCardAction;
import net.demilich.metastone.game.cards.desc.CardDesc;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.EntityType;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.minions.Race;
import net.demilich.metastone.game.spells.desc.valueprovider.ValueProvider;
import net.demilich.metastone.game.targeting.CardLocation;
import net.demilich.metastone.game.targeting.CardReference;
import net.demilich.metastone.game.targeting.IdFactory;
import net.demilich.metastone.game.utils.AttributeMap;

public abstract class Card extends Entity {
	private static final long serialVersionUID = 1L;

	private String description = "";
	@SerializedName("cardType2")
	private CardType cardType;
	private CardSet cardSet;
	private Rarity rarity;
	private HeroClass heroClass;
	private HeroClass[] heroClasses;
	private boolean collectible = true;
	private CardLocation location;
	private ValueProvider manaCostModifier;
	private String cardId;
	private CardDesc desc;

	public Card() {
		super();
	}

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
		if (desc.heroClasses != null) {
			heroClasses = desc.heroClasses;
		}

		setAttribute(Attribute.BASE_MANA_COST, desc.baseManaCost);
		if (desc.attributes != null) {
			getAttributes().putAll(desc.attributes);
		}

		if (desc.manaCostModifier != null) {
			manaCostModifier = desc.manaCostModifier.create();
		}

		if (desc.passiveTrigger != null) {
			getAttributes().put(Attribute.PASSIVE_TRIGGER, desc.passiveTrigger);
		}

		if (desc.deckTrigger != null) {
			getAttributes().put(Attribute.DECK_TRIGGER, desc.deckTrigger);
		}
	}

	@Override
	public Card clone() {
		Card clone = (Card) super.clone();
		clone.setAttributes(new AttributeMap(getAttributes()));
		return clone;
	}

	public boolean evaluateExpression(String operator, int value1, int value2) {
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

	public int getBaseManaCost() {
		return getAttributeValue(Attribute.BASE_MANA_COST);
	}

	public String getCardId() {
		return cardId;
	}

	public CardReference getCardReference() {
		return new CardReference(getOwner(), getCardLocation(), getId(), getName());
	}

	public CardSet getCardSet() {
		return cardSet;
	}

	public CardType getCardType() {
		return cardType;
	}

	public HeroClass getHeroClass() {
		return heroClass;
	}

	public HeroClass[] getHeroClasses() {
		return heroClasses;
	}

	public Card getCopy() {
		Card copy = clone();
		copy.setId(IdFactory.UNASSIGNED);
		copy.setLocation(CardLocation.PENDING);
		copy.getAttributes().remove(Attribute.ATTACK_BONUS);
		copy.getAttributes().remove(Attribute.HP_BONUS);
		copy.getAttributes().remove(Attribute.MANA_COST_MODIFIER);
		copy.resetEntityLocations();
		return copy;
	}

	public String getDescription() {
		// Cleanup the html tags that appear in the description
		if (description == null || description.isEmpty()) {
			return description;
		}
		// TODO: Show effects on card behaviour like increased spell damage
		String descriptionCleaned = description.replaceAll("(</?[bi]>)|\\[x\\]", "");
		// Include taunt if it doesn't seem to contain anything about taunt.
		if (hasAttribute(Attribute.CHARGE)
				&& !descriptionCleaned.matches("[Cc]harge")
				&& !descriptionCleaned.matches("[Ss]torm")) {
			descriptionCleaned = "Storm. " + descriptionCleaned;
		}

		if (hasAttribute(Attribute.TAUNT)
				&& !descriptionCleaned.matches("[Tt]aunt")
				&& !descriptionCleaned.matches("[Gt]uard]")) {
			descriptionCleaned = "Guard. " + descriptionCleaned;
		}
		return descriptionCleaned;
	}

	@Override
	public EntityType getEntityType() {
		return EntityType.CARD;
	}

	public CardLocation getCardLocation() {
		return location;
	}

	public int getManaCost(GameContext context, Player player) {
		int actualManaCost = getBaseManaCost();
		if (manaCostModifier != null) {
			actualManaCost -= manaCostModifier.getValue(context, player, null, this);
		}
		return actualManaCost;
	}

	public Rarity getRarity() {
		return rarity;
	}

	public Race getRace() {
		return hasAttribute(Attribute.RACE) ? (Race) getAttribute(Attribute.RACE) : Race.NONE;
	}

	public boolean hasHeroClass(HeroClass heroClass) {
		if (getHeroClasses() != null) {
			for (HeroClass h : getHeroClasses()) {
				if (heroClass.equals(h)) {
					return true;
				}
			}
		} else if (heroClass == getHeroClass()) {
			return true;
		}
		return false;
	}

	public boolean isCollectible() {
		return collectible;
	}

	public boolean matchesFilter(String filter) {
		if (filter == null || filter.isEmpty()) {
			return true;
		}
		String[] filters = filter.split(" ");
		for (String splitString : filters) {
			if (!matchesSplitFilter(splitString)) {
				return false;
			}
		}
		return true;
	}

	public boolean matchesSplitFilter(String filter) {
		filter = filter.toLowerCase();
		String[] split = filter.split("((<|>)=?)|(!?=)");
		if (split.length >= 2) {
			int value;
			try {
				value = Integer.parseInt(split[1]);
			} catch (Exception e) {
				return false;
			}
			String operator = filter.substring(split[0].length(), filter.indexOf(split[1], split[0].length() + 1));
			if ((split[0].contains("mana") || split[0].contains("cost")) &&
					evaluateExpression(operator, getBaseManaCost(), value)) {
				return true;
			}
			if (split[0].contains("attack") && hasAttribute(Attribute.BASE_ATTACK) &&
					evaluateExpression(operator, getAttributeValue(Attribute.BASE_ATTACK), value)) {
				return true;
			}
			if ((split[0].contains("health") || split[0].contains("hp")) && hasAttribute(Attribute.BASE_HP) &&
					evaluateExpression(operator, getAttributeValue(Attribute.BASE_HP), value)) {
				return true;
			}
		}
		if (getRarity().toString().toLowerCase().contains(filter)) {
			return true;
		}
		if (getRace() != Race.NONE && getRace().toString().toLowerCase().contains(filter)) {
			return true;
		}
		String cardType = getCardType() == CardType.CHOOSE_ONE ? "SPELL" : getCardType().toString();
		if (cardType.toLowerCase().contains(filter)) {
			return true;
		}
		if ((getHeroClass() == HeroClass.ANY && "neutral".contains(filter))
				|| (getHeroClass() != HeroClass.ANY && (getHeroClass().toString().toLowerCase().contains(filter)
				|| "class".contains(filter)))) {
			return true;
		}
		String lowerCaseName = getName().toLowerCase();
		if (lowerCaseName.contains(filter)) {
			return true;
		}
		String regexName = lowerCaseName.replaceAll("[:,\'\\- ]+", "");
		if (regexName.contains(filter)) {
			return true;
		}

		if (getDescription() != null) {
			String lowerCaseDescription = getDescription().toLowerCase();
			if (lowerCaseDescription.contains(filter)) {
				return true;
			}
		}

		return false;
	}

	@Suspendable
	public abstract PlayCardAction play();

	public void setCollectible(boolean collectible) {
		this.collectible = collectible;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setLocation(CardLocation location) {
		this.location = location;
	}

	@Override
	public String toString() {
		return String.format("[%s '%s' %s Manacost:%d]", getCardType(), getName(), getReference(), getBaseManaCost());
	}

	public CardDesc getDesc() {
		return desc;
	}
}
