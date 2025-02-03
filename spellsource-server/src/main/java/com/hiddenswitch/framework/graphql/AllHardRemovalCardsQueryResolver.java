package com.hiddenswitch.framework.graphql;


/**
 * Reads and enables pagination through a set of `HardRemovalCard`.
 */
public interface AllHardRemovalCardsQueryResolver {

    /**
     * Reads and enables pagination through a set of `HardRemovalCard`.
     */
    HardRemovalCardsConnection allHardRemovalCards(Integer first, Integer last, Integer offset, String before, String after, java.util.List<HardRemovalCardsOrderBy> orderBy, HardRemovalCardCondition condition, HardRemovalCardFilter filter) throws Exception;

}
