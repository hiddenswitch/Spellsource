package com.hiddenswitch.framework.graphql;


public class InviteResponse implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private Invite invite;

    public InviteResponse() {
    }

    public InviteResponse(Invite invite) {
        this.invite = invite;
    }

    public Invite getInvite() {
        return invite;
    }
    public void setInvite(Invite invite) {
        this.invite = invite;
    }



    public static InviteResponse.Builder builder() {
        return new InviteResponse.Builder();
    }

    public static class Builder {

        private Invite invite;

        public Builder() {
        }

        public Builder setInvite(Invite invite) {
            this.invite = invite;
            return this;
        }


        public InviteResponse build() {
            return new InviteResponse(invite);
        }

    }
}
