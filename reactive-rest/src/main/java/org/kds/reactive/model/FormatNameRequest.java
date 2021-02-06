package org.kds.reactive.model;

import org.kds.reactive.validator.MustContainAt;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class FormatNameRequest {

    @NotNull(message = "Title cannot be null")
    private String title;

    @NotNull
    @Size(message = "First name must be between 2 and 25 characters", min = 2, max = 25)
    private String firstName;

    @MustContainAt(message = "Middle name must contain @ character")
    private String middleName;

    @NotBlank(message = "Last name cannot be empty")
    private String lastName;

    public String getTitle() {
        return title;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public String getLastName() {
        return lastName;
    }
}
