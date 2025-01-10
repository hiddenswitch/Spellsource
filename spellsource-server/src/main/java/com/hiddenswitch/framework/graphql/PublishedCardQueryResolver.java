package com.hiddenswitch.framework.graphql;


/**
 * Reads a single `PublishedCard` using its globally unique `ID`.
 */
public interface PublishedCardQueryResolver {

    /**
     * Reads a single `PublishedCard` using its globally unique `ID`.
     */
    PublishedCard publishedCard(String nodeId) throws Exception;

}
