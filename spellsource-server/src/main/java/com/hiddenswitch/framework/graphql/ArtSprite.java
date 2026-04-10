package com.hiddenswitch.framework.graphql;


public class ArtSprite implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String named;

    public ArtSprite() {
    }

    public ArtSprite(String named) {
        this.named = named;
    }

    public String getNamed() {
        return named;
    }
    public void setNamed(String named) {
        this.named = named;
    }



    public static ArtSprite.Builder builder() {
        return new ArtSprite.Builder();
    }

    public static class Builder {

        private String named;

        public Builder() {
        }

        public Builder setNamed(String named) {
            this.named = named;
            return this;
        }


        public ArtSprite build() {
            return new ArtSprite(named);
        }

    }
}
