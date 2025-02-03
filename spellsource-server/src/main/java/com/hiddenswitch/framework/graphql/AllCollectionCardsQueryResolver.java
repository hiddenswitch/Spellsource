package com.hiddenswitch.framework.graphql;


/**
 * Reads and enables pagination through a set of `CollectionCard`.
 */
public interface AllCollectionCardsQueryResolver {

    /**
     * Reads and enables pagination through a set of `CollectionCard`.
     */
    CollectionCardsConnection allCollectionCards(Integer first, Integer last, Integer offset, String before, String after, java.util.List<CollectionCardsOrderBy> orderBy, CollectionCardCondition condition, CollectionCardFilter filter) throws Exception;

}
