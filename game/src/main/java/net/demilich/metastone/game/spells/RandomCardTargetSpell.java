package net.demilich.metastone.game.spells;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.actions.PlaySpellCardAction;
import net.demilich.metastone.game.cards.*;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.targeting.TargetSelection;
import net.demilich.metastone.game.targeting.Zones;

import java.util.List;

public class RandomCardTargetSpell extends Spell {

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		Card card = SpellUtils.getCard(context, desc);
		SpellCard spellCard;

		GameAction action = null;

		if (ChooseOneCard.class.isAssignableFrom(card.getClass())) {
			ChooseOneCard chooseOneCard = (ChooseOneCard) card;
			int choice = context.getLogic().random(chooseOneCard.getChoiceCards().length);
			spellCard = (SpellCard) chooseOneCard.getChoiceCards()[choice];
		} else if (SpellCard.class.isAssignableFrom(card.getClass())) {
			spellCard = (SpellCard) card;
		} else {
			throw new RuntimeException("A non-spell card was passed into a RandomCardTargetSpell");
		}

		spellCard.setOwner(player.getId());
		spellCard.setId(context.getLogic().getIdFactory().generateId());
		spellCard.moveOrAddTo(context, Zones.SET_ASIDE_ZONE);

		if (spellCard.getTargetRequirement() == TargetSelection.NONE) {
			SpellUtils.castChildSpell(context, player, spellCard.getSpell(), source, null);
			spellCard.moveOrAddTo(context, Zones.REMOVED_FROM_PLAY);
			return;
		}

		action = new PlaySpellCardAction(spellCard.getSpell(), spellCard, spellCard.getTargetRequirement());
		List<Entity> targets = context.getLogic().getValidTargets(player.getId(), action);
		EntityReference randomTarget = null;
		if (targets != null && targets.size() != 0) {
			randomTarget = targets.get(context.getLogic().random(targets.size())).getReference();
			SpellUtils.castChildSpell(context, player, spellCard.getSpell(), source, context.resolveSingleTarget(randomTarget));
		}

		spellCard.moveOrAddTo(context, Zones.REMOVED_FROM_PLAY);
	}

}
