package net.demilich.metastone.game.spells.trigger;

import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.spellsource.client.models.EntityType;
import com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.EntityLocation;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.events.HasValue;
import net.demilich.metastone.game.logic.GameLogic;
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
import net.demilich.metastone.game.targeting.IdFactory;
import net.demilich.metastone.game.targeting.TargetSelection;
import net.demilich.metastone.game.targeting.Zones;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static net.demilich.metastone.game.GameContext.PLAYER_1;
import static net.demilich.metastone.game.GameContext.PLAYER_2;

/**
 * An enchantment is a type of entity that reacts to certain events using a trigger by casting a spell.
 * <p>
 * Enchantments live inside {@link GameLogic#getActiveTriggers(EntityReference)}; some enchantments, like {@link Secret}
 * and {@link Quest}, live in their respective zones {@link Zones#QUEST} and {@link Zones#SECRET}.
 * <p>
 * Enchantments consistent of two important features: a {@link EventTrigger} built from an {@link EventTriggerDesc}, and
 * a {@link SpellDesc} indicating which spell should be cast when the enchantment's {@link EventTrigger} and its
 * conditions {@link EventTrigger#innerQueues(GameEvent, Enchantment, Entity)}.
 * <p>
 * Enchantments are specified by a {@link EnchantmentDesc} in the card JSON. They can also be specified as fields in
 * e.g., {@link AddEnchantmentSpell}.
 * <p>
 * The lifecycle of an enchantment looks like the following:
 * <ul>
 * <li>{@code onAdd}: Called when the enchantment comes into play. At this moment, the number of times the
 * enchantment has been fired is set to zero; it is not {@code expired}, and </li>
 * <li>{@code onGameEvent}: Gives the enchantment a chance to look at a {@link GameEvent}, evaluate a {@link
 * EventTrigger} against it, and determine whether its {@link EnchantmentDesc#getSpell()} should be cast.</li>
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
	private static final EventTriggerDesc[] DEFAULT_TRIGGERS = new EventTriggerDesc[0];
	private static final Zones[] DEFAULT_BATTLEFIELD_ZONES = new Zones[]{Zones.BATTLEFIELD, Zones.HERO, Zones.HERO_POWER, Zones.WEAPON, Zones.PLAYER, Zones.QUEST, Zones.SECRET};
	private static final Zones[] DEFAULT_PASSIVE_ZONES = new Zones[]{Zones.HERO_POWER, Zones.HAND};
	private static final Zones[] DEFAULT_GAME_ZONES = new Zones[]{Zones.DISCOVER, Zones.SET_ASIDE_ZONE, Zones.DECK, Zones.HAND};
	private static final Zones[] DEFAULT_DECK_ZONES = new Zones[]{Zones.DECK};
	private static final Set<Zones> DEFAULT_BATTLEFIELD_ZONES_SET = Arrays.stream(Enchantment.getDefaultBattlefieldZones()).collect(Collectors.toSet());
	protected List<EventTrigger> triggers = new ArrayList<>();
	protected List<EventTrigger> expirationTriggers = new ArrayList<>();
	protected List<EventTrigger> activationTriggers = new ArrayList<>();
	protected SpellDesc spell;
	protected EntityReference hostReference;
	protected boolean oneTurn;
	protected boolean activated = true;
	protected boolean expired;
	protected boolean persistentOwner;
	private Integer maxFires;
	private int fires;
	protected boolean keepAfterTransform;
	protected Integer countUntilCast;
	protected boolean countByValue;
	protected boolean usesSpellTrigger = true;
	protected Integer maxFiresPerSequence;
	protected int firesThisSequence;
	protected boolean copyToActor;
	protected Zones[] zones;

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

	protected EventTriggerDesc[] getDefaultTriggers() {
		return DEFAULT_TRIGGERS;
	}

	@Override
	public EntityType getEntityType() {
		return EntityType.ENCHANTMENT;
	}

	@Suspendable
	public void expire(GameContext context) {
		expired = true;
	}

	@Override
	public EntityReference getHostReference() {
		return hostReference;
	}

	public SpellDesc getSpell() {
		return spell;
	}

	@Override
	public boolean interestedIn(EventTypeEnum eventType) {
		if (!usesSpellTrigger) {
			return false;
		}
		for (EventTrigger trigger : getTriggers()) {
			if (trigger.interestedIn() == eventType || trigger.interestedIn() == EventTypeEnum.ALL) {
				return true;
			}
		}
		for (EventTrigger trigger : getExpirationTriggers()) {
			if (trigger.interestedIn() == eventType || trigger.interestedIn() == EventTypeEnum.ALL) {
				return true;
			}
		}
		for (EventTrigger trigger : getActivationTriggers()) {
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
	public void onAdd(GameContext context, Player player, Entity source, Entity host) {
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
			setFires(getFires() + value);
			firesThisSequence += value;
		} else {
			setFires(getFires() + 1);
			firesThisSequence++;
		}

		var spellCasts = true;

		// Prevents infinite looping
		if (maxFiresPerSequence != null && firesThisSequence > maxFiresPerSequence) {
			spellCasts = false;
		}

		if (countUntilCast != null && getFires() < countUntilCast) {
			spellCasts = false;
		}

		if (spellCasts) {
			cast(ownerId, spell, event);
		}

		if (getMaxFires() != null
				&& getFires() >= getMaxFires()) {
			expire(event.getGameContext());
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
			expire(event.getGameContext());
		}

		// Notify the game context that a spell trigger was successfully fired, as long as it wasn't due to a
		// board changed event.
		if (shouldNotifyEnchantmentFired(event)) {
			event.getGameContext().onEnchantmentFired(this);
		}
		process(ownerId, getSpell(), event);
	}

	@Suspendable
	protected boolean shouldNotifyEnchantmentFired(GameEvent event) {
		return event.getEventType() != EventTypeEnum.BOARD_CHANGED
				&& event.getEventType() != EventTypeEnum.WILL_END_SEQUENCE
				&& getTriggers().stream().noneMatch(trigger -> trigger.interestedIn() == EventTypeEnum.ALL)
				&& hostReference != null
				&& !(hostReference.equals(new EntityReference(PLAYER_1))
				|| hostReference.equals(new EntityReference(PLAYER_2)));
	}

	@Override
	public void setHostReference(EntityReference reference) {
		hostReference = reference;
	}

	@Override
	public Enchantment getCopy() {
		var clone = this.clone();
		clone.setId(IdFactory.UNASSIGNED);
		clone.setEntityLocation(EntityLocation.UNASSIGNED);
		clone.hostReference = EntityReference.NONE;
		clone.expired = false;
		clone.fires = 0;
		if (!isPersistentOwner()) {
			clone.setOwner(NO_OWNER);
		}
		return clone;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
				.append("host", getHostReference())
				.append("id", getId())
				.append("sourceCard", getSourceCard())
				.toString();
	}

	@Override
	@Suspendable
	public final boolean queues(GameEvent event) {
		Entity host = event.getGameContext().resolveSingleTarget(hostReference, false);

		// If the host of the enchantment has been removed from play, this enchantment should not queue
		if (host.getZone() == Zones.REMOVED_FROM_PLAY) {
			return false;
		}

		if (!innerQueues(event, host)) {
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
				if (trigger.queues(event, this, host, getOwner())) {
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
				if (trigger.queues(event, this, host, getOwner())) {
					expire(event.getGameContext());
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
			if (trigger.queues(event, this, host, getOwner())) {
				return true;
			}
		}

		return false;
	}

	@Suspendable
	protected boolean innerQueues(GameEvent event, Entity host) {
		return Arrays.stream(getZones()).anyMatch(z -> z == host.getZone());
	}

	public boolean isPersistentOwner() {
		return persistentOwner;
	}

	public void setPersistentOwner(boolean persistentOwner) {
		this.persistentOwner = persistentOwner;
	}

	public boolean oneTurnOnly() {
		return oneTurn;
	}

	public boolean fires(GameEvent event) {
		var host = event.getGameContext().resolveSingleTarget(hostReference, false);
		// Expired
		if (!isActivated() || isExpired()) {
			return false;
		}

		for (EventTrigger trigger : getTriggers()) {
			if (trigger.fires(event, host, getOwner())) {
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

	public void setCountUntilCast(Integer countUntilCast) {
		this.countUntilCast = countUntilCast;
	}

	public Integer getCountUntilCast() {
		return countUntilCast;
	}

	public Enchantment setSpell(SpellDesc spell) {
		this.spell = spell;
		return this;
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

	public Zones[] getZones() {
		return (zones == null || zones.length == 0) ? getDefaultZones() : zones;
	}

	public Enchantment setZones(Zones[] zones) {
		this.zones = zones;
		return this;
	}

	protected Zones[] getDefaultZones() {
		return DEFAULT_BATTLEFIELD_ZONES;
	}

	public Enchantment setOneTurn(boolean oneTurn) {
		this.oneTurn = oneTurn;
		return this;
	}

	public boolean getOneTurn() {
		return oneTurn;
	}

	public void setUsesSpellTrigger(boolean usesSpellTrigger) {
		this.usesSpellTrigger = usesSpellTrigger;
	}

	public boolean getUsesSpellTrigger() {
		return usesSpellTrigger;
	}

	protected Enchantment setFires(int fires) {
		this.fires = fires;
		return this;
	}


	public static Zones[] getDefaultBattlefieldZones() {
		return DEFAULT_BATTLEFIELD_ZONES;
	}

	public static Zones[] getDefaultPassiveZones() {
		return DEFAULT_PASSIVE_ZONES;
	}

	public static Zones[] getDefaultGameZones() {
		return DEFAULT_GAME_ZONES;
	}

	public static Zones[] getDefaultDeckZones() {
		return DEFAULT_DECK_ZONES;
	}

	public static Set<Zones> getDefaultBattlefieldZonesSet() {
		return DEFAULT_BATTLEFIELD_ZONES_SET;
	}

	public boolean isCopyToActor() {
		return copyToActor;
	}

	public Enchantment setCopyToActor(boolean giveToActor) {
		this.copyToActor = giveToActor;
		return this;
	}
}