package com.hiddenswitch.framework.graphql;


public class PutCardResult implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String cardId;
    private String editableCardId;
    private java.util.List<String> cardScriptErrors;

    public PutCardResult() {
    }

    public PutCardResult(String cardId, String editableCardId, java.util.List<String> cardScriptErrors) {
        this.cardId = cardId;
        this.editableCardId = editableCardId;
        this.cardScriptErrors = cardScriptErrors;
    }

    public String getCardId() {
        return cardId;
    }
    public void setCardId(String cardId) {
        this.cardId = cardId;
    }

    public String getEditableCardId() {
        return editableCardId;
    }
    public void setEditableCardId(String editableCardId) {
        this.editableCardId = editableCardId;
    }

    public java.util.List<String> getCardScriptErrors() {
        return cardScriptErrors;
    }
    public void setCardScriptErrors(java.util.List<String> cardScriptErrors) {
        this.cardScriptErrors = cardScriptErrors;
    }



    public static PutCardResult.Builder builder() {
        return new PutCardResult.Builder();
    }

    public static class Builder {

        private String cardId;
        private String editableCardId;
        private java.util.List<String> cardScriptErrors;

        public Builder() {
        }

        public Builder setCardId(String cardId) {
            this.cardId = cardId;
            return this;
        }

        public Builder setEditableCardId(String editableCardId) {
            this.editableCardId = editableCardId;
            return this;
        }

        public Builder setCardScriptErrors(java.util.List<String> cardScriptErrors) {
            this.cardScriptErrors = cardScriptErrors;
            return this;
        }


        public PutCardResult build() {
            return new PutCardResult(cardId, editableCardId, cardScriptErrors);
        }

    }
}
