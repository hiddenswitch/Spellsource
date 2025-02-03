package com.hiddenswitch.framework.graphql;


/**
 * Updates a single `Card` using its globally unique id and a patch.
 */
public interface UpdateCardMutationResolver {

    /**
     * Updates a single `Card` using its globally unique id and a patch.
     */
    UpdateCardPayload updateCard(UpdateCardInput input) throws Exception;

}
