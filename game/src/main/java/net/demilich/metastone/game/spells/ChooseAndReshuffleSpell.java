package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.source.HandSource;
import net.demilich.metastone.game.spells.desc.valueprovider.PlayerAttributeValueProvider;

/**
 * Prompts the player to choose among cards in their hand. Then, the choice is shuffled into their deck.
 * <p>
 * Implements Timeweaver's Reshuffle keyword.
 */
public final class ChooseAndReshuffleSpell extends Spell {

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		int times = desc.getValue(SpellArg.VALUE, context, player, target, source, 1);
		SpellDesc discover = new SpellDesc(DiscoverSpell.class);
		discover.put(SpellArg.EXCLUSIVE, true);
		discover.put(SpellArg.CARD_SOURCE, HandSource.create());
		discover.put(SpellArg.HOW_MANY, PlayerAttributeValueProvider.create(PlayerAttribute.HAND_COUNT));
		discover.put(SpellArg.SPELL, new SpellDesc(ShuffleOriginalToDeckSpell.class));
		for (int i = 0; i < times; i++) {
			SpellUtils.castChildSpell(context, player, discover, source, target);
		}
	}
}
