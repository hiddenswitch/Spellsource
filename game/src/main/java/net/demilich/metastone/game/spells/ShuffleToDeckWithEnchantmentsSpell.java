package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.custom.AddEnchantmentToMinionCardSpell;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.trigger.EnchantmentDesc;
import net.demilich.metastone.game.spells.trigger.Enchantment;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;

/**
 * Shuffles the {@code target} {@link net.demilich.metastone.game.entities.EntityType#MINION} into the player's deck
 * with the enchantments
 */
public final class ShuffleToDeckWithEnchantmentsSpell extends ShuffleMinionToDeckSpell {

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		if (target == null) {
			return;
		}

		if (!(target instanceof Actor)) {
			return;
		}


		Actor actor = (Actor) target;
		List<Enchantment> enchantments = context.getTriggersAssociatedWith(actor.getReference()).stream()
				.filter(Enchantment.class::isInstance)
				.map(Enchantment.class::cast)
				.filter(Predicate.not(Enchantment::isExpired))
				.collect(toList());

		List<EnchantmentDesc> copies = enchantments.stream()
				.filter(e -> e.getSourceCard().getId() != target.getSourceCard().getId())
				.map(enchantment -> {
					EnchantmentDesc enchantmentDesc = new EnchantmentDesc();
					enchantmentDesc.eventTrigger = enchantment.getTriggers().get(0).getDesc().clone();
					enchantmentDesc.countByValue = enchantment.isCountByValue();
					enchantmentDesc.keepAfterTransform = enchantment.isKeptAfterTransform();
					enchantmentDesc.maxFires = enchantment.getMaxFires();
					enchantmentDesc.spell = enchantment.getSpell().clone();
					enchantmentDesc.oneTurn = enchantment.oneTurnOnly();
					enchantmentDesc.persistentOwner = enchantment.hasPersistentOwner();
					return enchantmentDesc;
				})
				.collect(toList());

		// Get the deathrattles.
		List<SpellDesc> deathrattles = new ArrayList<>(actor.getDeathrattleEnchantments());

		if (!actor.isDestroyed()) {
			context.getLogic().removeActor(actor, true);
		}

		// Shuffles a copy of Immortal Prelate back into the deck
		Card card = CopyCardSpell.copyCard(context, player, target.getSourceCard(), (playerId, copiedCard) -> {
			// Da Undatakah interaction
			if (!copiedCard.hasAttribute(Attribute.KEEPS_ENCHANTMENTS)) {
				copiedCard.setAttribute(Attribute.KEEPS_ENCHANTMENTS);
			}
			context.getLogic().shuffleToDeck(player, copiedCard, false);
			SpellUtils.processKeptEnchantments(target, copiedCard);
		});


		for (EnchantmentDesc enchantmentDesc : copies) {
			SpellUtils.castChildSpell(context, player, AddEnchantmentToMinionCardSpell.create(card, enchantmentDesc), source, card);
		}

		// Also add the deathrattles
		for (int i = 0; i < deathrattles.size(); i++) {
			// Skip this deathrattle.
			SpellDesc deathrattle = deathrattles.get(i);
			if ((int) deathrattle.getOrDefault(SpellArg.DEATHRATTLE_ID, i) == (int) desc.getOrDefault(SpellArg.DEATHRATTLE_ID, -1)) {
				continue;
			}
			SpellUtils.castChildSpell(context, player, AddDeathrattleSpell.create(card.getReference(), deathrattle), source, card);
		}
	}
}

