package net.demilich.metastone.game.spells;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.targeting.IdFactory;
import net.demilich.metastone.game.targeting.TargetSelection;
import com.hiddenswitch.spellsource.rpc.Spellsource.ZonesMessage.Zones;

import java.util.Map;

/**
 * Casts the specified {@link SpellArg#CARD} or the card targeted by {@link SpellArg#SECONDARY_TARGET} onto the {@code
 * target}. Currently only tested with spell cards.
 * <p>
 * To cast a choose one spell card, cast the choice card.
 *
 * Only casts on {@code target} if it is still valid (i.e., {@code target.isInPlay()} is {@code true}).
 * <p>
 * Uses {@link SpellArg#EXCLUSIVE} to signal that the cards should be created and put into the graveyard as they're
 * cast, rather than just their effects being enacted.
 * <p>
 * When using {@link SpellArg#SECONDARY_TARGET} to specify the spell card, the reference must resolve to a
 * {@link Card} entity. In trigger contexts, be aware that different event types populate {@code EVENT_SOURCE}
 * and {@code EVENT_TARGET} differently:
 * <ul>
 *     <li>{@link net.demilich.metastone.game.events.AfterSpellCastedEvent}: {@code EVENT_SOURCE} = spell card,
 *     {@code EVENT_TARGET} = the spell's target (a minion/hero, NOT a card). Use {@code EVENT_SOURCE} to reference
 *     the cast spell card.</li>
 *     <li>{@link net.demilich.metastone.game.events.CardPlayedEvent}: {@code EVENT_SOURCE} = played card,
 *     {@code EVENT_TARGET} = {@code null}. Use {@code EVENT_SOURCE}.</li>
 * </ul>
 * Using the wrong reference will cause a {@link ClassCastException} (if it resolves to a non-Card entity) or
 * a {@link NullPointerException} (if it resolves to {@code null}).
 * <p>
 * For example, to cast Inner Fire on every minion in your deck:
 * <pre>
 *   {
 *     "class": "CastCardsSpell",
 *     "card": "spell_inner_fire",
 *     "target": "FRIENDLY_DECK",
 *     "filter": {
 *       "class": "CardFilter",
 *       "cardType": "MINION"
 *     }
 *   }
 * </pre>
 */
public final class CastCardsSpell extends Spell {

	public static SpellDesc create(String card) {
		Map<SpellArg, Object> arguments = new SpellDesc(CastCardsSpell.class);
		arguments.put(SpellArg.CARD, card);
		return new SpellDesc(arguments);
	}

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		EntityReference secondaryTarget = (EntityReference) desc.get(SpellArg.SECONDARY_TARGET);
		Card card;
		if (secondaryTarget != null) {
			card = context.resolveSingleTarget(player, source, secondaryTarget);
		} else {
			card = SpellUtils.getCard(context, desc);
		}

		if (card == null) {
			return;
		}
		if (desc.getBool(SpellArg.EXCLUSIVE)) {
			card.setId(context.getLogic().generateId());
			card.setOwner(player.getIndex());
			card.moveOrAddTo(context, Zones.GRAVEYARD);
		}

		var requiresTarget = !card.getTargetSelection().equals(TargetSelection.NONE);
		// A targeted spell cannot be cast if the target is null or has left play
		if (requiresTarget && (target == null || !target.isInPlay())) {
			return;
		}
		// An untargeted spell is cast as long as the target hasn't left play (target may be null for NONE spells)
		if (!requiresTarget && target != null && !target.isInPlay()) {
			return;
		}
		if (card.isSpell()
				&& card.getSpell() != null) {
			SpellUtils.castChildSpell(context, player, card.getSpell().removeArg(SpellArg.FILTER),
					card.getId() == IdFactory.UNASSIGNED ? source : card,
					requiresTarget ? target : null);
		}
	}
}


