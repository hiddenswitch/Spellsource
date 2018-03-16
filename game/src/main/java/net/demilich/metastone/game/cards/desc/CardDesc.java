package net.demilich.metastone.game.cards.desc;

import java.io.Serializable;

import net.demilich.metastone.game.actions.BattlecryAction;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardSet;
import net.demilich.metastone.game.cards.CardType;
import net.demilich.metastone.game.cards.Rarity;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.minions.Race;
import net.demilich.metastone.game.spells.desc.BattlecryDesc;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.aura.AuraDesc;
import net.demilich.metastone.game.spells.desc.condition.ConditionDesc;
import net.demilich.metastone.game.spells.desc.manamodifier.CardCostModifierDesc;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;
import net.demilich.metastone.game.spells.desc.trigger.TriggerDesc;
import net.demilich.metastone.game.spells.desc.valueprovider.ValueProviderDesc;
import net.demilich.metastone.game.targeting.TargetSelection;
import net.demilich.metastone.game.utils.Attribute;
import net.demilich.metastone.game.utils.AttributeMap;

public class CardDesc implements Serializable, Cloneable {
	public String id;
	public String name;
	public String description;
	public Boolean legacy;
	public CardType type;
	public HeroClass heroClass;
	public HeroClass[] heroClasses;
	public Rarity rarity;
	public CardSet set;
	public int baseManaCost;
	public boolean collectible = true;
	public AttributeMap attributes;
	public int fileFormatVersion = 1;
	public ValueProviderDesc manaCostModifier;
	public TriggerDesc passiveTrigger;
	public TriggerDesc[] passiveTriggers;
	public TriggerDesc deckTrigger;
	public TriggerDesc[] gameTriggers;
	public String author;
	/**
	 * Specifies the actor's battlecry.
	 * <p>
	 * Battlecries are always executed whenever the {@link net.demilich.metastone.game.cards.ActorCard} is played from
	 * the hand.
	 * <p>
	 * In order to be counted as a "Battlecry" minion, the card's {@link CardDesc#attributes} must contain a {@link
	 * Attribute#BATTLECRY} key with {@code true}.
	 *
	 * @see BattlecryDesc for more about battlecries.
	 */
	public BattlecryDesc battlecry;
	/**
	 * Specifies the actor's deathrattle.
	 *
	 * @see SpellDesc for more about deathrattles.
	 */
	public SpellDesc deathrattle;
	/**
	 * Specifies the actor's trigger that become active when the actor goes into an in-play zone ({@link
	 * net.demilich.metastone.game.targeting.Zones#BATTLEFIELD}, {@link net.demilich.metastone.game.targeting.Zones#WEAPON},
	 * or {@link net.demilich.metastone.game.targeting.Zones#HERO}).
	 */
	public TriggerDesc trigger;
	/**
	 * Multiple {@link #trigger} objects that should come into play whenever the actor comes into an in-play zone.
	 */
	public TriggerDesc[] triggers;
	/**
	 * The aura that is active whenever the actor is in play.
	 */
	public AuraDesc aura;
	/**
	 * The aura that is active whenever the actor is in play.
	 */
	public AuraDesc[] auras;
	/**
	 * The actor's race, or "tribe."
	 */
	public Race race;
	/**
	 * A card cost modifier that is active whenever the actor is in play.
	 */
	public CardCostModifierDesc cardCostModifier;
	/**
	 * The base attack of the minion. This will be the {@link Actor#getBaseAttack()} value.
	 */
	public int baseAttack;
	/**
	 * The base HP of the minion. This will be the {@link Actor#getBaseHp()} value.
	 */
	public int baseHp;
	public BattlecryDesc[] chooseOneBattlecries;
	public BattlecryDesc chooseBothBattlecry;
	public String[] chooseOneCardIds;
	public String chooseBothCardId;
	public int damage;
	public int durability;
	public SpellDesc onEquip;
	public SpellDesc onUnequip;
	public String heroPower;
	public TargetSelection targetSelection;
	public SpellDesc spell;
	public ConditionDesc condition;
	public SpellDesc[] group;
	public EventTriggerDesc secret;
	public EventTriggerDesc quest;
	public int countUntilCast;

	public Card create() {
		return new Card(this);
	}

	public boolean getCollectible() {
		return collectible;
	}

	public BattlecryAction getBattlecryAction() {
		if (battlecry == null) {
			return null;
		}
		BattlecryAction battlecryAction = BattlecryAction.createBattlecry(battlecry.spell, battlecry.getTargetSelection());
		if (battlecry.condition != null) {
			battlecryAction.setCondition(battlecry.condition.create());
		}
		return battlecryAction;
	}
}
