package com.hiddenswitch.framework.graphql;


/**
 * Get a single `GameUser`.
 */
public interface GameUserByGameIdAndUserIdQueryResolver {

    /**
     * Get a single `GameUser`.
     */
    GameUser gameUserByGameIdAndUserId(String gameId, String userId) throws Exception;

}
