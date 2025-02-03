package com.hiddenswitch.framework.graphql;


public class Card implements java.io.Serializable, Node {

    private static final long serialVersionUID = 1L;

    private String nodeId;
    private Boolean collectible;
    private String type;
    private Integer cost;
    private String id;
    private String createdBy;
    private String uri;
    private String blocklyWorkspace;
    private String cardScript;
    private String createdAt;
    private String lastModified;
    private boolean isArchived;
    private boolean isPublished;
    private String succession;
    private PublishedCardsConnection publishedCardsBySuccession;

    public Card() {
    }

    public Card(String nodeId, Boolean collectible, String type, Integer cost, String id, String createdBy, String uri, String blocklyWorkspace, String cardScript, String createdAt, String lastModified, boolean isArchived, boolean isPublished, String succession, PublishedCardsConnection publishedCardsBySuccession) {
        this.nodeId = nodeId;
        this.collectible = collectible;
        this.type = type;
        this.cost = cost;
        this.id = id;
        this.createdBy = createdBy;
        this.uri = uri;
        this.blocklyWorkspace = blocklyWorkspace;
        this.cardScript = cardScript;
        this.createdAt = createdAt;
        this.lastModified = lastModified;
        this.isArchived = isArchived;
        this.isPublished = isPublished;
        this.succession = succession;
        this.publishedCardsBySuccession = publishedCardsBySuccession;
    }

    /**
     * A globally unique identifier. Can be used in various places throughout the system to identify this single value.
     */
    public String getNodeId() {
        return nodeId;
    }
    /**
     * A globally unique identifier. Can be used in various places throughout the system to identify this single value.
     */
    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public Boolean getCollectible() {
        return collectible;
    }
    public void setCollectible(Boolean collectible) {
        this.collectible = collectible;
    }

    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }

    public Integer getCost() {
        return cost;
    }
    public void setCost(Integer cost) {
        this.cost = cost;
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getCreatedBy() {
        return createdBy;
    }
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    /**
     * The URI of the application that created this card. The git URL by default represents cards that came from the
    Spellsource git repository. https://www.getspellsource.com/cards/editor or similar represents cards authored in the
    web interface
     */
    public String getUri() {
        return uri;
    }
    /**
     * The URI of the application that created this card. The git URL by default represents cards that came from the
    Spellsource git repository. https://www.getspellsource.com/cards/editor or similar represents cards authored in the
    web interface
     */
    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getBlocklyWorkspace() {
        return blocklyWorkspace;
    }
    public void setBlocklyWorkspace(String blocklyWorkspace) {
        this.blocklyWorkspace = blocklyWorkspace;
    }

    public String getCardScript() {
        return cardScript;
    }
    public void setCardScript(String cardScript) {
        this.cardScript = cardScript;
    }

    public String getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getLastModified() {
        return lastModified;
    }
    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }

    public boolean getIsArchived() {
        return isArchived;
    }
    public void setIsArchived(boolean isArchived) {
        this.isArchived = isArchived;
    }

    public boolean getIsPublished() {
        return isPublished;
    }
    public void setIsPublished(boolean isPublished) {
        this.isPublished = isPublished;
    }

    public String getSuccession() {
        return succession;
    }
    public void setSuccession(String succession) {
        this.succession = succession;
    }

    /**
     * Reads and enables pagination through a set of `PublishedCard`.
     */
    public PublishedCardsConnection getPublishedCardsBySuccession() {
        return publishedCardsBySuccession;
    }
    /**
     * Reads and enables pagination through a set of `PublishedCard`.
     */
    public void setPublishedCardsBySuccession(PublishedCardsConnection publishedCardsBySuccession) {
        this.publishedCardsBySuccession = publishedCardsBySuccession;
    }



    public static Card.Builder builder() {
        return new Card.Builder();
    }

    public static class Builder {

        private String nodeId;
        private Boolean collectible;
        private String type;
        private Integer cost;
        private String id;
        private String createdBy;
        private String uri;
        private String blocklyWorkspace;
        private String cardScript;
        private String createdAt;
        private String lastModified;
        private boolean isArchived;
        private boolean isPublished;
        private String succession;
        private PublishedCardsConnection publishedCardsBySuccession;

        public Builder() {
        }

        /**
         * A globally unique identifier. Can be used in various places throughout the system to identify this single value.
         */
        public Builder setNodeId(String nodeId) {
            this.nodeId = nodeId;
            return this;
        }

        public Builder setCollectible(Boolean collectible) {
            this.collectible = collectible;
            return this;
        }

        public Builder setType(String type) {
            this.type = type;
            return this;
        }

        public Builder setCost(Integer cost) {
            this.cost = cost;
            return this;
        }

        public Builder setId(String id) {
            this.id = id;
            return this;
        }

        public Builder setCreatedBy(String createdBy) {
            this.createdBy = createdBy;
            return this;
        }

        /**
         * The URI of the application that created this card. The git URL by default represents cards that came from the
    Spellsource git repository. https://www.getspellsource.com/cards/editor or similar represents cards authored in the
    web interface
         */
        public Builder setUri(String uri) {
            this.uri = uri;
            return this;
        }

        public Builder setBlocklyWorkspace(String blocklyWorkspace) {
            this.blocklyWorkspace = blocklyWorkspace;
            return this;
        }

        public Builder setCardScript(String cardScript) {
            this.cardScript = cardScript;
            return this;
        }

        public Builder setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder setLastModified(String lastModified) {
            this.lastModified = lastModified;
            return this;
        }

        public Builder setIsArchived(boolean isArchived) {
            this.isArchived = isArchived;
            return this;
        }

        public Builder setIsPublished(boolean isPublished) {
            this.isPublished = isPublished;
            return this;
        }

        public Builder setSuccession(String succession) {
            this.succession = succession;
            return this;
        }

        /**
         * Reads and enables pagination through a set of `PublishedCard`.
         */
        public Builder setPublishedCardsBySuccession(PublishedCardsConnection publishedCardsBySuccession) {
            this.publishedCardsBySuccession = publishedCardsBySuccession;
            return this;
        }


        public Card build() {
            return new Card(nodeId, collectible, type, cost, id, createdBy, uri, blocklyWorkspace, cardScript, createdAt, lastModified, isArchived, isPublished, succession, publishedCardsBySuccession);
        }

    }
}
