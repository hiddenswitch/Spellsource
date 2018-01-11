package net.demilich.metastone.game.spells;

import java.util.Map;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.cards.*;
import net.demilich.metastone.game.targeting.Zones;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.demilich.metastone.game.utils.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.filter.CardFilter;
import net.demilich.metastone.game.spells.desc.source.CardSource;

public class CastRandomSpellSpell extends Spell {
	Logger logger = LoggerFactory.getLogger(CastRandomSpellSpell.class);

	public static SpellDesc create(int value) {
		Map<SpellArg, Object> arguments = SpellDesc.build(CastRandomSpellSpell.class);
		arguments.put(SpellArg.VALUE, value);
		return new SpellDesc(arguments);
	}

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		internalYogg(context, player, desc, source, target);
	}

	@Suspendable
	private void internalYogg(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		CardFilter filter = (CardFilter) desc.get(SpellArg.CARD_FILTER);
		CardList spells = CardCatalogue.query(context.getDeckFormat(), CardType.SPELL);
		CardSource cardSource = (CardSource) desc.get(SpellArg.CARD_SOURCE);

		if (cardSource != null) {
			spells = cardSource.getCards(context, player);
		}

		CardList filteredSpells = new CardArrayList();
		for (Card spell : spells) {
			if (filter == null || filter.matches(context, player, spell, source)) {
				filteredSpells.addCard(spell);
			}
		}

		player.setAttribute(Attribute.RANDOM_CHOICES, true);

		int numberOfSpellsToCast = desc.getValue(SpellArg.VALUE, context, player, target, source, 1);
		for (int i = 0; i < numberOfSpellsToCast; i++) {
			// In case Yogg changes sides, this should case who the spells are being cast for.
			Player owner = context.getPlayer(source.getOwner());
			// If Yogg is removed from the board, stop casting spells.
			if (source.getZone() != Zones.BATTLEFIELD
					|| source.isDestroyed()) {
				break;
			}
			// Must retrieve a copy because castWithRandomTargets mutates the incoming spell card
			Card randomCard = context.getLogic().getRandom(filteredSpells).getCopy();
			logger.debug("Yogg-Saron chooses to play " + randomCard.getName());
			RandomCardTargetSpell.castCardWithRandomTargets(context, owner, source, randomCard);
			context.getLogic().checkForDeadEntities();
		}

		player.getAttributes().remove(Attribute.RANDOM_CHOICES);
	}

}
