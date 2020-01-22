package net.demilich.metastone.game.cards;

import com.hiddenswitch.spellsource.client.models.Entity;

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

	public Entity.RarityEnum getClientRarity() {
		switch (this) {
			case FREE:
				return Entity.RarityEnum.FREE;
			case COMMON:
				return Entity.RarityEnum.COMMON;
			case RARE:
				return Entity.RarityEnum.RARE;
			case EPIC:
				return Entity.RarityEnum.EPIC;
			case LEGENDARY:
				return Entity.RarityEnum.LEGENDARY;
			case ALLIANCE:
				return Entity.RarityEnum.ALLIANCE;
		}
		return null;
	}
}
