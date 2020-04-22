package net.demilich.metastone.game.spells.desc.trigger;

import co.paralleluniverse.fibers.Suspendable;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.desc.HasEntrySet;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.spells.desc.AbstractEnchantmentDesc;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.trigger.Enchantment;
import net.demilich.metastone.game.spells.trigger.Trigger;
import net.demilich.metastone.game.targeting.Zones;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.collect.Maps.immutableEntry;

/**
 * Data specifying a trigger, including what event it reacts to, what spell it casts, and various options. Each field in
 * this class corresponds directly to a field that appears in the JSON whenever an {@code EnchantmentDesc} is expected.
 * In other words, an EnchantmentDesc looks like:
 * <pre>
 *   {
 *    "eventTrigger": { An object corresponding to an {@link EventTriggerDesc} }
 *    "spell": { An object corresponding to a {@link SpellDesc} }
 *    "oneTurn": A boolean indicating that this Enchantment lasts one turn.
 *    "maxFires": The maximum number of times this enchantment will fire until it expires.
 *    "countUntilCast": The minimum number of times this enchantment must fire until its spell is cast
 *    "persistentOwner": See {@link #persistentOwner}
 *    "keepAfterTransform": See {@link #keepAfterTransform}
 *    "countByValue": See {@link #countByValue}
 *   }
 * </pre>
 * <p>
 * This object is used to create an {@link Enchantment} using its {@link #create()} function whenever entities like
 * actors come into play with a {@link net.demilich.metastone.game.cards.desc.CardDesc#trigger} specified.
 * <p>
 * For <b>example,</b> the following JSON would belong on the {@link net.demilich.metastone.game.cards.desc.CardDesc#trigger}
 * field to describe a minion that draws a card whenever it is damaged, up to 3 times:
 * <pre>
 *     {
 *         "eventTrigger": {
 *             "class": "DamagedReceivedTrigger",
 *             "hostTargetType": "IGNORE_OTHER_TARGETS"
 *         },
 *         "spell": {
 *             "class": "DrawCardSpell",
 *             "targetPlayer": "SELF"
 *         },
 *         "maxFires": 3
 *     }
 * </pre>
 * Note, this is distinct from an {@link EventTriggerDesc} or {@link net.demilich.metastone.game.spells.trigger.EventTrigger},
 * which defines how to react to which events in game.
 *
 * @see Enchantment for more about enchantments.
 * @see net.demilich.metastone.game.cards.desc.CardDesc to see where {@link EnchantmentDesc} can typically go.
 */
@JsonInclude(value = JsonInclude.Include.NON_DEFAULT)
public final class EnchantmentDesc implements Serializable, Cloneable, HasEntrySet<EnchantmentDescArg, Object>, AbstractEnchantmentDesc<Enchantment> {

	public EnchantmentDesc() {
	}

	protected EventTriggerDesc eventTrigger;
	protected SpellDesc spell;
	protected boolean oneTurn;
	protected boolean persistentOwner;
	protected boolean keepAfterTransform;
	protected Integer maxFires;
	protected Integer maxFiresPerSequence;
	protected Integer countUntilCast;
	protected boolean countByValue;
	protected EventTriggerDesc[] activationTriggers;
	protected EventTriggerDesc[] expirationTriggers = new EventTriggerDesc[0];
	protected String name;
	protected String description;
	protected Zones[] zones = Enchantment.getDefaultBattlefieldZones();

	public Set<Map.Entry<EnchantmentDescArg, Object>> entrySet() {
		@SuppressWarnings("unchecked")
		Set<Map.Entry<EnchantmentDescArg, Object>> entries = Set.of(
				immutableEntry(EnchantmentDescArg.EVENT_TRIGGER, getEventTrigger()),
				immutableEntry(EnchantmentDescArg.SPELL, getSpell()),
				immutableEntry(EnchantmentDescArg.ONE_TURN, isOneTurn()),
				immutableEntry(EnchantmentDescArg.PERSISTENT_OWNER, isPersistentOwner()),
				immutableEntry(EnchantmentDescArg.MAX_FIRES, getMaxFires()),
				immutableEntry(EnchantmentDescArg.COUNT_UNTIL_CAST, getCountUntilCast()),
				immutableEntry(EnchantmentDescArg.COUNT_BY_VALUE, isCountByValue()),
				immutableEntry(EnchantmentDescArg.MAX_FIRES_PER_SEQUENCE, getMaxFiresPerSequence()),
				immutableEntry(EnchantmentDescArg.EXPIRATION_TRIGGERS, getExpirationTriggers()),
				immutableEntry(EnchantmentDescArg.ACTIVATION_TRIGGERS, getActivationTriggers()),
				immutableEntry(EnchantmentDescArg.DESCRIPTION, getDescription()),
				immutableEntry(EnchantmentDescArg.NAME, getName()),
				immutableEntry(EnchantmentDescArg.ZONES, getZones())
		);
		return entries;
	}

