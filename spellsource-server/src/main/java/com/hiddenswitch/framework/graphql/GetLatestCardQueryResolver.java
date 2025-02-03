package com.hiddenswitch.framework.graphql;


public interface GetLatestCardQueryResolver {

    Card getLatestCard(String cardId, Boolean published) throws Exception;

}
