package com.hiddenswitch.framework.graphql;


/**
 * Reads and enables pagination through a set of `Game`.
 */
public interface AllGamesQueryResolver {

    /**
     * Reads and enables pagination through a set of `Game`.
     */
    GamesConnection allGames(Integer first, Integer last, Integer offset, String before, String after, java.util.List<GamesOrderBy> orderBy, GameCondition condition, GameFilter filter) throws Exception;

}
