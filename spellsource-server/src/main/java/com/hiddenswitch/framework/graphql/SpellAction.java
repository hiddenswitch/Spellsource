package com.hiddenswitch.framework.graphql;


public class SpellAction implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private int action;
    private ActionType actionType;
    private int sourceId;
    private String description;
    private java.util.List<SpellAction> choices;
    private Entity entity;
    private java.util.List<TargetActionPair> targetKeyToActions;

    public SpellAction() {
    }

    public SpellAction(int action, ActionType actionType, int sourceId, String description, java.util.List<SpellAction> choices, Entity entity, java.util.List<TargetActionPair> targetKeyToActions) {
        this.action = action;
        this.actionType = actionType;
        this.sourceId = sourceId;
        this.description = description;
        this.choices = choices;
        this.entity = entity;
        this.targetKeyToActions = targetKeyToActions;
    }

    public int getAction() {
        return action;
    }
    public void setAction(int action) {
        this.action = action;
    }

    public ActionType getActionType() {
        return actionType;
    }
    public void setActionType(ActionType actionType) {
        this.actionType = actionType;
    }

    public int getSourceId() {
        return sourceId;
    }
    public void setSourceId(int sourceId) {
        this.sourceId = sourceId;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public java.util.List<SpellAction> getChoices() {
        return choices;
    }
    public void setChoices(java.util.List<SpellAction> choices) {
        this.choices = choices;
    }

    public Entity getEntity() {
        return entity;
    }
    public void setEntity(Entity entity) {
        this.entity = entity;
    }

    public java.util.List<TargetActionPair> getTargetKeyToActions() {
        return targetKeyToActions;
    }
    public void setTargetKeyToActions(java.util.List<TargetActionPair> targetKeyToActions) {
        this.targetKeyToActions = targetKeyToActions;
    }



    public static SpellAction.Builder builder() {
        return new SpellAction.Builder();
    }

    public static class Builder {

        private int action;
        private ActionType actionType;
        private int sourceId;
        private String description;
        private java.util.List<SpellAction> choices;
        private Entity entity;
        private java.util.List<TargetActionPair> targetKeyToActions;

        public Builder() {
        }

        public Builder setAction(int action) {
            this.action = action;
            return this;
        }

        public Builder setActionType(ActionType actionType) {
            this.actionType = actionType;
            return this;
        }

        public Builder setSourceId(int sourceId) {
            this.sourceId = sourceId;
            return this;
        }

        public Builder setDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder setChoices(java.util.List<SpellAction> choices) {
            this.choices = choices;
            return this;
        }

        public Builder setEntity(Entity entity) {
            this.entity = entity;
            return this;
        }

        public Builder setTargetKeyToActions(java.util.List<TargetActionPair> targetKeyToActions) {
            this.targetKeyToActions = targetKeyToActions;
            return this;
        }


        public SpellAction build() {
            return new SpellAction(action, actionType, sourceId, description, choices, entity, targetKeyToActions);
        }

    }
}
