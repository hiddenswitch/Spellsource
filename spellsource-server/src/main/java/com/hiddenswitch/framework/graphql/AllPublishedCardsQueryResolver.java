package com.hiddenswitch.framework.graphql;


/**
 * Reads and enables pagination through a set of `PublishedCard`.
 */
public interface AllPublishedCardsQueryResolver {

    /**
     * Reads and enables pagination through a set of `PublishedCard`.
     */
    PublishedCardsConnection allPublishedCards(Integer first, Integer last, Integer offset, String before, String after, java.util.List<PublishedCardsOrderBy> orderBy, PublishedCardCondition condition, PublishedCardFilter filter) throws Exception;

}
