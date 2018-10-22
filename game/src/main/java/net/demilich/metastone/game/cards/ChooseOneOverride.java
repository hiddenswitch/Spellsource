package net.demilich.metastone.game.cards;

/**
 * When specified on a {@link net.demilich.metastone.game.spells.aura.ChooseOneOverrideAura}, specifies which choose one
 * option is taken by the targeted card.
 */
public enum ChooseOneOverride {
	NONE,
	ALWAYS_FIRST,
	ALWAYS_SECOND,
	BOTH_COMBINED
}