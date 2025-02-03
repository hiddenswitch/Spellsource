package com.hiddenswitch.framework.graphql;


/**
 * Reads and enables pagination through a set of `DeckPlayerAttributeTuple`.
 */
public interface AllDeckPlayerAttributeTuplesQueryResolver {

    /**
     * Reads and enables pagination through a set of `DeckPlayerAttributeTuple`.
     */
    DeckPlayerAttributeTuplesConnection allDeckPlayerAttributeTuples(Integer first, Integer last, Integer offset, String before, String after, java.util.List<DeckPlayerAttributeTuplesOrderBy> orderBy, DeckPlayerAttributeTupleCondition condition, DeckPlayerAttributeTupleFilter filter) throws Exception;

}
