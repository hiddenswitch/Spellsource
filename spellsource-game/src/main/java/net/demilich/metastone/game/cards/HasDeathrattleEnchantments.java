package net.demilich.metastone.game.cards;

import net.demilich.metastone.game.spells.desc.SpellDesc;

import java.util.List;

/**
 * Indicates this entity or object contains deathrattles.
 */
public interface HasDeathrattleEnchantments {
	void addDeathrattle(SpellDesc deathrattle);

	/**
	 * Removes all the deathrattles that were added by effects other than those printed on this card / actor.
	 */
	void clearAddedDeathrattles();

	List<SpellDesc> getDeathrattleEnchantments();
}
