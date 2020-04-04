package net.demilich.metastone.game.spells.custom;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardType;
import net.demilich.metastone.game.cards.desc.CardDesc;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.ChangeHeroPowerSpell;
import net.demilich.metastone.game.spells.HeroPowerSpell;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * Creates a skill from the specified spell.
 * <p>
 * Skills ({@link net.demilich.metastone.game.cards.CardType#HERO_POWER}) are exactly the same as {@link
 * net.demilich.metastone.game.cards.CardType#SPELL} but with a different card type and with the {@link
 * CardDesc#getSpell()} field wrapped as a {@link net.demilich.metastone.game.spells.HeroPowerSpell}.
 */
public final class SpellToHeroPowerSpell extends ChangeHeroPowerSpell {

	private static Logger LOGGER = LoggerFactory.getLogger(SpellToHeroPowerSpell.class);

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		if (target == null) {
			return;
		}
		if (!(target instanceof Card)) {
			LOGGER.error("onCast {} {}: {} not card", context.getGameId(), source, target);
			return;
		}
		if (!((Card) target).getCardType().isCardType(CardType.SPELL)) {
			LOGGER.error("onCast {} {}: {} not spell", context.getGameId(), source, target);
			return;
		}
		var targetCard = (Card) target;
		var heroPowerCardDesc = targetCard.getDesc().clone();
		heroPowerCardDesc.setType(CardType.HERO_POWER);
		heroPowerCardDesc.setSpell(HeroPowerSpell.create(heroPowerCardDesc.getSpell()));
		heroPowerCardDesc.setId(context.getLogic().generateCardId());
		// That's... it?
		var card = heroPowerCardDesc.create();
		context.addTempCard(card);
		super.onCast(context, player, ChangeHeroPowerSpell.create(card.getCardId()), source, target);
	}
}