	/**
	 * Creates an enchantment represented by this configuration.
	 * <p>
	 * The enchantment's {@link Trigger#setHostReference(net.demilich.metastone.game.targeting.EntityReference)} call
	 * should be applied immediately afterwards to specify the host of this enchantment. {@link
	 * net.demilich.metastone.game.spells.trigger.secrets.Quest} and {@link net.demilich.metastone.game.spells.trigger.secrets.Secret}
	 * enchantments exist in play, and by convention they have unspecified hosts.
	 *
	 * @return The enchantment
	 */
	@JsonIgnore
	public Enchantment create() {
		Enchantment enchantment = new Enchantment();
		// may be null if a card is being used as an enchantment
		if (getEventTrigger() != null) {
			enchantment.getTriggers().add(getEventTrigger().create());
		}
		enchantment.setSpell(getSpell());
		enchantment.setOneTurn(isOneTurn());
		enchantment.setMaxFires(getMaxFires());
		enchantment.setPersistentOwner(isPersistentOwner());
		enchantment.setKeepAfterTransform(isKeepAfterTransform());
		enchantment.setCountUntilCast(getCountUntilCast());
		enchantment.setCountByValue(isCountByValue());
		enchantment.setMaxFiresPerSequence(getMaxFiresPerSequence());
		enchantment.setName(getName());
		if (getDescription() != null) {
			enchantment.setDescription(getDescription());
		}
		if (getExpirationTriggers() != null && getExpirationTriggers().length > 0) {
			enchantment.setExpirationTriggers(Arrays.stream(getExpirationTriggers()).map(EventTriggerDesc::create).collect(Collectors.toList()));
		}
		if (getActivationTriggers() != null && getActivationTriggers().length > 0) {
			enchantment.setActivationTriggers(Arrays.stream(getActivationTriggers()).map(EventTriggerDesc::create).collect(Collectors.toList()));
		}
		if (getZones() != null) {
			enchantment.setZones(getZones());
		}
		return enchantment;
	}

	/**
	 * The description of which "event trigger" (reacting to which event) this trigger will react to
	 */
	public EventTriggerDesc getEventTrigger() {
		return eventTrigger;
	}

	public EnchantmentDesc setEventTrigger(EventTriggerDesc eventTrigger) {
		this.eventTrigger = eventTrigger;
		return this;
	}

	/**
	 * The spell that will be cast by an {@link Enchantment#process(int, SpellDesc, GameEvent)} invocation.
	 *
	 * @see Enchantment for more about enchantments.
	 */
	public SpellDesc getSpell() {
		return spell;
	}

	public EnchantmentDesc setSpell(SpellDesc spell) {
		this.spell = spell;
		return this;
	}

	/**
	 * When {@code true}, indicates the enchantment should only last one turn.
	 */
	public boolean isOneTurn() {
		return oneTurn;
	}

	public EnchantmentDesc setOneTurn(boolean oneTurn) {
		this.oneTurn = oneTurn;
		return this;
	}

	/**
	 * When {@code true}, indicates the owner of the enchantment for the purposes of evaluating the {@code player}
	 * argument of a {@link net.demilich.metastone.game.spells.Spell#onCast(GameContext, Player, SpellDesc, Entity,
	 * Entity)} invocation shouldn't change if the owner of the {@link Enchantment#hostReference} changes.
	 * <p>
	 * This implements Blessing of Wisdom, Power Word: Glory and other effects whose text would change depending on whose
	 * perspective the text is read from.
	 */
	public boolean isPersistentOwner() {
		return persistentOwner;
	}

	public EnchantmentDesc setPersistentOwner(boolean persistentOwner) {
		this.persistentOwner = persistentOwner;
		return this;
	}

	/**
	 * When {@code true}, this {@link Enchantment} should not be removed by a {@link
	 * net.demilich.metastone.game.logic.GameLogic#transformMinion(SpellDesc, Entity, Minion, Minion, boolean)} or {@link
	 * net.demilich.metastone.game.logic.GameLogic#replaceCard(int, Card, Card)} effect.
	 * <p>
	 * Implements Shifter Zerus, Molten Blade and other in-hand every-turn-replacement effects.
	 */
	public boolean isKeepAfterTransform() {
		return keepAfterTransform;
	}

