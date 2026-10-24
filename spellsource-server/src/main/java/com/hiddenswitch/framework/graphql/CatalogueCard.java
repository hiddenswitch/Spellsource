package com.hiddenswitch.framework.graphql;


/**
 * Published card definitions from the combined Git and database catalogue.
 */
public class CatalogueCard implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String createdBy;
    private java.lang.Object cardScript;

    public CatalogueCard() {
    }

    public CatalogueCard(String id, String createdBy, java.lang.Object cardScript) {
        this.id = id;
        this.createdBy = createdBy;
        this.cardScript = cardScript;
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



    public static CatalogueCard.Builder builder() {
        return new CatalogueCard.Builder();
    }

    public static class Builder {

        private String id;
        private String createdBy;
        private java.lang.Object cardScript;

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


        public CatalogueCard build() {
            return new CatalogueCard(id, createdBy, cardScript);
        }

    }
}
