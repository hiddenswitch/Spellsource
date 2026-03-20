package com.hiddenswitch.framework.graphql;


public class DraftsPostInput implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private Boolean startDraft;
    private Boolean retireEarly;

    public DraftsPostInput() {
    }

    public DraftsPostInput(Boolean startDraft, Boolean retireEarly) {
        this.startDraft = startDraft;
        this.retireEarly = retireEarly;
    }

    public Boolean getStartDraft() {
        return startDraft;
    }
    public void setStartDraft(Boolean startDraft) {
        this.startDraft = startDraft;
    }

    public Boolean getRetireEarly() {
        return retireEarly;
    }
    public void setRetireEarly(Boolean retireEarly) {
        this.retireEarly = retireEarly;
    }



    public static DraftsPostInput.Builder builder() {
        return new DraftsPostInput.Builder();
    }

    public static class Builder {

        private Boolean startDraft;
        private Boolean retireEarly;

        public Builder() {
        }

        public Builder setStartDraft(Boolean startDraft) {
            this.startDraft = startDraft;
            return this;
        }

        public Builder setRetireEarly(Boolean retireEarly) {
            this.retireEarly = retireEarly;
            return this;
        }


        public DraftsPostInput build() {
            return new DraftsPostInput(startDraft, retireEarly);
        }

    }
}
