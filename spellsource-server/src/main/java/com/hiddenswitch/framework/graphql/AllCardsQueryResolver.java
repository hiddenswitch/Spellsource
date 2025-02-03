package com.hiddenswitch.framework.graphql;


/**
 * Reads and enables pagination through a set of `Card`.
 */
public interface AllCardsQueryResolver {

    /**
     * Reads and enables pagination through a set of `Card`.
     */
    CardsConnection allCards(Integer first, Integer last, Integer offset, String before, String after, java.util.List<CardsOrderBy> orderBy, CardCondition condition, CardFilter filter, IncludeArchivedOption includeArchived) throws Exception;

}
