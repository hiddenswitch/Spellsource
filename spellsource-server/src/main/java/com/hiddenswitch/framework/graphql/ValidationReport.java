package com.hiddenswitch.framework.graphql;


public class ValidationReport implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private java.util.List<String> errors;
    private boolean valid;

    public ValidationReport() {
    }

    public ValidationReport(java.util.List<String> errors, boolean valid) {
        this.errors = errors;
        this.valid = valid;
    }

    public java.util.List<String> getErrors() {
        return errors;
    }
    public void setErrors(java.util.List<String> errors) {
        this.errors = errors;
    }

    public boolean getValid() {
        return valid;
    }
    public void setValid(boolean valid) {
        this.valid = valid;
    }



    public static ValidationReport.Builder builder() {
        return new ValidationReport.Builder();
    }

    public static class Builder {

        private java.util.List<String> errors;
        private boolean valid;

        public Builder() {
        }

        public Builder setErrors(java.util.List<String> errors) {
            this.errors = errors;
            return this;
        }

        public Builder setValid(boolean valid) {
            this.valid = valid;
            return this;
        }


        public ValidationReport build() {
            return new ValidationReport(errors, valid);
        }

    }
}
