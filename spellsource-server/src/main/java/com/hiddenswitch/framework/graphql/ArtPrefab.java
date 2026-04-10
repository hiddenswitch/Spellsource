package com.hiddenswitch.framework.graphql;


public class ArtPrefab implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String named;

    public ArtPrefab() {
    }

    public ArtPrefab(String named) {
        this.named = named;
    }

    public String getNamed() {
        return named;
    }
    public void setNamed(String named) {
        this.named = named;
    }



    public static ArtPrefab.Builder builder() {
        return new ArtPrefab.Builder();
    }

    public static class Builder {

        private String named;

        public Builder() {
        }

        public Builder setNamed(String named) {
            this.named = named;
            return this;
        }


        public ArtPrefab build() {
            return new ArtPrefab(named);
        }

    }
}
