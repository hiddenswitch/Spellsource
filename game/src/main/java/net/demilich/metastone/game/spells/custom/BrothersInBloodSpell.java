package net.demilich.metastone.game.spells.custom;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.CardType;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.EntityType;
import net.demilich.metastone.game.spells.*;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.trigger.EnchantmentDesc;
import net.demilich.metastone.game.spells.desc.valueprovider.AttributeValueProvider;
import net.demilich.metastone.game.spells.trigger.MinionSummonedTrigger;
import net.demilich.metastone.game.targeting.EntityReference;

/**
 * Gives the {@code source} (or the player entity, when the source is a spell or not in play) the enchantment, "Whenever
 * a minion whose card is the same as the chosen minion is summoned, double the newly summoned minion's stats."
 * <p>
 * Implements Brothers in Blood.
 */
public final class BrothersInBloodSpell extends Spell {

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		Entity host;
		if (source == null
				|| !source.isInPlay()
				|| (source.getEntityType() == EntityType.CARD && source.getSourceCard().getCardType() == CardType.SPELL)) {
			host = player;
		} else {
			host = source;
		}
		String cardId = target.getSourceCard().getCardId();
		EnchantmentDesc enchantment = new EnchantmentDesc();
		enchantment.eventTrigger = MinionSummonedTrigger.create(TargetPlayer.SELF, cardId);
		enchantment.spell = MetaSpell.create(DoubleAttackSpell.create(EntityReference.EVENT_TARGET), BuffSpell.create(EntityReference.EVENT_TARGET, null, AttributeValueProvider.create(Attribute.HP).create()));
		SpellUtils.castChildSpell(context, player, AddEnchantmentSpell.create(enchantment), source, host);
	}
}
