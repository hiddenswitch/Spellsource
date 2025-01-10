package com.hiddenswitch.framework.graphql;


/**
 * Reads and enables pagination through a set of `BotUser`.
 */
public interface AllBotUsersQueryResolver {

    /**
     * Reads and enables pagination through a set of `BotUser`.
     */
    BotUsersConnection allBotUsers(Integer first, Integer last, Integer offset, String before, String after, java.util.List<BotUsersOrderBy> orderBy, BotUserCondition condition, BotUserFilter filter) throws Exception;

}
