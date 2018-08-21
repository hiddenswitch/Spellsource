package net.demilich.metastone.game.spells.trigger.secrets;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.EntityType;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.trigger.EnchantmentDesc;
import net.demilich.metastone.game.spells.trigger.EventTrigger;
import net.demilich.metastone.game.spells.trigger.Enchantment;

/**
 * A quest is an enchantment and entity that goes into the {@link net.demilich.metastone.game.targeting.Zones#QUEST}
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
public class Quest extends Enchantment {

	public Quest(EventTrigger trigger, SpellDesc spell, Card source, int countUntilCast) {
		super(trigger, spell);
		this.setSourceCard(source);
		this.setCountUntilCast(countUntilCast);
	}

	public Quest(EnchantmentDesc desc, Card source) {
		this(desc.eventTrigger.create(), desc.spell, source, desc.countUntilCast);
		setMaxFires(desc.maxFires);
		setKeepAfterTransform(desc.keepAfterTransform);
		setCountByValue(desc.countByValue);
		setPersistentOwner(desc.persistentOwner);
	}

	@Override
	protected boolean onFire(int ownerId, SpellDesc spell, GameEvent event) {
		final boolean spellFired = super.onFire(ownerId, spell, event);
		if (isInPlay() && spellFired) {
			Player owner = event.getGameContext().getPlayer(ownerId);
			event.getGameContext().getLogic().questTriggered(owner, this);
			expire();
		}
		return spellFired;
	}

	@Override
	@Suspendable
	public void onGameEvent(GameEvent event) {
		super.onGameEvent(event);
	}

	@Override
	public EntityType getEntityType() {
		return EntityType.QUEST;
	}

	@Override
	public Quest clone() {
		Quest clone = (Quest) super.clone();
		clone.setSourceCard(getSourceCard());
		return clone;
	}
}
