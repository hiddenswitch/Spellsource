package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.spellsource.rpc.Spellsource.EntityTypeMessage.EntityType;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellDesc;

/**
 * Shuffles the {@code target} {@link EntityType#MINION} into the player's deck with the enchantments
 */
public class ShuffleToDeckWithEnchantmentsSpell extends ShuffleMinionToDeckSpell {

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		if (target == null) {
			return;
		}

		if (!(target instanceof Actor)) {
			return;
		}

		var actor = (Actor) target;
		var sourceCard = target.getSourceCard();
		if (!actor.isDestroyed()) {
			context.getLogic().removeActor(actor, true);
		}

		// Remove the attack and HP bonuses from the source card, if they exist (why?)
		sourceCard.getAttributes().remove(Attribute.ATTACK_BONUS);
		sourceCard.getAttributes().remove(Attribute.HP_BONUS);
		// Shuffles a copy of Immortal Prelate back into the deck
		Card card = CopyCardSpell.copyCard(context, player, source, sourceCard, (playerId, copiedCard) -> {
			moveCopyToDestination(context, player, target, copiedCard);
		});

		if (card == null) {
			return;
		}

		var enchantments = context.getLogic().copyEnchantments(player, source, target, card, null, true);
		for (var enchantment : enchantments) {
			enchantment.setCopyToActor(true);
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

