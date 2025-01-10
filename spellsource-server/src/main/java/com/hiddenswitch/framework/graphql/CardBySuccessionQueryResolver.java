package com.hiddenswitch.framework.graphql;


/**
 * Get a single `Card`.
 */
public interface CardBySuccessionQueryResolver {

    /**
     * Get a single `Card`.
     */
    Card cardBySuccession(String succession) throws Exception;

}
