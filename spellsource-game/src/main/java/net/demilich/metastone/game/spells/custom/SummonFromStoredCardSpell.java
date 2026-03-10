package net.demilich.metastone.game.spells.custom;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.Card;
import com.hiddenswitch.spellsource.rpc.Spellsource.CardTypeMessage.CardType;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.SpellUtils;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Summons a minion from a card ID stored as a string attribute on the {@code source} entity.
 * <p>
 * The attribute used defaults to {@link Attribute#RESERVED_STRING_1} but can be overridden with {@link
 * SpellArg#ATTRIBUTE}.
 * <p>
 * This is used for effects like Shadow of Death where a spell token needs to remember which minion to summon. The card
 * ID is stored on the token via {@link StoreCardIdSpell} and read at cast time by this spell.
 */
public class SummonFromStoredCardSpell extends Spell {

	private static final Logger LOGGER = LoggerFactory.getLogger(SummonFromStoredCardSpell.class);

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		Attribute attribute = (Attribute) desc.getOrDefault(SpellArg.ATTRIBUTE, Attribute.RESERVED_STRING_1);
		String cardId = (String) source.getAttribute(attribute);
		if (cardId == null || cardId.isEmpty()) {
			LOGGER.warn("onCast {} {}: No stored card ID found in attribute {} on source {}", context.getGameId(), source, attribute, source);
			return;
		}
		Card card = context.getCardById(cardId);
		if (!GameLogic.isCardType(card.getCardType(), CardType.MINION)) {
			LOGGER.warn("onCast {} {}: Stored card {} is not a minion card", context.getGameId(), source, cardId);
			return;
		}
		int boardPosition = SpellUtils.getBoardPosition(context, player, desc, source);
		int count = desc.getValue(SpellArg.VALUE, context, player, target, source, 1);
		for (int i = 0; i < count; i++) {
			Minion minion = card.minion();
			if (context.getLogic().summon(player.getId(), minion, source, boardPosition, false)) {
				for (SpellDesc subSpell : desc.subSpells(0)) {
					SpellUtils.castChildSpell(context, player, subSpell, source, target, minion);
				}
			}
		}
	}
}
