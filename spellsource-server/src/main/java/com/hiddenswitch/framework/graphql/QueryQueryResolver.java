package com.hiddenswitch.framework.graphql;


/**
 * Exposes the root query type nested one level down. This is helpful for Relay 1
which can only query top level fields if they are in a particular form.
 */
public interface QueryQueryResolver {

    /**
     * Exposes the root query type nested one level down. This is helpful for Relay 1
which can only query top level fields if they are in a particular form.
     */
    Query query() throws Exception;

}