	public EnchantmentDesc setKeepAfterTransform(boolean keepAfterTransform) {
		this.keepAfterTransform = keepAfterTransform;
		return this;
	}

	/**
	 * The maximum number of times this trigger can fire until it expires.
	 * <p>
	 * When {@code null} (the default), the trigger can fire an unlimited number of times.
	 */
	public Integer getMaxFires() {
		return maxFires;
	}

	public EnchantmentDesc setMaxFires(Integer maxFires) {
		this.maxFires = maxFires;
		return this;
	}

	/**
	 * The maximum number of times this trigger can fire per sequence. This counter is reset at the beginning of the
	 * sequence. Does <b>not</b> expire the trigger when exceeded.
	 * <p>
	 * When {@code null} (the default), the trigger can fire an unlimited number of times per sequence.
	 */
	public Integer getMaxFiresPerSequence() {
		return maxFiresPerSequence;
	}

	public EnchantmentDesc setMaxFiresPerSequence(Integer maxFiresPerSequence) {
		this.maxFiresPerSequence = maxFiresPerSequence;
		return this;
	}

	/**
	 * The number of times an {@link Enchantment} fires until it actually casts its spell.
	 * <p>
	 * Implements Quests and many other counting behaviours in triggers.
	 * <p>
	 * Typically {@link #maxFires} is set to the same value as {@code countUntilCast} to limit the trigger to casting a
	 * spell at most once, as soon as the {@link #eventTrigger} has fired {@code countUntilCast} times.
	 */
	public Integer getCountUntilCast() {
		return countUntilCast;
	}

	public EnchantmentDesc setCountUntilCast(Integer countUntilCast) {
		this.countUntilCast = countUntilCast;
		return this;
	}

	/**
	 * When {@code true}, treats the {@link GameContext#getEventValue()} as the amount to increment this enchantment's
	 * firing counter.
	 * <p>
	 * Implements spellstones.
	 */
	public boolean isCountByValue() {
		return countByValue;
	}

	public EnchantmentDesc setCountByValue(boolean countByValue) {
		this.countByValue = countByValue;
		return this;
	}

	/**
	 * Triggers that activate this enchantment when they fired.
	 * <p>
	 * If any triggers are specified, the enchantment will come into play with {@link Enchantment#isActivated()} set to
	 * {@code false}.
	 * <p>
	 * Activation occurs during the queueing phase, and thus you should use a {@link EventTriggerArg#QUEUE_CONDITION} if
	 * the trigger should have a condition to determine whether or not it activates. Activations are <b>NOT</b> evaluated
	 * during the firing phase.
	 */
	public EventTriggerDesc[] getActivationTriggers() {
		return activationTriggers;
	}

	public EnchantmentDesc setActivationTriggers(EventTriggerDesc[] activationTriggers) {
		this.activationTriggers = activationTriggers;
		return this;
	}

	/**
	 * When set, these triggers will expire this enchantment when fired.
	 * <p>
	 * Expiration triggers are evaluated during the queueing phase. If an expiration trigger queues, the trigger is
	 * expired and will not fired. Thus you should use a {@link EventTriggerArg#QUEUE_CONDITION} if the trigger should
	 * have a condition to determine whether or not it expires.
	 */
	public EventTriggerDesc[] getExpirationTriggers() {
		return expirationTriggers;
	}

	public EnchantmentDesc setExpirationTriggers(EventTriggerDesc[] expirationTriggers) {
		this.expirationTriggers = expirationTriggers;
		return this;
	}

	/**
	 * A name field to use when rendering the enchantment on the client
	 */
	public String getName() {
		return name;
	}

	public EnchantmentDesc setName(String name) {
		this.name = name;
		return this;
	}

	/**
	 * A description field to use when rendering the enchantment on the client.
	 */
	public String getDescription() {
		return description;
	}

	public EnchantmentDesc setDescription(String description) {
		this.description = description;
		return this;
	}

	/**
	 * Zones where this enchantment is activated.
	 */
	public Zones[] getZones() {
		return zones;
	}

	public EnchantmentDesc setZones(Zones[] zones) {
		this.zones = zones;
		return this;
	}

	@Override
	@Suspendable
	public Optional<Enchantment> tryCreate(GameContext context, Player player, Entity effectSource, Card enchantmentSource, Entity host, boolean force) {
		return context.getLogic().tryCreateEnchantment(player, this, effectSource, enchantmentSource, host, force);
	}
}
