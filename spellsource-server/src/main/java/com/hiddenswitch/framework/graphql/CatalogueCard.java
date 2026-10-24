package com.hiddenswitch.framework.graphql;


/**
 * Published card definitions from the combined Git and database catalogue.
 */
public class CatalogueCard implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String createdBy;
    private java.lang.Object cardScript;
    private String lastModified;

    public CatalogueCard() {
    }

    public CatalogueCard(String id, String createdBy, java.lang.Object cardScript, String lastModified) {
        this.id = id;
        this.createdBy = createdBy;
        this.cardScript = cardScript;
        this.lastModified = lastModified;
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

    public java.lang.Object getCardScript() {
        return cardScript;
    }
    public void setCardScript(java.lang.Object cardScript) {
        this.cardScript = cardScript;
    }

    public String getLastModified() {
        return lastModified;
    }
    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }



    public static CatalogueCard.Builder builder() {
        return new CatalogueCard.Builder();
    }

    public static class Builder {

        private String id;
        private String createdBy;
        private java.lang.Object cardScript;
        private String lastModified;

        public Builder() {
        }

        public Builder setId(String id) {
            this.id = id;
            return this;
        }

        public Builder setCreatedBy(String createdBy) {
            this.createdBy = createdBy;
            return this;
        }

        public Builder setCardScript(java.lang.Object cardScript) {
            this.cardScript = cardScript;
            return this;
        }

        public Builder setLastModified(String lastModified) {
            this.lastModified = lastModified;
            return this;
        }


        public CatalogueCard build() {
            return new CatalogueCard(id, createdBy, cardScript, lastModified);
        }

    }
}
