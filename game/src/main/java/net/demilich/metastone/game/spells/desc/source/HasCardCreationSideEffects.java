package net.demilich.metastone.game.spells.desc.source;

/**
 * Indicates that this object creates cards as a side effect of its methods.
 *
 * For <b>example,</b> a {@link CatalogueSource} creates cards, while a {@link GraveyardCardsSource} does not.
 *
 * @see CardSource for where this is used.
 */
public interface HasCardCreationSideEffects {
}
