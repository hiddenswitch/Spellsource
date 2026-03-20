package com.hiddenswitch.framework.graphql;


public class Timers implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private java.lang.Long millisRemaining;

    public Timers() {
    }

    public Timers(java.lang.Long millisRemaining) {
        this.millisRemaining = millisRemaining;
    }

    public java.lang.Long getMillisRemaining() {
        return millisRemaining;
    }
    public void setMillisRemaining(java.lang.Long millisRemaining) {
        this.millisRemaining = millisRemaining;
    }



    public static Timers.Builder builder() {
        return new Timers.Builder();
    }

    public static class Builder {

        private java.lang.Long millisRemaining;

        public Builder() {
        }

        public Builder setMillisRemaining(java.lang.Long millisRemaining) {
            this.millisRemaining = millisRemaining;
            return this;
        }


        public Timers build() {
            return new Timers(millisRemaining);
        }

    }
}
