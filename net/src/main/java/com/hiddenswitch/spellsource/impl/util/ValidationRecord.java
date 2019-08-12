package com.hiddenswitch.spellsource.impl.util;

import com.hiddenswitch.spellsource.client.models.ValidationReport;
import org.apache.commons.lang3.ArrayUtils;

import java.io.Serializable;
import java.util.Arrays;
import java.util.stream.Collectors;

public class ValidationRecord implements Serializable {

    private boolean valid;
    private String[] errorMessages;

    public ValidationRecord() {
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public void setErrorMessages(String[] errorMessages) {
        this.errorMessages = errorMessages;
    }

    public ValidationRecord(boolean valid, String[] errorMessages) {
        this.valid = valid;
        this.errorMessages = errorMessages;
    }

    public boolean isValid() {
        return valid;
    }

    public String[] getErrorMessages() {
        return errorMessages;
    }

    public void addErrorMessage(String error) {
        if (errorMessages == null) {
            errorMessages = new String[]{};
        }
        errorMessages = ArrayUtils.add(errorMessages, error);
    }

    public ValidationReport toValidationReport() {
        ValidationReport validationReport = new ValidationReport();
        validationReport.setValid(valid);
        if (errorMessages == null) {
            errorMessages = new String[]{};
        }
        validationReport.setErrors(Arrays.stream(errorMessages).collect(Collectors.toList()));
        return validationReport;
    }

}
