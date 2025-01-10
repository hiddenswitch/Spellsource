package com.hiddenswitch.framework.graphql;


/**
 * Get a single `DeckShare`.
 */
public interface DeckShareByDeckIdAndShareRecipientIdQueryResolver {

    /**
     * Get a single `DeckShare`.
     */
    DeckShare deckShareByDeckIdAndShareRecipientId(String deckId, String shareRecipientId) throws Exception;

}
