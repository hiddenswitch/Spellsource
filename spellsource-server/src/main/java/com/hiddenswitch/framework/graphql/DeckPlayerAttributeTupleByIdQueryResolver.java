package com.hiddenswitch.framework.graphql;


/**
 * Get a single `DeckPlayerAttributeTuple`.
 */
public interface DeckPlayerAttributeTupleByIdQueryResolver {

    /**
     * Get a single `DeckPlayerAttributeTuple`.
     */
    DeckPlayerAttributeTuple deckPlayerAttributeTupleById(String id) throws Exception;

}
