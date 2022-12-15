package net.demilich.metastone.game.spells.custom;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.OpenerAction;
import net.demilich.metastone.game.actions.OpenerAsPlaySpellCardAction;
import net.demilich.metastone.game.actions.PlaySpellCardAction;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.cards.ChooseOneOverride;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.environment.Environment;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.SpellUtils;
import net.demilich.metastone.game.spells.TargetPlayer;
import net.demilich.metastone.game.spells.desc.OpenerDesc;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.targeting.TargetSelection;
import net.demilich.metastone.game.cards.Attribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static net.demilich.metastone.game.spells.SpellUtils.determineCastingPlayer;

/**
 * Retrieves a list of cards using {@link SpellUtils#getCards(GameContext, Player, Entity, Entity, SpellDesc, int)} and
 * plays their openers with this {@code source} actor as the source.
 *
 * If the {@code source} is not an actor, an exception will be thrown.
 */
public final class RepeatAllOtherBattlecriesSpell extends Spell {

	private static Logger logger = LoggerFactory.getLogger(RepeatAllOtherBattlecriesSpell.class);

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		// Retrieve all other battlecry cards this player has played
		CardList cards = desc.getFilteredCards(context, player, source);
		cards.shuffle(context.getLogic().getRandom());

		logger.debug("onCast {} {}: {} battlecries were selected.", context.getGameId(), source, cards.size());

		player.modifyAttribute(Attribute.RANDOM_CHOICES, 1);
		context.getOpponent(player).modifyAttribute(Attribute.RANDOM_CHOICES, 1);
		// Replay the battlecries with targets chosen randomly
		for (int i = 0; i < cards.size(); i++) {
			Card card = cards.get(i);
			if (!card.hasBattlecry()) {
				logger.error("onCast {} {}: Matched a card {} that does not have a opener specified.", context.getGameId(), source, card);
				continue;
			}

			SpellUtils.DetermineCastingPlayer determineCastingPlayer = determineCastingPlayer(context, player, source, TargetPlayer.SELF);
			// Stop casting battlecries if Shudderwock is transformed or destroyed
			if (!determineCastingPlayer.isSourceInPlay()) {
				break;
			}

			Player castingPlayer = determineCastingPlayer.getCastingPlayer();

			// Execute the battlecry on a random target
			if (!castBattlecryRandomly(context, castingPlayer, card, (Actor) source)) {
				continue;
			}

			context.getEnvironment().remove(Environment.TARGET);
			context.getLogic().endOfSequence();
		}

		player.modifyAttribute(Attribute.RANDOM_CHOICES, -1);
		context.getOpponent(player).modifyAttribute(Attribute.RANDOM_CHOICES, -1);
	}

	/**
	 * Executes battlecries from the given card with random targets
	 *
	 * @param context             The game context
	 * @param player              The player who is casting the effect
	 * @param battlecryCardSource The card from which the battlecry is being read
	 * @param battlecrySource     The source entity that is actually casting the battlecry. Should typically be an actor.
	 * @return
	 */
	public static boolean castBattlecryRandomly(GameContext context, Player player, Card battlecryCardSource, Actor battlecrySource) {
		OpenerAction action;
		OpenerDesc[] chooseOneBattlecries = battlecryCardSource.getDesc().getChooseOneBattlecries();

		if (chooseOneBattlecries != null) {
			if (battlecryCardSource.getAttributes().containsKey(Attribute.CHOICE)) {
				int choice = battlecryCardSource.getAttributeValue(Attribute.CHOICE);
				if (choice == -1) {
					action = battlecryCardSource.getDesc().getChooseBothBattlecry().toOpenerAction();
				} else {
					action = chooseOneBattlecries[choice].toOpenerAction();
				}

			} else {

				// Choose randomly
				action = chooseOneBattlecries[context.getLogic().getRandom().nextInt()].toOpenerAction();
			}

			// Make a rules-based choice
			ChooseOneOverride chooseOneOverride = context.getLogic().getChooseOneAuraOverrides(player, battlecryCardSource);
			if (chooseOneOverride != ChooseOneOverride.NONE) {
				switch (chooseOneOverride) {
					case ALWAYS_FIRST:
						action = chooseOneBattlecries[0].toOpenerAction();
						break;
					case ALWAYS_SECOND:
						action = chooseOneBattlecries[1].toOpenerAction();
						break;
					case BOTH_COMBINED:
						action = battlecryCardSource.getDesc().getChooseBothBattlecry().toOpenerAction();
						break;
				}
			}
		} else {
			action = battlecryCardSource.getDesc().getBattlecry().toOpenerAction();
		}

		if (action == null) {
			logger.error("onCast {} {}: Matched a card {} that does not have a battlecry action.", context.getGameId(), battlecrySource, battlecryCardSource);
			return true;
		}

		// Skip calls to this specific battlecry
		if (action.getSpell() != null && action.getSpell().getDescClass().equals(RepeatAllOtherBattlecriesSpell.class)) {
			logger.debug("onCast {} {}: Matched a card {} that has RepeatAllOtherBattlecriesSpell, so it was skipped.", context.getGameId(), battlecrySource, battlecryCardSource);
			return false;
		}


		action = action.clone();
		action.setSourceReference(battlecrySource.getReference());
		EntityReference battlecryTarget;
		if (action.getTargetRequirement() != TargetSelection.NONE) {
			PlaySpellCardAction spellCardAction = new OpenerAsPlaySpellCardAction(action.getSourceReference(), action.getSpell(), battlecryCardSource, action.getTargetRequirement(), action.getCondition());
			// Compute the battlecry's valid targets as though it was a spell
			List<Entity> targets = context.getLogic().getValidTargets(player.getId(), spellCardAction);
			if (targets != null && !targets.isEmpty()) {
				// They shouldn't actually be able to target the source
				targets.remove(battlecrySource);
			}
			if (targets == null || targets.isEmpty()) {
				context.getLogic().revealCard(player, battlecryCardSource);
				context.getLogic().endOfSequence();
				return false;
			}
			battlecryTarget = context.getLogic().getRandom(targets).getReference();
			action.setTargetReference(battlecryTarget);
			context.getEnvironment().put(Environment.TARGET, battlecryTarget);
		} else {
			battlecryTarget = EntityReference.NONE;
			if (action.getCondition() != null && !action.getCondition().isFulfilled(context, player, battlecrySource, null)) {
				return false;
			}
		}

		// Actually execute the battlecry
		context.getLogic().revealCard(player, battlecryCardSource);
		context.getLogic().castSpell(player.getId(), action.getSpell(), battlecrySource.getReference(), battlecryTarget, action.getTargetRequirement(), false, action);
		return true;
	}
}
