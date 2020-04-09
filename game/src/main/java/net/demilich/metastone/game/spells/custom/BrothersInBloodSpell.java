package net.demilich.metastone.game.spells.custom;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import com.hiddenswitch.spellsource.client.models.CardType;
import net.demilich.metastone.game.entities.Entity;
import com.hiddenswitch.spellsource.client.models.EntityType;
import net.demilich.metastone.game.spells.*;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.trigger.EnchantmentDesc;
import net.demilich.metastone.game.spells.trigger.MinionPlayedTrigger;
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
		enchantment.eventTrigger = MinionPlayedTrigger.create(TargetPlayer.SELF, cardId);
		enchantment.spell = SummonSpell.create();
		enchantment.spell.setTarget(EntityReference.EVENT_TARGET);
		SpellUtils.castChildSpell(context, player, AddEnchantmentSpell.create(enchantment), source, host);
	}
}
