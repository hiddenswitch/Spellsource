package net.demilich.metastone.game.cards.desc;

import net.demilich.metastone.game.actions.BattlecryAction;
import net.demilich.metastone.game.entities.heroes.Hero;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.entities.minions.Race;
import net.demilich.metastone.game.entities.weapons.Weapon;
import net.demilich.metastone.game.spells.desc.BattlecryDesc;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.aura.AuraDesc;
import net.demilich.metastone.game.spells.desc.manamodifier.CardCostModifierDesc;
import net.demilich.metastone.game.spells.desc.trigger.TriggerDesc;
import net.demilich.metastone.game.utils.Attribute;

/**
 * Fields common to all actors, like {@link Minion}, {@link Weapon} and {@link Hero}.
 * <p>
 * Not all must be specified to make a valid actor.
 */
public abstract class ActorCardDesc extends CardDesc {
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
	 * The actor's race, or "tribe."
	 */
	public Race race;
	/**
	 * A card cost modifier that is active whenever the actor is in play.
	 */
	public CardCostModifierDesc cardCostModifier;

	public BattlecryAction getBattlecryAction() {
		if (battlecry == null) {
			return null;
		}
		BattlecryAction battlecryAction = BattlecryAction.createBattlecry(battlecry.spell, battlecry.getTargetSelection());
		if (battlecry.condition != null) {
			battlecryAction.setCondition(battlecry.condition.createInstance());
		}
		return battlecryAction;
	}
}
