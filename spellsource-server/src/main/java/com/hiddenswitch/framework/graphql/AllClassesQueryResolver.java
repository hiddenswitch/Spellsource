package com.hiddenswitch.framework.graphql;


/**
 * Reads and enables pagination through a set of `Class`.
 */
public interface AllClassesQueryResolver {

    /**
     * Reads and enables pagination through a set of `Class`.
     */
    ClassesConnection allClasses(Integer first, Integer last, Integer offset, String before, String after, java.util.List<ClassesOrderBy> orderBy, ClassCondition condition, ClassFilter filter) throws Exception;

}
