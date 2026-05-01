package com.hiddenswitch.framework.graphql;


public class PerformedGameActionEvent implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private ActionType actionType;

    public PerformedGameActionEvent() {
    }

    public PerformedGameActionEvent(ActionType actionType) {
        this.actionType = actionType;
    }

    public ActionType getActionType() {
        return actionType;
    }
    public void setActionType(ActionType actionType) {
        this.actionType = actionType;
    }



    public static PerformedGameActionEvent.Builder builder() {
        return new PerformedGameActionEvent.Builder();
    }

    public static class Builder {

        private ActionType actionType;

        public Builder() {
        }

        public Builder setActionType(ActionType actionType) {
            this.actionType = actionType;
            return this;
        }


        public PerformedGameActionEvent build() {
            return new PerformedGameActionEvent(actionType);
        }

    }
}
