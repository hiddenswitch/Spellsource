package com.hiddenswitch.framework.graphql;


/**
 * Reads and enables pagination through a set of `CardsInDeck`.
 */
public interface AllCardsInDecksQueryResolver {

    /**
     * Reads and enables pagination through a set of `CardsInDeck`.
     */
    CardsInDecksConnection allCardsInDecks(Integer first, Integer last, Integer offset, String before, String after, java.util.List<CardsInDecksOrderBy> orderBy, CardsInDeckCondition condition, CardsInDeckFilter filter) throws Exception;

}
