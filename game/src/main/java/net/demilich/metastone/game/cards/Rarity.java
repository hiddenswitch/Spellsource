package net.demilich.metastone.game.cards;

import com.hiddenswitch.spellsource.client.models.EntityState;

/**
 * Possible rarities of different cards.
 * <p>
 * Decks typically can only have two or fewer of all rarities, and one or fewer of {@link #LEGENDARY} cards.
 * <p>
 * These rarities affect some effects, like {@link net.demilich.metastone.game.spells.desc.filter.CardFilter}, which can
 * select cards of a specific rarity; the draft logic in the {@code net} module; and the gem that appears in the middle
 * of the card.
 */
public enum Rarity {
	FREE,
	COMMON,
	RARE,
	EPIC,
	LEGENDARY,
	ALLIANCE;

	public boolean isRarity(Rarity rarity) {
		if (this == FREE && rarity == COMMON) {
			return true;
		} else if (this == rarity) {
			return true;
		}
		return false;
	}

	public EntityState.RarityEnum getClientRarity() {
		switch (this) {
			case FREE:
				return EntityState.RarityEnum.FREE;
			case COMMON:
				return EntityState.RarityEnum.COMMON;
			case RARE:
				return EntityState.RarityEnum.RARE;
			case EPIC:
				return EntityState.RarityEnum.EPIC;
			case LEGENDARY:
				return EntityState.RarityEnum.LEGENDARY;
			case ALLIANCE:
				return EntityState.RarityEnum.ALLIANCE;
		}
		return null;
	}
}
