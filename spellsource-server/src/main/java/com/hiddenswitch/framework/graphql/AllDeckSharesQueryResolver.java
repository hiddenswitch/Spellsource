package com.hiddenswitch.framework.graphql;


/**
 * Reads and enables pagination through a set of `DeckShare`.
 */
public interface AllDeckSharesQueryResolver {

    /**
     * Reads and enables pagination through a set of `DeckShare`.
     */
    DeckSharesConnection allDeckShares(Integer first, Integer last, Integer offset, String before, String after, java.util.List<DeckSharesOrderBy> orderBy, DeckShareCondition condition, DeckShareFilter filter) throws Exception;

}
