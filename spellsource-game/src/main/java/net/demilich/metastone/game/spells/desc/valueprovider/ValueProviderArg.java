package net.demilich.metastone.game.spells.desc.valueprovider;

public enum ValueProviderArg {
	CLASS,
	TARGET,
	ATTRIBUTE,
	PLAYER_ATTRIBUTE,
	VALUE,
	OFFSET,
	MULTIPLIER,
	RACE,
	TARGET_PLAYER,
	IF_TRUE,
	IF_FALSE,
	CONDITION,
	FILTER,
	OPERATION,
	VALUE1,
	VALUE2,
	GAME_VALUE,
	MIN,
	MAX,
	CARD_SOURCE,
	CARD_FILTER,
	/**
	 * Deprecated. Retained so Jackson can still deserialize card documents
	 * persisted in Postgres before this arg was removed; the R__ card-catalogue
	 * migration will rewrite those rows with fresh card content and drop it.
	 */
	EVALUATE_ONCE
}
