package net.demilich.metastone.game.spells.trigger;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import com.hiddenswitch.spellsource.client.models.EntityType;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.events.HasValue;
import net.demilich.metastone.game.spells.AddEnchantmentSpell;
import net.demilich.metastone.game.spells.SpellUtils;
import net.demilich.metastone.game.spells.aura.DoubleTurnEndTriggersAura;
import net.demilich.metastone.game.spells.aura.SecretsTriggerTwiceAura;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.trigger.EnchantmentDesc;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;
import net.demilich.metastone.game.spells.trigger.secrets.Quest;
import net.demilich.metastone.game.spells.trigger.secrets.Secret;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.targeting.TargetSelection;
import net.demilich.metastone.game.targeting.Zones;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static net.demilich.metastone.game.GameContext.PLAYER_1;
import static net.demilich.metastone.game.GameContext.PLAYER_2;

/**
 * An enchantment is a type of entity that reacts to certain events using a trigger by casting a spell.
 * <p>
 * Enchantments live inside {@link TriggerManager#getTriggers()}; some enchantments, like {@link Secret} and {@link
 * Quest}, live in their respective zones {@link Zones#QUEST} and {@link Zones#SECRET}. Otherwise, unlike in
 * Hearthstone, enchantments are generally not targetable and do not "live" on cards or in zones.
 * <p>
 * Enchantments consistent of two important features: a {@link EventTrigger} built from an {@link EventTriggerDesc}, and
 * a {@link SpellDesc} indicating which spell should be cast when the enchantment's {@link EventTrigger} and its
 * conditions {@link EventTrigger#innerQueues(GameEvent, Entity)}.
 * <p>
 * Enchantments are specified by a {@link EnchantmentDesc} in the card JSON. They can also be specified as fields in
 * e.g., {@link AddEnchantmentSpell}.
 * <p>
 * The lifecycle of an enchantment looks like the following:
 * <ul>
 * <li>{@code onAdd}: Called when the encantment comes into play. At this moment, the number of times the
 * enchantment has been fired is set to zero; it is not {@code expired}, and </li>
 * <li>{@code onGameEvent}: Gives the enchantment a chance to look at a {@link GameEvent}, evaluate a {@link
 * EventTrigger} against it, and determine whether its {@link EnchantmentDesc#spell} should be cast.</li>
 * <li>{@code onRemove}: Expires the enchantment, ensuring it will never fire again.</li>
 * </ul>
 * An enchantment's lifecycle matches the entity it is hosted by, like an {@link Actor}, the {@link Player} entity (when
 * e.g. {@link AddEnchantmentSpell} has a {@link SpellArg#TARGET} of {@link EntityReference#FRIENDLY_PLAYER}) or itself
 * (in the case of a quest and secret). This means that {@link EntityReference#FRIENDLY_PLAYER} is a natural host for
 * {@link Enchantment} objects that should not be removed for any reason like minion death.
 *
 * @see EnchantmentDesc for a description of the format of an enchantment.
 */
public class Enchantment extends Entity implements Trigger {
	protected List<EventTrigger> triggers = new ArrayList<>();
	protected SpellDesc spell;
	protected EntityReference hostReference;
	protected boolean oneTurn;
	protected boolean expired;
	protected boolean persistentOwner;
	protected Integer maxFires;
	protected int fires;
	protected boolean keepAfterTransform;
	protected Card sourceCard;
	protected Integer countUntilCast;
	protected boolean countByValue;
	protected boolean usesSpellTrigger = true;
	protected Integer maxFiresPerSequence;
	protected int firesThisSequence;

	public Enchantment(EventTrigger primaryTrigger, EventTrigger secondaryTrigger, SpellDesc spell, boolean oneTurn) {
		usesSpellTrigger = true;
		if (primaryTrigger != null) {
			this.triggers.add(primaryTrigger);
		}
		if (secondaryTrigger != null) {
			this.triggers.add(secondaryTrigger);
		}
		this.spell = spell;
		this.oneTurn = oneTurn;
	}

	public Enchantment(List<EventTrigger> triggers, SpellDesc spell) {
		usesSpellTrigger = true;
		this.triggers.addAll(triggers);
		this.spell = spell;
	}

