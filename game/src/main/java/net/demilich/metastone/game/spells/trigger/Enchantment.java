package net.demilich.metastone.game.spells.trigger;

import co.paralleluniverse.fibers.Suspendable;
import com.google.common.collect.Iterables;
import com.hiddenswitch.spellsource.client.models.EntityType;
import com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.events.HasValue;
import net.demilich.metastone.game.spells.AddEnchantmentSpell;
import net.demilich.metastone.game.spells.SpellUtils;
import net.demilich.metastone.game.spells.aura.DoubleTurnEndTriggersAura;
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
 * <li>{@code onAdd}: Called when the enchantment comes into play. At this moment, the number of times the
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
	protected List<EventTrigger> expirationTriggers = new ArrayList<>();
	protected List<EventTrigger> activationTriggers = new ArrayList<>();
	protected SpellDesc spell;
	protected EntityReference hostReference;
	protected boolean oneTurn;
	protected boolean activated = true;
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
			this.getTriggers().add(primaryTrigger);
		}
		if (secondaryTrigger != null) {
			this.getTriggers().add(secondaryTrigger);
		}
		this.spell = spell;
		this.oneTurn = oneTurn;
	}

	public Enchantment(List<EventTrigger> triggers, SpellDesc spell) {
		usesSpellTrigger = true;
		this.getTriggers().addAll(triggers);
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
		clone.setTriggers(new ArrayList<>());
		clone.setExpirationTriggers(new ArrayList<>());
		clone.setActivationTriggers(new ArrayList<>());
		for (var trigger : this.getTriggers()) {
			clone.getTriggers().add(trigger.clone());
		}
		for (var trigger : this.getExpirationTriggers()) {
			clone.getExpirationTriggers().add(trigger.clone());
		}
		for (var trigger : this.getActivationTriggers()) {
			clone.getActivationTriggers().add(trigger.clone());
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
		if (getTriggers().size() == 0) {
			return super.getOwner();
		}
		return getTriggers().get(0).getOwner();
	}

	public SpellDesc getSpell() {
		return spell;
	}

	@Override
	public boolean interestedIn(EventTypeEnum eventType) {
		if (!usesSpellTrigger) {
			return false;
		}
		for (EventTrigger trigger : Iterables.concat(getTriggers(), getExpirationTriggers(), getActivationTriggers())) {
			if (trigger.interestedIn() == eventType || trigger.interestedIn() == EventTypeEnum.ALL) {
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
	 * Casts the effects of this enchantment.
	 *
	 * @param ownerId
	 * @param spell
	 * @param event
	 * @return {@code true} if processing succeeded.
	 */
	@Suspendable
	protected boolean process(int ownerId, SpellDesc spell, GameEvent event) {
		if (!usesSpellTrigger) {
			return false;
		}
		// Maybe have to add a thing to prevent the maxfires from counting twice because of TURN_END_TRIGGERS_TWICE
		if (countByValue && event instanceof HasValue) {
			var value = ((HasValue) event).getValue();
			fires += value;
			firesThisSequence += value;
		} else {
			fires++;
			firesThisSequence++;
		}

		var spellCasts = true;

		// Prevents infinite looping
		if (maxFiresPerSequence != null && firesThisSequence > maxFiresPerSequence) {
			spellCasts = false;
		}

		if (countUntilCast != null && fires < countUntilCast) {
			spellCasts = false;
		}

		if (spellCasts) {
			cast(ownerId, spell, event);
		}

		if (maxFires != null
				&& fires >= maxFires) {
			expire();
		}
		return spellCasts;
	}

	@Suspendable
	protected void cast(int ownerId, SpellDesc spell, GameEvent event) {
		if (event.getEventType().equals(EventTypeEnum.TURN_END) && SpellUtils.getAuras(event.getGameContext(), ownerId, DoubleTurnEndTriggersAura.class).size() > 0) {
			event.getGameContext().getLogic().castSpell(ownerId, spell, hostReference, EntityReference.NONE, TargetSelection.NONE, false, null);
		}
		event.getGameContext().getLogic().castSpell(ownerId, spell, hostReference, EntityReference.NONE, TargetSelection.NONE, false, null);
	}

	@Override
	@Suspendable
	public void onGameEvent(GameEvent event) {
		if (!isActivated() || isExpired() || !usesSpellTrigger) {
			return;
		}

		int ownerId = getOwner();

		// Expire the trigger beforehand, in case of copying minion (Echoing
		// Ooze). Since this method should only be called
		// after being checked to be played, copying one-turn triggers should no
		// longer matter.
		// But let's check to make sure we don't accidentally expire something
		// that's still using it.
		if (oneTurn
				&& (event.getEventType() == EventTypeEnum.TURN_END || event.getEventType() == EventTypeEnum.TURN_START)) {
			expire();
		}

		// Notify the game context that a spell trigger was successfully fired, as long as it wasn't due to a
		// board changed event.
		if (event.getEventType() != EventTypeEnum.BOARD_CHANGED
				&& event.getEventType() != EventTypeEnum.WILL_END_SEQUENCE
				&& getTriggers().stream().noneMatch(trigger -> trigger.interestedIn() == EventTypeEnum.ALL)
				&& hostReference != null
				&& !(hostReference.equals(new EntityReference(PLAYER_1))
				|| hostReference.equals(new EntityReference(PLAYER_2)))) {
			event.getGameContext().onEnchantmentFired(this);
		}
		process(ownerId, getSpell(), event);
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
		for (EventTrigger trigger : Iterables.concat(getTriggers(), getExpirationTriggers(), getActivationTriggers())) {
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
				.append("sourceCard", getSourceCard())
				.toString();
	}

	@Override
	public final boolean queues(GameEvent event) {
		Entity host = event.getGameContext().resolveSingleTarget(hostReference, false);

		// If the host of the enchantment has been removed from play, this enchantment should not queue
		if (host.getZone() == Zones.REMOVED_FROM_PLAY) {
			return false;
		}

		// Expired triggers do not queue
		if (isExpired()) {
			return false;
		}

		if (!isActivated()) {
			for (EventTrigger trigger : getActivationTriggers()) {
				if (trigger == null) {
					continue;
				}
				if (trigger.interestedIn() != event.getEventType() && trigger.interestedIn() != EventTypeEnum.ALL) {
					continue;
				}
				if (trigger.queues(event, host)) {
					activate();
					break;
				}
			}
		}

		if (!isActivated()) {
			return false;
		}

		// Check for expiration next
		// Must have activated in order to expire
		if (!isExpired() && !getExpirationTriggers().isEmpty()) {
			for (EventTrigger trigger : getExpirationTriggers()) {
				if (trigger == null) {
					continue;
				}
				if (trigger.interestedIn() != event.getEventType() && trigger.interestedIn() != EventTypeEnum.ALL) {
					continue;
				}
				if (trigger.queues(event, host)) {
					expire();
					return false;
				}
			}
		}

		for (EventTrigger trigger : getTriggers()) {
			if (trigger == null) {
				continue;
			}
			if (trigger.interestedIn() != event.getEventType() && trigger.interestedIn() != EventTypeEnum.ALL) {
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
		if (!isActivated() || isExpired()) {
			return false;
		}

		for (EventTrigger trigger : getTriggers()) {
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
	 * Get the triggers that fire this enchantment.
	 *
	 * @return A list of triggers.
	 */
	public List<EventTrigger> getTriggers() {
		return triggers;
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

	public Enchantment setTriggers(List<EventTrigger> triggers) {
		this.triggers = triggers;
		return this;
	}

	public void setExpirationTriggers(List<EventTrigger> expirationTriggers) {
		this.expirationTriggers = expirationTriggers;
	}

	public List<EventTrigger> getExpirationTriggers() {
		return expirationTriggers;
	}

	@Override
	public boolean isActivated() {
		return activated;
	}

	public void activate() {
		activated = true;
	}

	public List<EventTrigger> getActivationTriggers() {
		return activationTriggers;
	}

	public void setActivationTriggers(List<EventTrigger> activationTriggers) {
		this.activationTriggers = activationTriggers;
	}

	public Enchantment setActivated(boolean activated) {
		this.activated = activated;
		return this;
	}
}
