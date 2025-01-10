package com.hiddenswitch.framework.graphql;


/**
 * Deletes a single `Card` using its globally unique id.
 */
public interface DeleteCardMutationResolver {

    /**
     * Deletes a single `Card` using its globally unique id.
     */
    DeleteCardPayload deleteCard(DeleteCardInput input) throws Exception;

}
