package org.kds.reactive.model;

import java.util.List;

public class FormattedNameResponse {

    private String formattedName;

    private List<Error> errors;

    public String getFormattedName() {
        return formattedName;
    }

    public void setFormattedName(String formattedName) {
        this.formattedName = formattedName;
    }

    public List<Error> getErrors() {
        return errors;
    }

    public void setErrors(List<Error> errors) {
        this.errors = errors;
    }
}
