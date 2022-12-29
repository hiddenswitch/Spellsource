package net.demilich.metastone.game.spells.custom;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardArrayList;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.SpellUtils;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;

/**
 * Casts the {@link SpellArg#SPELL} subspell with {@link EntityReference#OUTPUT} as a reference to {@link
 * SpellArg#VALUE} random cards (or source cards of actors) stored on the {@code source}.
 * <p>
 * If {@link SpellArg#SECONDARY_TARGET} is specified, retrieves the entity list keyed by that entity reference instead
 * of {@code source}.
 * <p>
 * Optionally specify how many copies of each card should be acted on by the sub spell with {@link SpellArg#HOW_MANY}
 * copies.
 */
public final class CastOnCardsInStorageSpell extends Spell {

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		Entity storageSource = source;
		int count = desc.getValue(SpellArg.VALUE, context, player, target, source, 1);
		int copies = desc.getValue(SpellArg.HOW_MANY, context, player, target, source, 1);
		if (copies == 0 || count == 0) {
			return;
		}

		if (desc.containsKey(SpellArg.SECONDARY_TARGET)) {
			storageSource = context.resolveSingleTarget(player, source, (EntityReference) desc.get(SpellArg.SECONDARY_TARGET));
		}

		CardList cards = new CardArrayList(EnvironmentEntityList.getList(context).getCards(context, storageSource))
				.shuffle(context.getLogic().getRandom());

		SpellDesc spell = desc.getSpell();
		for (int i = 0; i < count; i++) {
			if (cards.isEmpty()) {
				return;
			}
			Card card = cards.removeFirst();
			spell = spell.addArg(SpellArg.CARD, card.getCardId());
			SpellUtils.castChildSpell(context, player, spell, source, target, card);
			for (int j = 1; j < copies; j++) {
				SpellUtils.castChildSpell(context, player, spell, source, target, card.getCopy());
			}
		}
	}
}
