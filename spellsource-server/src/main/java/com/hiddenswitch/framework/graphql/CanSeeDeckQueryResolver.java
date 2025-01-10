package com.hiddenswitch.framework.graphql;


public interface CanSeeDeckQueryResolver {

    Boolean canSeeDeck(String userId, DeckInput deck) throws Exception;

}
