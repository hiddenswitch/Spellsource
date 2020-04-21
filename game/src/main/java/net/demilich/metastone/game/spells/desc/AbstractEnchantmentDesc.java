package net.demilich.metastone.game.spells.desc;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.trigger.Enchantment;

import java.util.Optional;

public interface AbstractEnchantmentDesc<T extends Enchantment> {
	@Suspendable
	Optional<T> tryCreate(GameContext context, Player player, Entity effectSource, Card enchantmentSource, Entity host, boolean force);
}
