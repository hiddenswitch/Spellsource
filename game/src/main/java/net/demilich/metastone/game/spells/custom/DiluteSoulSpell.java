package net.demilich.metastone.game.spells.custom;

import co.paralleluniverse.fibers.Suspendable;
import com.google.common.collect.Sets;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import com.hiddenswitch.spellsource.client.models.EntityType;
import net.demilich.metastone.game.spells.*;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.trigger.EnchantmentDesc;
import net.demilich.metastone.game.spells.trigger.TurnEndTrigger;
import net.demilich.metastone.game.targeting.EntityReference;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toSet;

/**
 * Discards a card. At the end of the turn, puts {@link SpellArg#VALUE} copies of the discorded card back into the
 * player's hand.
 * <p>
 * Implements Dilute Soul.
 */
public final class DiluteSoulSpell extends Spell {

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		// Fix to work with Scepter of Sargeras.
		Set<Integer> beforeDiscard = player.getGraveyard()
				.stream()
				.filter(e -> e.getEntityType() == EntityType.CARD && e.hasAttribute(Attribute.DISCARDED))
				.map(Entity::getId)
				.collect(toSet());
		SpellDesc discardSpell = new SpellDesc(DiscardSpell.class);
		SpellUtils.castChildSpell(context, player, discardSpell, source, target);
		Set<Integer> afterDiscard = player.getGraveyard()
				.stream()
				.filter(e -> e.getEntityType() == EntityType.CARD && e.hasAttribute(Attribute.DISCARDED))
				.map(Entity::getId)
				.collect(toSet());
		List<Card> discarded = Sets.difference(afterDiscard, beforeDiscard)
				.stream()
				.map(id -> context.tryFind(new EntityReference(id)))
				.filter(Objects::nonNull)
				.filter(Card.class::isInstance)
				.map(Card.class::cast)
				.collect(Collectors.toList());
		// By process of elimination, this has led us to the cards that were discarded as a result of this effect
		for (Card toDiscard : discarded) {
			EnchantmentDesc enchantmentDesc = new EnchantmentDesc();
			enchantmentDesc.eventTrigger = TurnEndTrigger.create(TargetPlayer.SELF);
			enchantmentDesc.spell = CopyCardSpell.create(toDiscard, desc.getValue(SpellArg.VALUE, context, player, target, source, 2));
			enchantmentDesc.maxFires = 1;
			SpellDesc addEnchantment = AddEnchantmentSpell.create(EntityReference.FRIENDLY_PLAYER, enchantmentDesc);
			SpellUtils.castChildSpell(context, player, addEnchantment, source, target);
		}
	}
}
