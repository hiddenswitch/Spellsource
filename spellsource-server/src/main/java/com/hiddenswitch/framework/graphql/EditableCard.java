package com.hiddenswitch.framework.graphql;


/**
 * ─── Editable card types ─────────────────────────────────────
 */
public class EditableCard implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String ownerUserId;
    private String source;

    public EditableCard() {
    }

    public EditableCard(String id, String ownerUserId, String source) {
        this.id = id;
        this.ownerUserId = ownerUserId;
        this.source = source;
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getOwnerUserId() {
        return ownerUserId;
    }
    public void setOwnerUserId(String ownerUserId) {
        this.ownerUserId = ownerUserId;
    }

    public String getSource() {
        return source;
    }
    public void setSource(String source) {
        this.source = source;
    }



    public static EditableCard.Builder builder() {
        return new EditableCard.Builder();
    }

    public static class Builder {

        private String id;
        private String ownerUserId;
        private String source;

        public Builder() {
        }

        public Builder setId(String id) {
            this.id = id;
            return this;
        }

        public Builder setOwnerUserId(String ownerUserId) {
            this.ownerUserId = ownerUserId;
            return this;
        }

        public Builder setSource(String source) {
            this.source = source;
            return this;
        }


        public EditableCard build() {
            return new EditableCard(id, ownerUserId, source);
        }

    }
}
