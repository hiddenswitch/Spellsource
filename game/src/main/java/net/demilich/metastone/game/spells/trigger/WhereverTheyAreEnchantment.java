package net.demilich.metastone.game.spells.trigger;

import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.spells.MetaSpell;
import net.demilich.metastone.game.spells.WhereverTheyAreSpell;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.condition.CardPropertyCondition;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerArg;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;
import net.demilich.metastone.game.targeting.EntityReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents an enchantment that affects a minion with the given {@code cardId} at construction time with whatever
 * {@code desc} spell was provided, "wherever it is."
 *
 * @see WhereverTheyAreSpell for more about this effect.
 */
public class WhereverTheyAreEnchantment extends Enchantment {
	private static Logger LOGGER = LoggerFactory.getLogger(WhereverTheyAreEnchantment.class);

	private final String cardId;

	public WhereverTheyAreEnchantment(String cardId, SpellDesc desc, Card sourceCard) {
		super();
		usesSpellTrigger = true;
		setSourceCard(sourceCard);
		if (!desc.getDescClass().equals(WhereverTheyAreSpell.class)) {
			LOGGER.warn("constructor: This (sourceCard {}) was not created with a WhereverTheyAreSpell", sourceCard);
		}
		this.cardId = cardId;
		SpellDesc enchantmentSpell = desc.clone();
		enchantmentSpell.put(SpellArg.CLASS, MetaSpell.class);
		enchantmentSpell.put(SpellArg.TARGET, EntityReference.EVENT_TARGET);
		enchantmentSpell.remove(SpellArg.ZONES);
		enchantmentSpell.remove(SpellArg.CARD);
		setSpell(enchantmentSpell);
		EventTriggerDesc eventTrigger = BeforeMinionSummonedTrigger.create();
		eventTrigger.put(EventTriggerArg.FIRE_CONDITION, CardPropertyCondition.create(cardId));
		getTriggers().add(eventTrigger.create());
	}

	public String getCardId() {
		return cardId;
	}
}
