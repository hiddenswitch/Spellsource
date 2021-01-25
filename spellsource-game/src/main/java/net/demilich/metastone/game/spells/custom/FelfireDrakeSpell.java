package net.demilich.metastone.game.spells.custom;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.SetAttackSpell;
import net.demilich.metastone.game.spells.SetHpSpell;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.SpellUtils;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.valueprovider.AttributeValueProvider;

/**
 * Sets the {@code target} minion's attack and health to the highest values for attack and health found among {@link
 * net.demilich.metastone.game.spells.desc.SpellArg#CARD_FILTER}-filtered cards in {@link
 * net.demilich.metastone.game.spells.desc.source.CardSource}.
 * <p>
 * Implements Felfire Drake.
 */
public final class FelfireDrakeSpell extends Spell {

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		CardList cards = SpellUtils.getCards(context, player, target, source, desc, 99);
		int attack = cards.stream().mapToInt(c -> AttributeValueProvider.provideValueForAttribute(context, Attribute.ATTACK, c)).max().orElse(-1);
		int hp = cards.stream().mapToInt(c -> AttributeValueProvider.provideValueForAttribute(context, Attribute.HP, c)).max().orElse(-1);

		if (attack == -1 || hp == -1) {
			return;
		}

		SpellUtils.castChildSpell(context, player, SetAttackSpell.create(attack), source, target);
		SpellUtils.castChildSpell(context, player, SetHpSpell.create(hp), source, target);
	}
}
