package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.actions.PlaySpellCardAction;
import net.demilich.metastone.game.cards.*;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.CardRevealedEvent;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.targeting.TargetSelection;
import net.demilich.metastone.game.targeting.Zones;

import java.util.Arrays;
import java.util.List;

public class RandomCardTargetSpell extends Spell {
	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		Card card = SpellUtils.getCard(context, desc);
		if (card == null
				&& target != null
				&& target instanceof Card) {
			card = (Card) target.getCopy();
		}
		if (card == null) {
			return;
		}
		castCardWithRandomTargets(context, player, source, card);
	}

	@Suspendable
	public static void castCardWithRandomTargets(GameContext context, Player player, Entity source, Card card) {
		SpellCard spellCard;
		GameAction action;
		if (ChooseOneCard.class.isAssignableFrom(card.getClass())) {
			ChooseOneCard chooseOneCard = (ChooseOneCard) card;
			spellCard = (SpellCard) context.getLogic().getRandom(Arrays.asList(chooseOneCard.getChoiceCards()));
		} else if (SpellCard.class.isAssignableFrom(card.getClass())) {
			spellCard = (SpellCard) card;
		} else {
			throw new RuntimeException("A non-spell card was passed into a RandomCardTargetSpell");
		}

		Zones destination = Zones.REMOVED_FROM_PLAY;
		if (spellCard.getZone() == Zones.DECK
				|| spellCard.getZone() == Zones.HAND) {
			destination = Zones.GRAVEYARD;
		}

		spellCard.setOwner(player.getId());
		spellCard.setId(context.getLogic().generateId());
		spellCard.moveOrAddTo(context, Zones.SET_ASIDE_ZONE);

		context.getLogic().revealCard(player, spellCard);

		if (spellCard.getTargetRequirement() == TargetSelection.NONE) {
			SpellUtils.castChildSpell(context, player, spellCard.getSpell(), source, null);
			spellCard.moveOrAddTo(context, destination);
			context.getLogic().removeCard(spellCard);
			return;
		}

		action = new PlaySpellCardAction(spellCard.getSpell(), spellCard, spellCard.getTargetRequirement());
		List<Entity> targets = context.getLogic().getValidTargets(player.getId(), action);
		EntityReference randomTarget = null;
		if (targets != null && targets.size() != 0) {
			randomTarget = context.getLogic().getRandom(targets).getReference();
			SpellUtils.castChildSpell(context, player, spellCard.getSpell(), source, context.resolveSingleTarget(randomTarget));
		}

		spellCard.moveOrAddTo(context, destination);
		context.getLogic().removeCard(spellCard);
	}

}
