package com.hiddenswitch.framework.graphql;


/**
 * Deletes a single `HardRemovalCard` using its globally unique id.
 */
public interface DeleteHardRemovalCardMutationResolver {

    /**
     * Deletes a single `HardRemovalCard` using its globally unique id.
     */
    DeleteHardRemovalCardPayload deleteHardRemovalCard(DeleteHardRemovalCardInput input) throws Exception;

}
