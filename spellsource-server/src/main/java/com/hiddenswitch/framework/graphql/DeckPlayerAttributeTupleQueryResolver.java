package com.hiddenswitch.framework.graphql;


/**
 * Reads a single `DeckPlayerAttributeTuple` using its globally unique `ID`.
 */
public interface DeckPlayerAttributeTupleQueryResolver {

    /**
     * Reads a single `DeckPlayerAttributeTuple` using its globally unique `ID`.
     */
    DeckPlayerAttributeTuple deckPlayerAttributeTuple(String nodeId) throws Exception;

}
