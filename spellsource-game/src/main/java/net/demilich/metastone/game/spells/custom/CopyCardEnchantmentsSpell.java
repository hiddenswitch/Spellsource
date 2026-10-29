package net.demilich.metastone.game.spells.custom;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.SpellUtils;
import net.demilich.metastone.game.spells.aura.Aura;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.aura.AuraDesc;
import net.demilich.metastone.game.spells.desc.trigger.EnchantmentDesc;
import net.demilich.metastone.game.spells.trigger.Enchantment;
import net.demilich.metastone.game.spells.trigger.Trigger;
import net.demilich.metastone.game.targeting.IdFactory;

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

				boolean copyToActor = applyingTo instanceof Card;
				if (applyingTo instanceof Card || applyingTo instanceof Actor) {
					for (EnchantmentDesc enchantmentDesc : triggers) {
						Enchantment enchantment = enchantmentDesc.create();
						enchantment.setEffectSource(source)
								.setSourceCard(card);
						enchantment.setCopyToActor(copyToActor);
						enchantment.setOwner(player.getId());
						context.getLogic().addEnchantment(player, enchantment, source, applyingTo);
					}
					for (AuraDesc auraDesc : auras) {
						Aura aura = auraDesc.create();
						aura.setEffectSource(source)
								.setSourceCard(card);
						aura.setCopyToActor(copyToActor);
						aura.setOwner(player.getId());
						context.getLogic().addEnchantment(player, aura, source, applyingTo);
					}
					if (card.getId() != IdFactory.UNASSIGNED) {
						for (Trigger t : context.getLogic().getActiveTriggers(card.getReference())) {
							if (t instanceof Enchantment ench && ench.isCopyToActor()) {
								Enchantment copy = ench.getCopy();
								copy.setCopyToActor(copyToActor);
								context.getLogic().addEnchantment(player, copy, source, applyingTo);
							}
						}
					}
				}
			}
		}
	}
}
