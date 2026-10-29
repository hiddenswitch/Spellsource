package net.demilich.metastone.game.spells;

import com.hiddenswitch.spellsource.rpc.Spellsource.EntityTypeMessage.EntityType;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.IdFactory;

/**
 * Shuffles the {@code target} {@link EntityType#MINION} into the player's deck with the enchantments
 */
public class ShuffleToDeckWithEnchantmentsSpell extends ShuffleMinionToDeckSpell {

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		if (target == null) {
			return;
		}

		if (!(target instanceof Actor)) {
			return;
		}

		var actor = (Actor) target;
		var baseCard = context.getCardById(target.getSourceCard().getCardId());
		if (!actor.isDestroyed()) {
			context.getLogic().removeActor(actor, true);
		}

		// Shuffles a copy of Immortal Prelate back into the deck
		Card card = CopyCardSpell.copyCard(context, player, source, baseCard, (playerId, copiedCard) -> {
			moveCopyToDestination(context, player, target, copiedCard);
		});

		// card is null if copyCard fails (e.g. transform reference), or if the deck is full the copy
		// won't have an assigned ID since shuffleToDeck doesn't add it to any zone
		if (card == null || card.getId() == IdFactory.UNASSIGNED) {
			return;
		}

		SpellDesc baseDeathrattle = baseCard.getDesc().getDeathrattle();
		var baseBattlecry = baseCard.getDesc().getBattlecry();
		var enchantments = context.getLogic().copyEnchantments(player, source, target, card, ench -> {
			if (ench.oneTurnOnly()) {
				return false;
			}
			if (ench instanceof net.demilich.metastone.game.spells.trigger.Aftermath a) {
				return baseDeathrattle == null || a.getSpell() != baseDeathrattle;
			}
			if (ench instanceof net.demilich.metastone.game.spells.trigger.Opener o) {
				return baseBattlecry == null || o.getOpenerDesc() != baseBattlecry;
			}
			return true;
		}, true);
		for (var enchantment : enchantments) {
			enchantment.setCopyToActor(true);
		}
	}

	protected void moveCopyToDestination(GameContext context, Player player, Entity target, Card copiedCard) {
		// Da Undatakah interaction
		if (!copiedCard.hasAttribute(Attribute.KEEPS_ENCHANTMENTS)) {
			copiedCard.setAttribute(Attribute.KEEPS_ENCHANTMENTS);
		}
		context.getLogic().shuffleToDeck(player, copiedCard, false);
		SpellUtils.processKeptEnchantments(target, copiedCard);
	}
}

