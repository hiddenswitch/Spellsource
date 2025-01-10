package com.hiddenswitch.framework.graphql;


/**
 * Reads and enables pagination through a set of `GeneratedArt`.
 */
public interface AllGeneratedArtsQueryResolver {

    /**
     * Reads and enables pagination through a set of `GeneratedArt`.
     */
    GeneratedArtsConnection allGeneratedArts(Integer first, Integer last, Integer offset, String before, String after, java.util.List<GeneratedArtsOrderBy> orderBy, GeneratedArtCondition condition, GeneratedArtFilter filter, IncludeArchivedOption includeArchived) throws Exception;

}
