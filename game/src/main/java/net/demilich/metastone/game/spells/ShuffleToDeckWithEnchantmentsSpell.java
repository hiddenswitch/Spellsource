package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.spellsource.client.models.EntityType;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.aura.Aura;
import net.demilich.metastone.game.spells.custom.AddEnchantmentToMinionCardSpell;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.trigger.EnchantmentDesc;
import net.demilich.metastone.game.spells.trigger.Enchantment;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * Shuffles the {@code target} {@link EntityType#MINION} into the player's deck
 * with the enchantments
 */
public class ShuffleToDeckWithEnchantmentsSpell extends Spell {

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
				.filter(e -> !e.isExpired())
				.collect(toList());

		Card sourceCard = target.getSourceCard();
		List<EnchantmentDesc> enchantmentDescCopies = enchantments.stream()
				.filter(e -> !(e instanceof Aura))
				.filter(e -> e.getSourceCard().getId() != sourceCard.getId())
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

		List<Aura> auras = enchantments.stream()
				.filter(e -> e instanceof Aura)
				.filter(e -> e.getSourceCard().getId() != sourceCard.getId())
				.map(Aura.class::cast)
				.collect(toList());

		// Get the deathrattles.
		List<SpellDesc> deathrattles = new ArrayList<>(actor.getDeathrattleEnchantments());

		if (!actor.isDestroyed()) {
			context.getLogic().removeActor(actor, true);
		}

		// Remove the attack and HP bonuses from the source card, if they exist
		sourceCard.getAttributes().remove(Attribute.ATTACK_BONUS);
		sourceCard.getAttributes().remove(Attribute.HP_BONUS);
		// Shuffles a copy of Immortal Prelate back into the deck
		Card card = CopyCardSpell.copyCard(context, player, source, sourceCard, (playerId, copiedCard) -> {
			moveCopyToDestination(context, player, target, copiedCard);
		});


		for (EnchantmentDesc enchantmentDesc : enchantmentDescCopies) {
			SpellUtils.castChildSpell(context, player, AddEnchantmentToMinionCardSpell.create(card, enchantmentDesc), source, card);
		}

		for (Aura aura : auras) {
			SpellUtils.castChildSpell(context, player, AddEnchantmentToMinionCardSpell.create(card, aura), source, card);
		}

		// Also add the deathrattles
		for (int i = 0; i < deathrattles.size(); i++) {
			// Skip this deathrattle.
			SpellDesc deathrattle = deathrattles.get(i);
			if ((int) deathrattle.getOrDefault(SpellArg.AFTERMATH_ID, i) == (int) desc.getOrDefault(SpellArg.AFTERMATH_ID, -1)) {
				continue;
			}
			SpellUtils.castChildSpell(context, player, AddDeathrattleSpell.create(card.getReference(), deathrattle), source, card);
		}
	}

	@Suspendable
	protected void moveCopyToDestination(GameContext context, Player player, Entity target, Card copiedCard) {
		// Da Undatakah interaction
		if (!copiedCard.hasAttribute(Attribute.KEEPS_ENCHANTMENTS)) {
			copiedCard.setAttribute(Attribute.KEEPS_ENCHANTMENTS);
		}
		context.getLogic().shuffleToDeck(player, copiedCard, false);
		SpellUtils.processKeptEnchantments(target, copiedCard);
	}
}

