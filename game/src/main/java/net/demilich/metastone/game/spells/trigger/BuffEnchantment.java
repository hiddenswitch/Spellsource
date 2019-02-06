package net.demilich.metastone.game.spells.trigger;

import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.spells.NullSpell;
import net.demilich.metastone.game.spells.desc.SpellDesc;

public class BuffEnchantment extends Enchantment {

	public BuffEnchantment(int attack, int hp, String name, Trigger revertTrigger) {
		setAttribute(Attribute.ATTACK_BONUS, attack);
		setAttribute(Attribute.HP_BONUS, hp);
		if (name != null) {
			setName(name);
		}
		if (revertTrigger != null) {
			usesSpellTrigger = true;
			maxFires = 1;
			spell = NullSpell.create();
		} else {
			usesSpellTrigger = false;
		}
	}

	public BuffEnchantment(int attack, int hp, String name) {
		this(attack, hp, name, null);
	}

	public BuffEnchantment(int attack, int hp) {
		this(attack, hp, null, null);
	}
}
