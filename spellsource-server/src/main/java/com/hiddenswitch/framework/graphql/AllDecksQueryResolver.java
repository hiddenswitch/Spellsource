package com.hiddenswitch.framework.graphql;


/**
 * Reads and enables pagination through a set of `Deck`.
 */
public interface AllDecksQueryResolver {

    /**
     * Reads and enables pagination through a set of `Deck`.
     */
    DecksConnection allDecks(Integer first, Integer last, Integer offset, String before, String after, java.util.List<DecksOrderBy> orderBy, DeckCondition condition, DeckFilter filter) throws Exception;

}
