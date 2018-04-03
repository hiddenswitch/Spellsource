package net.demilich.metastone.game.spells.desc.trigger;

import java.io.Serializable;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.trigger.Enchantment;

import static com.google.common.collect.Maps.immutableEntry;

/**
 * Data specifying a trigger, including what event it reacts to, what spell it casts, and various options.
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
 * @see Enchantment for more about enchantments.
 */
@JsonInclude(value = JsonInclude.Include.NON_DEFAULT)
public final class EnchantmentDesc extends AbstractMap<EnchantmentDescArg, Object> implements Serializable, Cloneable {

	public EnchantmentDesc() {
	}

	/**
	 * The description of which "event trigger" (reacting to which event) this trigger will react to
	 */
	public EventTriggerDesc eventTrigger;
	/**
	 * The spell that will be cast by an {@link Enchantment#onFire(int, SpellDesc, GameEvent)} invocation.
	 *
	 * @see Enchantment for more about enchantments.
	 */
	public SpellDesc spell;
	/**
	 * When {@code true}, indicates the enchantment should only last one turn.
	 */
	public boolean oneTurn;
	/**
	 * When {@code true}, indicates the owner of the enchantment for the purposes of evaluating the {@code player}
	 * argument of a {@link net.demilich.metastone.game.spells.Spell#onCast(GameContext, Player, SpellDesc, Entity,
	 * Entity)} invocation shouldn't change if the owner of the {@link Enchantment#hostReference} changes.
	 * <p>
	 * This implements Blessing of Wisdom, Power Word: Glory and other effects whose text would change depending on
	 * whose perspective the text is read from.
	 */
	public boolean persistentOwner;
	/**
	 * When {@code true}, this {@link Enchantment} should not be removed by a {@link
	 * net.demilich.metastone.game.logic.GameLogic#transformMinion(Minion, Minion)} or {@link
	 * net.demilich.metastone.game.logic.GameLogic#replaceCard(int, Card, Card)} effect.
	 * <p>
	 * Implements Shifter Zerus, Molten Blade and other in-hand every-turn-replacement effects.
	 */
	public boolean keepAfterTransform;
	/**
	 * The maximum number of times this trigger can fire until it expires.
	 * <p>
	 * When {@code null} (the default), the trigger can fire an unlimited number of times.
	 */
	public Integer maxFires;
	/**
	 * The number of times an {@link Enchantment} fires until it actually casts its spell.
	 * <p>
	 * Implements Quests and many other counting behaviours in triggers.
	 * <p>
	 * Typically {@link #maxFires} is set to the same value as {@code countUntilCast} to limit the trigger to casting a
	 * spell at most once, as soon as the {@link #eventTrigger} has fired {@code countUntilCast} times.
	 */
	public Integer countUntilCast;
	/**
	 * When {@code true}, treats the {@link GameContext#getEventValue()} as the amount to increment this enchantment's
	 * firing counter.
	 * <p>
	 * Implements spellstones.
	 */
	public boolean countByValue;

	@Override
	public Set<Entry<EnchantmentDescArg, Object>> entrySet() {
		return Sets.newHashSet(
				immutableEntry(EnchantmentDescArg.EVENT_TRIGGER, eventTrigger),
				immutableEntry(EnchantmentDescArg.SPELL, spell),
				immutableEntry(EnchantmentDescArg.ONE_TURN, oneTurn),
				immutableEntry(EnchantmentDescArg.PERSISTENT_OWNER, persistentOwner),
				immutableEntry(EnchantmentDescArg.MAX_FIRES, maxFires),
				immutableEntry(EnchantmentDescArg.COUNT_UNTIL_CAST, countUntilCast),
				immutableEntry(EnchantmentDescArg.COUNT_BY_VALUE, countByValue)
		);
	}

	/**
	 * Creates an enchantment represented by this configuration.
	 * <p>
	 * The enchantment's {@link Enchantment#setHost(Entity)} call should be applied immediately afterwards to specify
	 * the host of this enchantment. {@link net.demilich.metastone.game.spells.trigger.secrets.Quest} and {@link
	 * net.demilich.metastone.game.spells.trigger.secrets.Secret} enchantments exist in play, and by convention they
	 * have unspecified hosts.
	 *
	 * @return The enchantment
	 */
	@JsonIgnore
	public Enchantment create() {
		Enchantment enchantment = new Enchantment(eventTrigger.create(), spell, oneTurn);
		enchantment.setMaxFires(maxFires);
		enchantment.setPersistentOwner(persistentOwner);
		enchantment.setKeepAfterTransform(keepAfterTransform);
		enchantment.setCountUntilCast(countUntilCast);
		enchantment.setCountByValue(countByValue);
		return enchantment;
	}

}
