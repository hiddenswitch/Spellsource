package com.hiddenswitch.framework.graphql;


public class PutCardInput implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String editableCardId;
    private String source;
    private Boolean draw;

    public PutCardInput() {
    }

    public PutCardInput(String editableCardId, String source, Boolean draw) {
        this.editableCardId = editableCardId;
        this.source = source;
        this.draw = draw;
    }

    public String getEditableCardId() {
        return editableCardId;
    }
    public void setEditableCardId(String editableCardId) {
        this.editableCardId = editableCardId;
    }

    public String getSource() {
        return source;
    }
    public void setSource(String source) {
        this.source = source;
    }

    public Boolean getDraw() {
        return draw;
    }
    public void setDraw(Boolean draw) {
        this.draw = draw;
    }



    public static PutCardInput.Builder builder() {
        return new PutCardInput.Builder();
    }

    public static class Builder {

        private String editableCardId;
        private String source;
        private Boolean draw;

        public Builder() {
        }

        public Builder setEditableCardId(String editableCardId) {
            this.editableCardId = editableCardId;
            return this;
        }

        public Builder setSource(String source) {
            this.source = source;
            return this;
        }

        public Builder setDraw(Boolean draw) {
            this.draw = draw;
            return this;
        }


        public PutCardInput build() {
            return new PutCardInput(editableCardId, source, draw);
        }

    }
}
