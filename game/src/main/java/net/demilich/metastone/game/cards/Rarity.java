package net.demilich.metastone.game.cards;

import com.hiddenswitch.spellsource.client.models.EntityState;

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
