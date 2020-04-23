package net.demilich.metastone.game.spells.custom;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.SpellUtils;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.aura.AuraDesc;
import net.demilich.metastone.game.spells.desc.trigger.EnchantmentDesc;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Copies enchantments written on the {@link SpellUtils#getCards(GameContext, Player, Entity, Entity, SpellDesc, int)}
 * cards and the {@code target} (when specified) to the target specified in {@link
 * net.demilich.metastone.game.spells.desc.SpellArg#SECONDARY_TARGET}.
 * <p>
 * Implements The Dreadblade.
 */
public final class CopyCardEnchantmentsSpell extends Spell {

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		CardList cards = SpellUtils.getCards(context, player, target, source, desc, 99);
		Entity applyingTo = source;
		if (desc.getSecondaryTarget() != null) {
			applyingTo = context.resolveSingleTarget(player, source, desc.getSecondaryTarget());
		}
		List<Entity> copyingFrom = new ArrayList<>();
		if (target != null) {
			copyingFrom.add(target);
		}
		copyingFrom.addAll(cards);

		for (Entity entity : copyingFrom) {
			if (entity instanceof Card) {
				Card card = (Card) entity;
				List<EnchantmentDesc> triggers = new ArrayList<>();
				if (card.getDesc().getTrigger() != null) {
					triggers.add(card.getDesc().getTrigger());
				}
				if (card.getDesc().getTriggers() != null) {
					triggers.addAll(Arrays.asList(card.getDesc().getTriggers()));
				}
				List<AuraDesc> auras = new ArrayList<>();
				if (card.getDesc().getAura() != null) {
					auras.add(card.getDesc().getAura());
				}
				if (card.getDesc().getAuras() != null) {
					auras.addAll(Arrays.asList(card.getDesc().getAuras()));
				}

				if (triggers.isEmpty()) {
					return;
				}

				if (applyingTo instanceof Card) {
					Card applyingToCard = (Card) applyingTo;
					if (applyingToCard.getDesc().getTriggers() != null) {
						applyingToCard.getDesc().setTriggers((EnchantmentDesc[]) ArrayUtils.addAll(applyingToCard.getDesc().getTriggers(), triggers));
					} else applyingToCard.getDesc().setTriggers((EnchantmentDesc[]) triggers.toArray());

					if (applyingToCard.getDesc().getAuras() != null) {
						applyingToCard.getDesc().setAuras((AuraDesc[]) ArrayUtils.addAll(applyingToCard.getDesc().getAuras(), auras));
					} else applyingToCard.getDesc().setAuras((AuraDesc[]) auras.toArray());

				} else if (applyingTo instanceof Actor) {
					Actor applyingToActor = (Actor) applyingTo;

					for (EnchantmentDesc enchantmentDesc : triggers) {
						context.getLogic().addEnchantment(player, enchantmentDesc.create(), source, applyingToActor);
					}
					for (AuraDesc aura : auras) {
						context.getLogic().addEnchantment(player, aura.create(), source, applyingToActor);
					}
				}
			}
		}
	}
}
