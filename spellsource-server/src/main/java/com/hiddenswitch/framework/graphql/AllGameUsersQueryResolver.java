package com.hiddenswitch.framework.graphql;


/**
 * Reads and enables pagination through a set of `GameUser`.
 */
public interface AllGameUsersQueryResolver {

    /**
     * Reads and enables pagination through a set of `GameUser`.
     */
    GameUsersConnection allGameUsers(Integer first, Integer last, Integer offset, String before, String after, java.util.List<GameUsersOrderBy> orderBy, GameUserCondition condition, GameUserFilter filter) throws Exception;

}
