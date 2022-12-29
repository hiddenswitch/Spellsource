package net.demilich.metastone.game.spells;

import com.hiddenswitch.spellsource.rpc.Spellsource.CardTypeMessage.CardType;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.desc.CardDesc;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;

/**
 * The definition of a {@link ChooseOneSpell}'s sub spells.
 */
public class ChooseOneOptionSpell extends MetaSpell {

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		super.onCast(context, player, desc, source, target);
		context.getLogic().revealCard(player, getTempCard(context, desc, source.getSourceCard()));
	}

	public Card getTempCard(GameContext context, SpellDesc spellDesc, Card sourceCard) {
		return ChooseOneOptionSpell.getTempCard(context, spellDesc, sourceCard, "option_");
	}

	/**
	 * Gets or create the temporary card representing the option.
	 *
	 * @param context    the game context
	 * @param spellDesc  the dummy spell to make a card from
	 * @param sourceCard the card making the choice
	 * @return the temp card for the option
	 */
	public static Card getTempCard(GameContext context, SpellDesc spellDesc, Card sourceCard, String prefix) {
		var name = spellDesc.getString(SpellArg.NAME);
		var tempCardId = prefix + sourceCard.getCardId() + "_" + format(name);
		if (context.getTempCards().containsCard(tempCardId)) {
			return context.getCardById(tempCardId);
		}
		var description = spellDesc.getString(SpellArg.DESCRIPTION);
		var mana = spellDesc.getInt(SpellArg.MANA, 0);
		var cardDesc = new CardDesc();
		cardDesc.setId(tempCardId);
		cardDesc.setName(name);
		cardDesc.setDescription(description);
		cardDesc.setBaseManaCost(mana);
		cardDesc.setType(CardType.SPELL);
		cardDesc.setHeroClass(sourceCard.getHeroClass());
		cardDesc.setSpell(spellDesc);
		var card = cardDesc.create();
		context.addTempCard(card);
		return card;
	}

	public static String format(String str) {
		return str.toLowerCase().replace(" ", "_").replace("-", "_").replace("'", "");
	}
}
