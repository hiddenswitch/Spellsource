package com.hiddenswitch.framework.graphql;


/**
 * Creates a single `Card`.
 */
public interface CreateCardMutationResolver {

    /**
     * Creates a single `Card`.
     */
    CreateCardPayload createCard(CreateCardInput input) throws Exception;

}
