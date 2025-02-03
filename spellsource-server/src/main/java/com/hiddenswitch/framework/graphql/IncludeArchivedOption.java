package com.hiddenswitch.framework.graphql;

/**
 * Indicates whether archived items should be included in the results or not.
 */
public enum IncludeArchivedOption {

    /**
     * Exclude archived items.
     */
    NO("NO"),
    /**
     * Include archived items.
     */
    YES("YES"),
    /**
     * Only include archived items (i.e. exclude non-archived items).
     */
    EXCLUSIVELY("EXCLUSIVELY"),
    /**
     * If there is a parent GraphQL record and it is archived then this is equivalent to YES, in all other cases this is equivalent to NO.
     */
    INHERIT("INHERIT");

    private final String graphqlName;

    private IncludeArchivedOption(String graphqlName) {
        this.graphqlName = graphqlName;
    }

    @Override
    public String toString() {
        return this.graphqlName;
    }

}