	public Enchantment(EventTrigger trigger, SpellDesc spell) {
		this(trigger, spell, false);
	}

	public Enchantment(EventTrigger trigger, SpellDesc spell, boolean oneTime) {
		this(trigger, null, spell, oneTime);
	}

	public Enchantment() {
	}

	@Override
	public Enchantment clone() {
		Enchantment clone = (Enchantment) super.clone();
		clone.triggers = new ArrayList<>();
		for (EventTrigger trigger : this.triggers) {
			clone.triggers.add(trigger.clone());
		}
		if (getSpell() != null) {
			clone.spell = getSpell().clone();
		}
		return clone;
	}

	@Override
	public EntityType getEntityType() {
		return EntityType.ENCHANTMENT;
	}

	public void expire() {
		expired = true;
	}

	@Override
	public EntityReference getHostReference() {
		return hostReference;
	}

	@Override
	public int getOwner() {
		if (triggers.size() == 0) {
			return -1;
		}
		return triggers.get(0).getOwner();
	}

	public SpellDesc getSpell() {
		return spell;
	}

	@Override
	public boolean interestedIn(com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum eventType) {
		if (!usesSpellTrigger) {
			return false;
		}
		for (EventTrigger trigger : triggers) {
			if (trigger.interestedIn() == eventType || trigger.interestedIn() == com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum.ALL) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isExpired() {
		return expired;
	}

	@Override
	@Suspendable
	public void onAdd(GameContext context) {
	}

	/**
	 * Processes a firing.
	 *
	 * @param ownerId
	 * @param spell
	 * @param event
	 * @return
	 */
	@Suspendable
	protected boolean onFire(int ownerId, SpellDesc spell, GameEvent event) {
		if (!usesSpellTrigger) {
			return false;
		}
		// Maybe have to add a thing to prevent the maxfires from counting twice because of TURN_END_TRIGGERS_TWICE
		if (countByValue && event instanceof HasValue) {
			int value = ((HasValue) event).getValue();
			fires += value;
			firesThisSequence += value;
		} else {
			fires++;
			firesThisSequence++;
		}

		boolean spellCasts = true;

		// Prevents infinite looping
		if (maxFiresPerSequence != null && firesThisSequence > maxFiresPerSequence) {
			spellCasts = false;
		}

		// Max fires can expire the enchantment, while max fires per sequence does not.
		if (maxFires != null
				&& fires > maxFires) {
			spellCasts = false;
			expire();
		}

		if (countUntilCast != null && fires < countUntilCast) {
			spellCasts = false;
		}
		if (spellCasts) {
			if (this instanceof Quest) {
				expire();
			}
			if (this instanceof Secret && SpellUtils.hasAura(event.getGameContext(), ownerId, SecretsTriggerTwiceAura.class)) {
				event.getGameContext().getLogic().castSpell(ownerId, spell, hostReference, EntityReference.NONE, TargetSelection.NONE, false, null);
			}
			if (event.getEventType().equals(com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum.TURN_END) && SpellUtils.getAuras(event.getGameContext(), ownerId, DoubleTurnEndTriggersAura.class).size() > 0) {
				event.getGameContext().getLogic().castSpell(ownerId, spell, hostReference, EntityReference.NONE, TargetSelection.NONE, false, null);
			}
			event.getGameContext().getLogic().castSpell(ownerId, spell, hostReference, EntityReference.NONE, TargetSelection.NONE, false, null);
		}
		if (maxFires != null
				&& fires >= maxFires) {
			expire();
		}
		return spellCasts;
	}

	@Override
	@Suspendable
	public void onGameEvent(GameEvent event) {
		if (expired || !usesSpellTrigger) {
			return;
		}

		int ownerId = getOwner();

		// Expire the trigger beforehand, in case of copying minion (Echoing
		// Ooze). Since this method should only be called
		// after being checked to be played, copying one-turn triggers should no
		// longer matter.
		// But let's check to make sure we don't accidentally expire something
		// that's still using it.
		if (oneTurn && (event.getEventType() == com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum.TURN_END || event.getEventType() == com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum.TURN_START)) {
			expire();
		}

		// Notify the game context that a spell trigger was successfully fired, as long as it wasn't due to a
		// board changed event.
		if (event.getEventType() != com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum.BOARD_CHANGED
				&& event.getEventType() != com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum.WILL_END_SEQUENCE
				&& triggers.stream().noneMatch(trigger -> trigger.interestedIn() == com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum.ALL)
				&& hostReference != null
				&& !(hostReference.equals(new EntityReference(PLAYER_1))
				|| hostReference.equals(new EntityReference(PLAYER_2)))) {
			event.getGameContext().onEnchantmentFired(this);
		}
		onFire(ownerId, getSpell(), event);
	}

	@Override
	@Suspendable
	public void onRemove(GameContext context) {
		expire();
	}

	@Override
	public void setHost(Entity host) {
		this.hostReference = host.getReference();
	}

	@Override
	public void setOwner(int playerIndex) {
		super.setOwner(playerIndex);
		for (EventTrigger trigger : triggers) {
			trigger.setOwner(playerIndex);
		}
	}

	@Override
	public Enchantment getCopy() {
		return this.clone();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.appendSuper(super.toString())
				.append("id", getId())
				.append("sourceCard", getSourceCard() == null ? null : getSourceCard().getCardId())
				.toString();
	}

	@Override
	public final boolean queues(GameEvent event) {
		Entity host = event.getGameContext().resolveSingleTarget(hostReference);
		for (EventTrigger trigger : triggers) {
			if (trigger == null) {
				continue;
			}
			if (trigger.interestedIn() != event.getEventType() && trigger.interestedIn() != com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum.ALL) {
				continue;
			}
			if (trigger.queues(event, host)) {
				return true;
			}
		}
		return false;
	}


	public boolean hasPersistentOwner() {
		return persistentOwner;
	}

	public void setPersistentOwner(boolean persistentOwner) {
		this.persistentOwner = persistentOwner;
	}

	public boolean oneTurnOnly() {
		return oneTurn;
	}

	public boolean fires(GameEvent event) {
		// Expired
		if (isExpired()) {
			return false;
		}

		for (EventTrigger trigger : triggers) {
			if (trigger.fires(event)) {
				return true;
			}
		}
		return false;
	}

	public Card getSourceCard() {
		return sourceCard;
	}

	public int getFires() {
		return fires;
	}

	public Integer getMaxFires() {
		return maxFires;
	}

	public void setMaxFires(Integer maxFires) {
		this.maxFires = maxFires;
	}

	public void setKeepAfterTransform(boolean keepAfterTransform) {
		this.keepAfterTransform = keepAfterTransform;
	}

	public boolean isKeptAfterTransform() {
		return keepAfterTransform;
	}

	public void setSourceCard(Card sourceCard) {
		this.sourceCard = sourceCard;
	}

	public void setCountUntilCast(Integer countUntilCast) {
		this.countUntilCast = countUntilCast;
	}

	public Integer getCountUntilCast() {
		return countUntilCast;
	}

	public void setSpell(SpellDesc spell) {
		this.spell = spell;
	}

	public void setCountByValue(boolean countByValue) {
		this.countByValue = countByValue;
	}

	/**
	 * Indicates that the {@link HasValue#getValue()} of the event should be used to increment the enchantment's counter
	 * (typically its {@link #fires}) instead of the value 1 (i.e., every event that causes a trigger to fire increments
	 * the number of fires by 1).
	 *
	 * @return {@code true} if the enchantment increases its fires counter by the event's value.
	 */
	public boolean isCountByValue() {
		return countByValue;
	}

	/**
	 * Get a read-only list of triggers that fire this enchantment.
	 *
	 * @return An unmodifiable list of triggers.
	 */
	public List<EventTrigger> getTriggers() {
		return Collections.unmodifiableList(triggers);
	}

	public void setMaxFiresPerSequence(Integer maxFiresPerSequence) {
		this.maxFiresPerSequence = maxFiresPerSequence;
	}

	public Integer getMaxFiresPerSequence() {
		return maxFiresPerSequence;
	}

	/**
	 * Signals to the enchantment that the currently processing sequence is over.
	 */
	public void endOfSequence() {
		firesThisSequence = 0;
	}
}
