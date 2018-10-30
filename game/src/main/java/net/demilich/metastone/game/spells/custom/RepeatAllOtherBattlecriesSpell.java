package net.demilich.metastone.game.spells.custom;

import com.github.fromage.quasi.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.BattlecryAction;
import net.demilich.metastone.game.actions.PlaySpellCardAction;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.environment.Environment;
import net.demilich.metastone.game.spells.CastRandomSpellSpell;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.TargetPlayer;
import net.demilich.metastone.game.spells.desc.BattlecryDesc;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.targeting.TargetSelection;
import net.demilich.metastone.game.cards.Attribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

		player.setAttribute(Attribute.RANDOM_CHOICES);
		context.getOpponent(player).setAttribute(Attribute.RANDOM_CHOICES);
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
			BattlecryDesc battlecryDesc = card.getDesc().getBattlecry();
			// Skip calls to this specific battlecry
			if (battlecryDesc.getSpell() != null && battlecryDesc.getSpell().getDescClass().equals(RepeatAllOtherBattlecriesSpell.class)) {
				logger.debug("onCast {} {}: Matched a card {} that has RepeatAllOtherBattlecriesSpell, so it was skipped.", context.getGameId(), source, card);
				continue;
			}

			// Execute the battlecry on a random target
			BattlecryAction action;
			if (card.getAttributes().containsKey(Attribute.CHOICE)) {
				int choice = card.getAttributeValue(Attribute.CHOICE);
				if (choice == -1) {
					action = card.getDesc().getChooseBothBattlecry().toBattlecryAction();
				} else {
					action = card.getDesc().getChooseOneBattlecries()[choice].toBattlecryAction();
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
			if (action.getTargetRequirement() != TargetSelection.NONE) {
				// Compute the battlecry's valid targets as though it was a spell, so that the battlecry can target Shudderwock
				PlaySpellCardAction spellCardAction = new PlaySpellCardAction(battlecryDesc.spell, card, battlecryDesc.targetSelection);
				spellCardAction.setSource(action.getSourceReference());
				List<Entity> targets = context.getLogic().getValidTargets(castingPlayer.getId(), spellCardAction);
				if (targets.isEmpty()) {
					context.getLogic().revealCard(player, card);
					context.getLogic().endOfSequence();
					continue;
				}
				battlecryTarget = context.getLogic().getRandom(targets).getReference();
				action.setTargetReference(battlecryTarget);
				context.getEnvironment().put(Environment.TARGET, battlecryTarget);
			} else {
				battlecryTarget = EntityReference.NONE;
			}
			// Actually execute the battlecry
			context.getLogic().revealCard(player, card);

			context.getLogic().castSpell(castingPlayer.getId(), action.getSpell(), source.getReference(), battlecryTarget, action.getTargetRequirement(), false, action);
			context.getEnvironment().remove(Environment.TARGET);
			context.getLogic().endOfSequence();
		}

		player.getAttributes().remove(Attribute.RANDOM_CHOICES);
		context.getOpponent(player).getAttributes().remove(Attribute.RANDOM_CHOICES);
	}
}
