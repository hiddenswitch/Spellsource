package net.demilich.metastone.game.spells.custom;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.MinionCard;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.*;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.valueprovider.AlgebraicOperation;
import net.demilich.metastone.game.utils.Attribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Stream;

public class TextifySpell extends Spell {
	private static Logger logger = LoggerFactory.getLogger(TextifySpell.class);

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity spellSource, Entity spellTarget) {
		// For now, don't support textifying anything but MinionCards directly
		if (!(spellTarget instanceof MinionCard)) {
			logger.warn("onCast {}: Attempting to target {}, which is not a MinionCard. Exiting gracefully.", context.getGameId(), spellTarget);
			return;
		}

		// Retrieve a random effect
		Card random = context.getLogic().getRandom(desc.getFilteredCards(context, player, spellSource));
		if (!(random instanceof MinionCard)) {
			logger.warn("onCast {}: Attempting to copy text from a non-MinionCard {}. Exiting gracefully.", context.getGameId(), random);
			return;
		}

		String name = desc.getString(SpellArg.NAME);


		// Replaces the target card into the source card with the same non-text attributes
		MinionCard source = (MinionCard) random;
		MinionCard target = (MinionCard) spellTarget;

		MinionCard replaced = (MinionCard) context.getLogic().replaceCard(player.getId(), target, source);
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

