package com.hiddenswitch.framework.graphql;


public class GameEvent implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private GameEventType eventType;
    private int id;
    private String description;
    private boolean isPowerHistory;
    private boolean isSourcePlayerLocal;
    private boolean isTargetPlayerLocal;
    private Entity source;
    private Entity target;
    private java.util.List<Entity> targets;
    private Integer value;
    private CardEvent cardEvent;
    private PerformedGameActionEvent performedGameAction;
    private Integer entityTouched;
    private Integer entityUntouched;

    public GameEvent() {
    }

    public GameEvent(GameEventType eventType, int id, String description, boolean isPowerHistory, boolean isSourcePlayerLocal, boolean isTargetPlayerLocal, Entity source, Entity target, java.util.List<Entity> targets, Integer value, CardEvent cardEvent, PerformedGameActionEvent performedGameAction, Integer entityTouched, Integer entityUntouched) {
        this.eventType = eventType;
        this.id = id;
        this.description = description;
        this.isPowerHistory = isPowerHistory;
        this.isSourcePlayerLocal = isSourcePlayerLocal;
        this.isTargetPlayerLocal = isTargetPlayerLocal;
        this.source = source;
        this.target = target;
        this.targets = targets;
        this.value = value;
        this.cardEvent = cardEvent;
        this.performedGameAction = performedGameAction;
        this.entityTouched = entityTouched;
        this.entityUntouched = entityUntouched;
    }

    public GameEventType getEventType() {
        return eventType;
    }
    public void setEventType(GameEventType eventType) {
        this.eventType = eventType;
    }

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public boolean getIsPowerHistory() {
        return isPowerHistory;
    }
    public void setIsPowerHistory(boolean isPowerHistory) {
        this.isPowerHistory = isPowerHistory;
    }

    public boolean getIsSourcePlayerLocal() {
        return isSourcePlayerLocal;
    }
    public void setIsSourcePlayerLocal(boolean isSourcePlayerLocal) {
        this.isSourcePlayerLocal = isSourcePlayerLocal;
    }

    public boolean getIsTargetPlayerLocal() {
        return isTargetPlayerLocal;
    }
    public void setIsTargetPlayerLocal(boolean isTargetPlayerLocal) {
        this.isTargetPlayerLocal = isTargetPlayerLocal;
    }

    public Entity getSource() {
        return source;
    }
    public void setSource(Entity source) {
        this.source = source;
    }

    public Entity getTarget() {
        return target;
    }
    public void setTarget(Entity target) {
        this.target = target;
    }

    public java.util.List<Entity> getTargets() {
        return targets;
    }
    public void setTargets(java.util.List<Entity> targets) {
        this.targets = targets;
    }

    public Integer getValue() {
        return value;
    }
    public void setValue(Integer value) {
        this.value = value;
    }

    public CardEvent getCardEvent() {
        return cardEvent;
    }
    public void setCardEvent(CardEvent cardEvent) {
        this.cardEvent = cardEvent;
    }

    public PerformedGameActionEvent getPerformedGameAction() {
        return performedGameAction;
    }
    public void setPerformedGameAction(PerformedGameActionEvent performedGameAction) {
        this.performedGameAction = performedGameAction;
    }

    public Integer getEntityTouched() {
        return entityTouched;
    }
    public void setEntityTouched(Integer entityTouched) {
        this.entityTouched = entityTouched;
    }

    public Integer getEntityUntouched() {
        return entityUntouched;
    }
    public void setEntityUntouched(Integer entityUntouched) {
        this.entityUntouched = entityUntouched;
    }



    public static GameEvent.Builder builder() {
        return new GameEvent.Builder();
    }

    public static class Builder {

        private GameEventType eventType;
        private int id;
        private String description;
        private boolean isPowerHistory;
        private boolean isSourcePlayerLocal;
        private boolean isTargetPlayerLocal;
        private Entity source;
        private Entity target;
        private java.util.List<Entity> targets;
        private Integer value;
        private CardEvent cardEvent;
        private PerformedGameActionEvent performedGameAction;
        private Integer entityTouched;
        private Integer entityUntouched;

        public Builder() {
        }

        public Builder setEventType(GameEventType eventType) {
            this.eventType = eventType;
            return this;
        }

        public Builder setId(int id) {
            this.id = id;
            return this;
        }

        public Builder setDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder setIsPowerHistory(boolean isPowerHistory) {
            this.isPowerHistory = isPowerHistory;
            return this;
        }

        public Builder setIsSourcePlayerLocal(boolean isSourcePlayerLocal) {
            this.isSourcePlayerLocal = isSourcePlayerLocal;
            return this;
        }

        public Builder setIsTargetPlayerLocal(boolean isTargetPlayerLocal) {
            this.isTargetPlayerLocal = isTargetPlayerLocal;
            return this;
        }

        public Builder setSource(Entity source) {
            this.source = source;
            return this;
        }

        public Builder setTarget(Entity target) {
            this.target = target;
            return this;
        }

        public Builder setTargets(java.util.List<Entity> targets) {
            this.targets = targets;
            return this;
        }

        public Builder setValue(Integer value) {
            this.value = value;
            return this;
        }

        public Builder setCardEvent(CardEvent cardEvent) {
            this.cardEvent = cardEvent;
            return this;
        }

        public Builder setPerformedGameAction(PerformedGameActionEvent performedGameAction) {
            this.performedGameAction = performedGameAction;
            return this;
        }

        public Builder setEntityTouched(Integer entityTouched) {
            this.entityTouched = entityTouched;
            return this;
        }

        public Builder setEntityUntouched(Integer entityUntouched) {
            this.entityUntouched = entityUntouched;
            return this;
        }


        public GameEvent build() {
            return new GameEvent(eventType, id, description, isPowerHistory, isSourcePlayerLocal, isTargetPlayerLocal, source, target, targets, value, cardEvent, performedGameAction, entityTouched, entityUntouched);
        }

    }
}
