package com.hiddenswitch.framework.graphql;


/**
 * Updates a single `HardRemovalCard` using its globally unique id and a patch.
 */
public interface UpdateHardRemovalCardMutationResolver {

    /**
     * Updates a single `HardRemovalCard` using its globally unique id and a patch.
     */
    UpdateHardRemovalCardPayload updateHardRemovalCard(UpdateHardRemovalCardInput input) throws Exception;

}
