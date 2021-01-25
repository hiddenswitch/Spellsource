package net.demilich.metastone.game.spells.trigger.secrets;

import co.paralleluniverse.fibers.Suspendable;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import com.hiddenswitch.spellsource.rpc.Spellsource.EntityTypeMessage.EntityType;
import net.demilich.metastone.game.cards.desc.EnchantmentSerializer;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.trigger.EnchantmentDesc;
import net.demilich.metastone.game.spells.trigger.Enchantment;
import net.demilich.metastone.game.spells.trigger.EventTrigger;
import com.hiddenswitch.spellsource.rpc.Spellsource.ZonesMessage.Zones;

/**
 * A quest is an enchantment and entity that goes into the {@link com.hiddenswitch.spellsource.rpc.Spellsource.ZonesMessage.Zones#QUEST}
 * zone. The player typically triggers the quest {@link EnchantmentDesc#countUntilCast} times, when its {@link
 * EnchantmentDesc#spell} is cast.
 * <p>
 * For example, to implement the quest, "Quest: Gain 10 armor. Reward: The Coin":
 * <pre>
 *   {
 *     "eventTrigger": {
 *       "class": "ArmorChangedTrigger",
 *       "condition": {
 *         "class": "ComparisonCondition",
 *         "value1": {
 *           "class": "EventValueProvider"
 *         },
 *         "value2": 0,
 *         "operation": "GREATER"
 *       }
 *     },
 *     "spell": {
 *       "class": "ReceiveCardSpell",
 *       "card": "spell_the_coin"
 *     },
 *     "countUntilCast": 10,
 *     "countByValue": true
 *   }
 * </pre>
 * This is a complex quest. Observe that the quest can count up using an event's value by setting {@link
 * EnchantmentDesc#countByValue} to true (in this case, the amount of armor gained, computed by listening to armor
 * change events that have an event value greater than zero). {@link EnchantmentDesc#countUntilCast} is equal to the
 * amount of armor needed to be gained to fulfill the quest, in this case.
 *
 * @see EnchantmentDesc for a full description of all the fields that make a valid quest. Since a quest is just an
 * enchantment put into play a special way, the way it should be implemented is exactly the same as any other
 * enchantment.
 */
@JsonSerialize(using = EnchantmentSerializer.class)
public class Quest extends Enchantment {

	private static final Zones[] ZONES = new Zones[]{Zones.QUEST};
	private boolean isPact;

	public Quest(EnchantmentDesc desc, EventTrigger trigger, SpellDesc spell, Card source, int countUntilCast, boolean countByValue) {
		super(desc);
		getTriggers().add(trigger);
		setSpell(spell);
		setSourceCard(source);
		setCountUntilCast(countUntilCast);
		setCountByValue(countByValue);
	}

	public Quest(EnchantmentDesc desc, Card source) {
		this(desc, desc.getEventTrigger().create(), desc.getSpell(), source, desc.getCountUntilCast(), desc.isCountByValue());
		setMaxFires(desc.getMaxFires());
		setKeepAfterTransform(desc.isKeepAfterTransform());
		setCountByValue(desc.isCountByValue());
		setPersistentOwner(desc.isPersistentOwner());
	}

	/**
	 * Set to {@code true} by the {@link net.demilich.metastone.game.spells.AddPactSpell}.
	 *
	 * @param pact
	 */
	public void setPact(boolean pact) {
		isPact = pact;
	}

	/**
	 * Pacts are a kind of quest that can be triggered by either player.
	 *
	 * @return {@code true} if this quest should behave like a pact.
	 * @see net.demilich.metastone.game.spells.AddPactSpell for more on pacts.
	 */
	public boolean isPact() {
		return isPact;
	}

	@Override
	@Suspendable
	protected boolean process(int ownerId, SpellDesc spell, GameEvent event) {
		// Also casts the spell!
		boolean spellFired = super.process(ownerId, spell, event);
		if (isInPlay() && spellFired) {
			expire(event.getGameContext());
			Player owner = event.getGameContext().getPlayer(ownerId);
			event.getGameContext().getLogic().questTriggered(owner, this);
		}
		return spellFired;
	}

	@Override
	@Suspendable
	protected void cast(int ownerId, SpellDesc spell, GameEvent event) {
		expire(event.getGameContext());
		super.cast(ownerId, spell, event);
	}

	@Override
	public Zones[] getZones() {
		return ZONES;
	}

	@Override
	public EntityType getEntityType() {
		return EntityType.QUEST;
	}

	@Override
	public Quest clone() {
		return (Quest) super.clone();
	}
}
