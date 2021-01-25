package net.demilich.metastone.game.spells.custom;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.*;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import com.hiddenswitch.spellsource.rpc.Spellsource.ZonesMessage.Zones;

/**
 * Gives the {@link net.demilich.metastone.game.spells.desc.SpellArg#SECONDARY_TARGET} the deathrattle, "Summon {@code
 * target}", and removes {@code target} from wherever it is.
 * <p>
 * Implements Doctor Hatchett
 */
public final class PutIntoEggSpell extends RevealCardSpell {

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		Card card = target.getSourceCard();
		// Reveal the card
		super.onCast(context, player, desc, source, card);

		Entity egg = context.resolveSingleTarget(player, source, desc.getSecondaryTarget());

		// Add the deathrattle
		SpellUtils.castChildSpell(context, player, AddDeathrattleSpell.create(egg.getReference(), SummonSpell.create(TargetPlayer.SELF, card)), source, egg);

		// Change its description
		SpellUtils.castChildSpell(context, player, SetDescriptionSpell.create(String.format("Deathrattle: Summon a %d/%d %s", card.getAttack(), card.getHp(), card.getName())), source, egg);

		// Remove the card
		if (target.getZone() == Zones.HAND || target.getZone() == Zones.DECK) {
			context.getLogic().removeCard(card);
		} else if (target.isInPlay() && target instanceof Actor) {
			// Or destroy the target
			context.getLogic().destroy((Actor) target);
		}
	}
}
