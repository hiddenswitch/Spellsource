package com.hiddenswitch.framework.graphql;


/**
 * Get a single `HardRemovalCard`.
 */
public interface HardRemovalCardByCardIdQueryResolver {

    /**
     * Get a single `HardRemovalCard`.
     */
    HardRemovalCard hardRemovalCardByCardId(String cardId) throws Exception;

}
