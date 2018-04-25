package net.demilich.metastone.game.spells.custom;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.BattlecryAction;
import net.demilich.metastone.game.actions.PlayCardAction;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.environment.Environment;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.spells.CastRandomSpellSpell;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.SpellUtils;
import net.demilich.metastone.game.spells.TargetPlayer;
import net.demilich.metastone.game.spells.desc.BattlecryDesc;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.utils.Attribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.List;

import static net.demilich.metastone.game.spells.CastRandomSpellSpell.determineCastingPlayer;

public class RepeatAllOtherBattlecriesSpell extends Spell {

	private static Logger logger = LoggerFactory.getLogger(RepeatAllOtherBattlecriesSpell.class);

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		// Retrieve all other battlecry cards this player has played
		CardList cards = desc.getFilteredCards(context, player, source);
		cards.shuffle(context.getLogic().getRandom());

		logger.debug("onCast {} {}: {} battlecries were selected.", context.getGameId(), source, cards.size());

		// Replay the battlecries with targets chosen randomly
		for (int i = 0; i < cards.size(); i++) {
			Card card = cards.get(i);
			if (!card.hasBattlecry()) {
				logger.error("onCast {} {}: Matched a card {} that does not have a battlecry specified.", context.getGameId(), source, card);
				continue;
			}

			CastRandomSpellSpell.DetermineCastingPlayer determineCastingPlayer = determineCastingPlayer(context, player, source, TargetPlayer.SELF);
			// Stop casting battlecries if Shudderwock is transformed or destroyed
			if (!determineCastingPlayer.isSourceInPlay()) {
				break;
			}

			Player castingPlayer = determineCastingPlayer.getCastingPlayer();
			BattlecryDesc battlecryDesc = card.getDesc().battlecry;
			// Skip calls to this specific battlecry
			if (battlecryDesc.spell != null && battlecryDesc.spell.getDescClass().equals(RepeatAllOtherBattlecriesSpell.class)) {
				logger.debug("onCast {} {}: Matched a card {} that has RepeatAllOtherBattlecriesSpell, so it was skipped.", context.getGameId(), source, card);
				continue;
			}

			// Execute the battlecry on a random target
			BattlecryAction action;
			if (card.getAttributes().containsKey(Attribute.CHOICE)) {
				int choice = card.getAttributeValue(Attribute.CHOICE);
				if (choice == -1) {
					action = card.getDesc().chooseBothBattlecry.toBattlecryAction();
				} else {
					action = card.getDesc().chooseOneBattlecries[choice].toBattlecryAction();
				}
			} else {
				action = card.getDesc().getBattlecryAction();
			}
			if (action == null) {
				logger.error("onCast {} {}: Matched a card {} that does not have a battlecry action.", context.getGameId(), source, card);
				continue;
			}
			action = action.clone();
			action.setSource(source.getReference());
			EntityReference battlecryTarget;
			if (action.getPredefinedSpellTargetOrUserTarget() == null) {
				List<Entity> targets = context.getLogic().getValidTargets(castingPlayer.getId(), action);
				if (targets.isEmpty()) {
					context.getLogic().revealCard(player, card);
					context.getLogic().endOfSequence();
					continue;
				}
				battlecryTarget = context.getLogic().getRandom(targets).getReference();
				action.setTargetReference(battlecryTarget);
			} else {
				battlecryTarget = action.getPredefinedSpellTargetOrUserTarget();
			}
			// Actually execute the battlecry
			context.getLogic().revealCard(player, card);
			context.getEnvironment().put(Environment.TARGET, battlecryTarget);
			context.getLogic().castSpell(castingPlayer.getId(), action.getSpell(), source.getReference(), battlecryTarget, action.getTargetRequirement(), false, action);
			context.getEnvironment().remove(Environment.TARGET);
			context.getLogic().endOfSequence();
		}
	}
}
