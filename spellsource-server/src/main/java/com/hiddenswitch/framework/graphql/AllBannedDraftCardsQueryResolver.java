package com.hiddenswitch.framework.graphql;


/**
 * Reads and enables pagination through a set of `BannedDraftCard`.
 */
public interface AllBannedDraftCardsQueryResolver {

    /**
     * Reads and enables pagination through a set of `BannedDraftCard`.
     */
    BannedDraftCardsConnection allBannedDraftCards(Integer first, Integer last, Integer offset, String before, String after, java.util.List<BannedDraftCardsOrderBy> orderBy, BannedDraftCardCondition condition, BannedDraftCardFilter filter) throws Exception;

}
