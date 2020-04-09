package net.demilich.metastone.game.spells.custom;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import com.hiddenswitch.spellsource.client.models.CardType;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.*;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.cards.Attribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Stream;

/**
 * Puts text from a random {@link SpellDesc#getFilteredCards(GameContext, Player, Entity)} card onto the {@code target}
 * {@link Card}. The attack, hitpoints, race and cost of the {@code target} are retained.
 * <p>
 * If the {@link SpellArg#NAME} attribute is {@code "ORIGINAL"}, the {@code target}'s name is not changed. Otherwise, it
 * is changed to the randomly chosen card's name.
 * <p>
 * Implements Fifi Fizzlewarp.
 */
public class TextifySpell extends Spell {
	private static Logger logger = LoggerFactory.getLogger(TextifySpell.class);

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity spellSource, Entity spellTarget) {
		// For now, don't support textifying anything but MinionCards directly
		if (!(spellTarget instanceof Card)
				|| ((Card) spellTarget).getCardType() != CardType.MINION) {
			logger.warn("onCast {}: Attempting to target {}, which is not a Card. Exiting gracefully.", context.getGameId(), spellTarget);
			return;
		}

		// Retrieve a random effect
		Card random = context.getLogic().getRandom(desc.getFilteredCards(context, player, spellSource));
		if (random == null) {
			logger.warn("onCast {}: Attempting to copy text from a non-Card {}. Exiting gracefully.", context.getGameId(), random);
			return;
		}

		String name = desc.getString(SpellArg.NAME);


		// Replaces the target card into the source card with the same non-text attributes
		Card source = random;
		Card target = (Card) spellTarget;

		Card replaced = context.getLogic().replaceCard(player.getId(), target, source);
		// At this point, the target is removed from play, so it cannot be queried in the regular targeting system for
		// its attributes. We will retrieve the attributes directly through the reference.
		Stream.of(
				SetAttackSpell.create(target.getBaseAttack(), true),
				SetHpSpell.create(target.getBaseHp(), true),
				SetRaceSpell.create(target.getRace()),
				SetAttributeSpell.create(replaced.getReference(), Attribute.BASE_MANA_COST, target.getBaseManaCost())
		).forEach(spellDesc -> SpellUtils.castChildSpell(context, player, spellDesc, spellSource, replaced));

		if (!name.equals("ORIGINAL")) {
			SpellUtils.castChildSpell(context, player, SetAttributeSpell.create(replaced.getReference(), Attribute.NAME, target.getName()), spellSource, replaced);
		}
	}
}

