package net.demilich.metastone.game.cards;

import net.demilich.metastone.game.spells.desc.SpellDesc;

import java.util.List;

public interface HasDeathrattleEnchantments {
	void addDeathrattle(SpellDesc deathrattle);

	List<SpellDesc> getDeathrattleEnchantments();
}
