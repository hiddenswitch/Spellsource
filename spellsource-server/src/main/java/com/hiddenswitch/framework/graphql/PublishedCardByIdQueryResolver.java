package com.hiddenswitch.framework.graphql;


/**
 * Get a single `PublishedCard`.
 */
public interface PublishedCardByIdQueryResolver {

    /**
     * Get a single `PublishedCard`.
     */
    PublishedCard publishedCardById(String id) throws Exception;

}
