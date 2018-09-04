package net.demilich.metastone.game.spells;

import com.github.fromage.quasi.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.actions.PlaySpellCardAction;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.CardType;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.targeting.IdFactory;
import net.demilich.metastone.game.targeting.TargetSelection;
import net.demilich.metastone.game.targeting.Zones;
import net.demilich.metastone.game.utils.Attribute;

import java.util.Arrays;
import java.util.List;

public class RandomCardTargetSpell extends Spell {
	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		Card card = SpellUtils.getCard(context, desc);
		// If the spell is not exclusive, it will copy the target card. Otherwise, it will cast the card.
		final boolean exclusive = (boolean) desc.getOrDefault(SpellArg.EXCLUSIVE, false);
		if (card == null
				&& target != null
				&& target instanceof Card) {
			card = (Card) target;
			if (!exclusive) {
				card = card.getCopy();
			}
		}
		if (card == null) {
			return;
		}
		castCardWithRandomTargets(context, player, source, card);
	}

	@Suspendable
	public static void castCardWithRandomTargets(GameContext context, Player player, Entity source, Card card) {
		GameAction action;
		Card spellCard;
		if (card.hasChoices()) {
			spellCard = CardCatalogue.getCardById(context.getLogic().getRandom(Arrays.asList(card.getChooseOneCardIds())));
		} else if (card.getCardType() == CardType.SPELL) {
			spellCard = card;
		} else {
			throw new RuntimeException(String.format("castCardWithRandomTargets %s %s: A non-spell card %s was passed into a RandomCardTargetSpell", context.getGameId(), source.toString(), card.toString()));
		}

		// Makes random discover choices
		player.setAttribute(Attribute.RANDOM_CHOICES);

		Zones destination = Zones.REMOVED_FROM_PLAY;
		if (spellCard.getZone() == Zones.DECK
				|| spellCard.getZone() == Zones.HAND) {
			destination = Zones.GRAVEYARD;
		}

		if (spellCard.getId() == IdFactory.UNASSIGNED) {
			spellCard.setId(context.getLogic().generateId());
		}
		if (spellCard.getOwner() == IdFactory.UNASSIGNED) {
			spellCard.setOwner(player.getId());
		}

		spellCard.moveOrAddTo(context, Zones.SET_ASIDE_ZONE);

		context.getLogic().revealCard(player, spellCard);

		if (spellCard.getTargetSelection() == TargetSelection.NONE) {
			SpellUtils.castChildSpell(context, player, spellCard.getSpell(), source, null);
			spellCard.moveOrAddTo(context, destination);
			context.getLogic().removeCard(spellCard);
			player.getAttributes().remove(Attribute.RANDOM_CHOICES);
			return;
		}

		action = spellCard.play();
		List<Entity> targets = context.getLogic().getValidTargets(player.getId(), action);
		EntityReference randomTarget = null;
		if (targets != null && targets.size() != 0) {
			randomTarget = context.getLogic().getRandom(targets).getReference();
			SpellUtils.castChildSpell(context, player, spellCard.getSpell(), source, context.resolveSingleTarget(randomTarget));
		}

		spellCard.moveOrAddTo(context, destination);
		context.getLogic().removeCard(spellCard);
		player.getAttributes().remove(Attribute.RANDOM_CHOICES);
	}

}
