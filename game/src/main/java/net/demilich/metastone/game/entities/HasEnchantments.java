package net.demilich.metastone.game.entities;

import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.trigger.Enchantment;

import java.util.List;

public interface HasEnchantments {
	void addDeathrattle(SpellDesc deathrattleSpell);

	void addEnchantment(Enchantment enchantment);

	void clearEnchantments();

	List<Enchantment> getEnchantments();

	boolean hasEnchantment();
}
