package com.hiddenswitch.framework.graphql;


public class ArtFont implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private ArtColor vertex;

    public ArtFont() {
    }

    public ArtFont(ArtColor vertex) {
        this.vertex = vertex;
    }

    public ArtColor getVertex() {
        return vertex;
    }
    public void setVertex(ArtColor vertex) {
        this.vertex = vertex;
    }



    public static ArtFont.Builder builder() {
        return new ArtFont.Builder();
    }

    public static class Builder {

        private ArtColor vertex;

        public Builder() {
        }

        public Builder setVertex(ArtColor vertex) {
            this.vertex = vertex;
            return this;
        }


        public ArtFont build() {
            return new ArtFont(vertex);
        }

    }
}
