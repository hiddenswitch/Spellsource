package net.demilich.metastone.game.spells;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.Entity;
import com.hiddenswitch.spellsource.rpc.Spellsource.EntityTypeMessage.EntityType;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.trigger.Enchantment;
import net.demilich.metastone.game.spells.trigger.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Expires and removes {@link SpellArg#HOW_MANY} copies (default {@link Integer#MAX_VALUE}) of the enchantments whose
 * {@link Entity#getSourceCard()} has the card ID of {@link net.demilich.metastone.game.spells.desc.SpellArg#CARD} from
 * the {@code target}.
 * <p>
 * If a target is specified and it resolves to an enchantment, removes that enchantment instead.
 */
public final class RemoveEnchantmentSpell extends Spell {
	private static Logger logger = LoggerFactory.getLogger(RemoveEnchantmentSpell.class);

	public static SpellDesc create(Enchantment target) {
		return new SpellDesc(RemoveEnchantmentSpell.class, target.getReference(), null, false);
	}

	public void onCast(GameContext context, Player player, Entity source, Entity target, CardList cards, @Default(Integer.MAX_VALUE) int howMany) {
		if (target != null && target.getEntityType() == EntityType.ENCHANTMENT) {
			var enchantment = (Enchantment) target;
			enchantment.onRemove(context);
			enchantment.expire(context);
			return;
		}
		if (cards.isEmpty()) {
			return;
		}
		Set<String> cardIds = cards.stream().map(Card::getCardId).collect(Collectors.toSet());
		for (Trigger e : context.getLogic().getActiveTriggers(target.getReference())) {
			if (howMany <= 0) {
				break;
			}
			if (e instanceof Enchantment enchantment) {
				Card srcCard = enchantment.getSourceCard();
				if (srcCard != null) {
					String cid = srcCard.getCardId();
					boolean matches = cardIds.contains(cid);
					if (!matches && cid != null) {
						int idx = cid.indexOf("__ench_");
						if (idx > 0 && cardIds.contains(cid.substring(0, idx))) {
							matches = true;
						}
					}
					if (matches) {
						howMany--;
						enchantment.onRemove(context);
						enchantment.expire(context);
					}
				}
			}
		}
	}
}
