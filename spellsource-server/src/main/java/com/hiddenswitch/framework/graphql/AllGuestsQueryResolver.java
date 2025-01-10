package com.hiddenswitch.framework.graphql;


/**
 * Reads and enables pagination through a set of `Guest`.
 */
public interface AllGuestsQueryResolver {

    /**
     * Reads and enables pagination through a set of `Guest`.
     */
    GuestsConnection allGuests(Integer first, Integer last, Integer offset, String before, String after, java.util.List<GuestsOrderBy> orderBy, GuestCondition condition, GuestFilter filter) throws Exception;

}
