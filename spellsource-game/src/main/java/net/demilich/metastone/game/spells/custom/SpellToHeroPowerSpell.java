package net.demilich.metastone.game.spells.custom;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import com.hiddenswitch.spellsource.rpc.Spellsource.CardTypeMessage.CardType;
import net.demilich.metastone.game.cards.desc.CardDesc;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.spells.ChangeHeroPowerSpell;
import net.demilich.metastone.game.spells.HeroPowerSpell;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates a skill from the specified spell.
 * <p>
 * Skills ({@link CardType#HERO_POWER}) are exactly the same as {@link
 * CardType#SPELL} but with a different card type and with the {@link
 * CardDesc#getSpell()} field wrapped as a {@link net.demilich.metastone.game.spells.HeroPowerSpell}.
 */
public final class SpellToHeroPowerSpell extends ChangeHeroPowerSpell {

	private static Logger LOGGER = LoggerFactory.getLogger(SpellToHeroPowerSpell.class);

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		if (target == null) {
			return;
		}
		if (!(target instanceof Card)) {
			LOGGER.error("onCast {} {}: {} not card", context.getGameId(), source, target);
			return;
		}
		if (!GameLogic.isCardType(((Card) target).getCardType(), CardType.SPELL)) {
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
