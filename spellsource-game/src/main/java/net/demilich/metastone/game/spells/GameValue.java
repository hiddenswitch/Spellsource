package net.demilich.metastone.game.spells;

/**
 * Used in {@link MetaSpell} and {@link net.demilich.metastone.game.spells.desc.valueprovider.GameValueProvider} to
 * refer to specific values calculated at the time {@link MetaSpell} is evaluated.
 */
public enum GameValue {
	LAST_MANA_COST,
	/**
	 * @see net.demilich.metastone.game.spells.desc.valueprovider.GameValueProvider for an example of spell value.
	 */
	SPELL_VALUE,
}
