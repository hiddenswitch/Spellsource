package com.hiddenswitch.framework.graphql;


/**
 * Get a single `Game`.
 */
public interface GameByIdQueryResolver {

    /**
     * Get a single `Game`.
     */
    Game gameById(String id) throws Exception;

}
