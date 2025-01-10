package com.hiddenswitch.framework.graphql;


/**
 * Get a single `GeneratedArt`.
 */
public interface GeneratedArtByHashAndOwnerQueryResolver {

    /**
     * Get a single `GeneratedArt`.
     */
    GeneratedArt generatedArtByHashAndOwner(String hash, String owner) throws Exception;

}
