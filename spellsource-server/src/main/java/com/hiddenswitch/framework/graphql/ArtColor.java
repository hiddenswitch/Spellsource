package com.hiddenswitch.framework.graphql;


/**
 * ─── Art types ────────────────────────────────────────────────
 */
public class ArtColor implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private double r;
    private double g;
    private double b;
    private double a;

    public ArtColor() {
    }

    public ArtColor(double r, double g, double b, double a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    public double getR() {
        return r;
    }
    public void setR(double r) {
        this.r = r;
    }

    public double getG() {
        return g;
    }
    public void setG(double g) {
        this.g = g;
    }

    public double getB() {
        return b;
    }
    public void setB(double b) {
        this.b = b;
    }

    public double getA() {
        return a;
    }
    public void setA(double a) {
        this.a = a;
    }



    public static ArtColor.Builder builder() {
        return new ArtColor.Builder();
    }

    public static class Builder {

        private double r;
        private double g;
        private double b;
        private double a;

        public Builder() {
        }

        public Builder setR(double r) {
            this.r = r;
            return this;
        }

        public Builder setG(double g) {
            this.g = g;
            return this;
        }

        public Builder setB(double b) {
            this.b = b;
            return this;
        }

        public Builder setA(double a) {
            this.a = a;
            return this;
        }


        public ArtColor build() {
            return new ArtColor(r, g, b, a);
        }

    }
}
