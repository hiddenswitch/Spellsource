package com.hiddenswitch.framework.graphql;


/**
 * ─── Card catalogue ───────────────────────────────────────────
 */
public class CardRecord implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private java.lang.Long id;
    private Entity entity;
    private String cardId;
    private String userId;
    private int count;
    private java.util.List<String> collectionIds;

    public CardRecord() {
    }

    public CardRecord(java.lang.Long id, Entity entity, String cardId, String userId, int count, java.util.List<String> collectionIds) {
        this.id = id;
        this.entity = entity;
        this.cardId = cardId;
        this.userId = userId;
        this.count = count;
        this.collectionIds = collectionIds;
    }

    public java.lang.Long getId() {
        return id;
    }
    public void setId(java.lang.Long id) {
        this.id = id;
    }

    public Entity getEntity() {
        return entity;
    }
    public void setEntity(Entity entity) {
        this.entity = entity;
    }

    public String getCardId() {
        return cardId;
    }
    public void setCardId(String cardId) {
        this.cardId = cardId;
    }

    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getCount() {
        return count;
    }
    public void setCount(int count) {
        this.count = count;
    }

    public java.util.List<String> getCollectionIds() {
        return collectionIds;
    }
    public void setCollectionIds(java.util.List<String> collectionIds) {
        this.collectionIds = collectionIds;
    }



    public static CardRecord.Builder builder() {
        return new CardRecord.Builder();
    }

    public static class Builder {

        private java.lang.Long id;
        private Entity entity;
        private String cardId;
        private String userId;
        private int count;
        private java.util.List<String> collectionIds;

        public Builder() {
        }

        public Builder setId(java.lang.Long id) {
            this.id = id;
            return this;
        }

        public Builder setEntity(Entity entity) {
            this.entity = entity;
            return this;
        }

        public Builder setCardId(String cardId) {
            this.cardId = cardId;
            return this;
        }

        public Builder setUserId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder setCount(int count) {
            this.count = count;
            return this;
        }

        public Builder setCollectionIds(java.util.List<String> collectionIds) {
            this.collectionIds = collectionIds;
            return this;
        }


        public CardRecord build() {
            return new CardRecord(id, entity, cardId, userId, count, collectionIds);
        }

    }
}
